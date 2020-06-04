/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.repo.replication;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.addon.replication.PushReplicationSettings;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.replication.GlobalReplicationsConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.repo.service.ImportJob;
import org.artifactory.schedule.*;
import org.artifactory.schedule.quartz.QuartzCommand;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.spring.InternalContextHelper;
import org.jfrog.storage.common.ConflictGuard;
import org.jfrog.storage.common.ConflictsGuard;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.artifactory.storage.db.locks.service.DbLocksService.REPLICATION_LOCK_CATEGORY;

/**
 * @author Noam Y. Tenne
 */
@JobCommand(runOnlyOnPrimary = false, schedulerUser = TaskUser.SYSTEM, manualUser = TaskUser.CURRENT,
        keyAttributes = {Task.REPO_KEY, Task.PUSH_REPLICATION_URL},
        commandsToStop = {
                @StopCommand(command = ImportJob.class, strategy = StopStrategy.IMPOSSIBLE)
        }
)
public class PushReplicationJob extends QuartzCommand {
    private static final Logger log = LoggerFactory.getLogger(PushReplicationJob.class);

    @Override
    protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
        InternalArtifactoryContext context = InternalContextHelper.get();
        if (!context.isReady()) {
            log.debug("Skipping execution of '{}', server is not ready yet", PushReplicationJob.class.getName());
            return;
        }

        LocalReplicationDescriptor replication = replicationDescriptorFromJobOrConfig(context, callbackContext);
        if (replication == null) {
            log.warn("Unable to execute replication for repo: cannot find replication descriptor.");
            return;
        }

        String replicationKey = replication.getReplicationKey();
        AddonsManager addonsManager = context.beanForType(AddonsManager.class);
        if (ConstantValues.replicationPrimaryOnly.getBoolean()) {
            if (isPrimary(addonsManager)) {
                handleReplication(replication, context, addonsManager);
            } else {
                propagateIfNeeded(callbackContext, addonsManager);
            }
        } else {
            attemptToReplicate(context, replication, replicationKey, addonsManager);
        }
    }


    /**
     * try to acquire lock on replication key and perform replication if succeeded
     */
    private void attemptToReplicate(InternalArtifactoryContext context, LocalReplicationDescriptor replication,
            String replicationKey, AddonsManager addonsManager) {
        ConflictsGuard<String> replicationLockingMap = getReplicationLockingMap(context);
        ConflictGuard lockObject = replicationLockingMap.getLock(replicationKey);
        boolean lockAcquired = false;
        try {
            log.debug("Attempting to acquire lock on {}", replicationKey);
            lockAcquired = lockObject.tryToLock(0, TimeUnit.SECONDS);
            if (lockAcquired) {
                log.debug("Lock acquired on: {}", replicationKey);
                handleReplication(replication, context, addonsManager);
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while trying to acquire replication lock on {}: {}", replicationKey, e);
            Thread.currentThread().interrupt();
        } finally {
            if (lockAcquired) {
                log.debug("Releasing lock for {}", replicationKey);
                lockObject.unlock();
            }
        }
    }

    private void propagateIfNeeded(JobExecutionContext callbackContext, AddonsManager addonsManager) {
        if (callbackContext.getMergedJobDataMap().get(ReplicationAddon.TASK_MANUAL_DESCRIPTOR) != null) {
                Task taskToPropagate = createManualTask(callbackContext);
                log.debug("Propagating task to master {}", taskToPropagate.getToken());
                addonsManager.addonByType(HaAddon.class).propagateTaskToPrimary(taskToPropagate);
        }
    }

    private Task createManualTask(JobExecutionContext callbackContext) {
        TaskBase manualTask = TaskUtils.createManualTask(PushReplicationJob.class, 0);
        callbackContext.getMergedJobDataMap().getWrappedMap().forEach(manualTask::addAttribute);
        return manualTask;
    }

    //TODO [by shayb]: replace this with unit test?
    private void assertValidJobCommand(JobExecutionContext callbackContext) {
        if (isRunOnlyOnMaster(callbackContext)) {
            throw new IllegalStateException("runOnlyOnPrimary is not allowed in PushReplicationJob JobCommand.");
        }
    }

    private void handleReplication(LocalReplicationDescriptor replication, InternalArtifactoryContext context,
            AddonsManager addonsManager) {
        GlobalReplicationsConfigDescriptor replications = context.getCentralConfig().getDescriptor()
                .getReplicationsConfig();
        if (replications.isBlockPushReplications()) {
            log.warn("Skipping execution of '{}', push replications are disabled globally",
                    PushReplicationJob.class.getName());
            return;
        }

        PushReplicationSettings settings = new PushReplicationSettings(replication);

        SecurityService securityService = context.beanForType(SecurityService.class);
        try {
            securityService.authenticateAsSystem();
            ReplicationAddon replicationAddon = addonsManager.addonByType(ReplicationAddon.class);
            replicationAddon.performLocalReplication(settings);
        } catch (Exception e) {
            log.error("An error occurred while performing replication for repository '{}'",
                    replication.getRepoKey(), e);
        } finally {
            securityService.nullifyContext();
        }
    }

    private boolean isPrimary(AddonsManager addonsManager) {
        return addonsManager.addonByType(HaAddon.class).isPrimary();
    }

    private ConflictsGuard<String> getReplicationLockingMap(InternalArtifactoryContext context) {
        HaAddon haAddon = context.beanForType(AddonsManager.class).addonByType(HaAddon.class);
        return haAddon.getConflictsGuard(REPLICATION_LOCK_CATEGORY);
    }

    private LocalReplicationDescriptor replicationDescriptorFromJobOrConfig(InternalArtifactoryContext context,
            JobExecutionContext callbackContext) {
        JobDetail jobDetail = callbackContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        Object manualInvocationDescriptor = jobDataMap.get(ReplicationAddon.TASK_MANUAL_DESCRIPTOR);
        if (manualInvocationDescriptor != null) {
            return ((LocalReplicationDescriptor) manualInvocationDescriptor);
        } else {
            CentralConfigService centralConfig = context.getCentralConfig();
            CentralConfigDescriptor centralConfigDescriptor = centralConfig.getDescriptor();
            return centralConfigDescriptor.getLocalReplication(jobDataMap.getString(Task.REPO_KEY),
                    jobDataMap.getString(Task.PUSH_REPLICATION_URL));
        }
    }
}
