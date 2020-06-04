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

package org.artifactory.lock.service;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.artifactory.lock.LockingProviderTypeEnum;
import org.artifactory.schedule.*;
import org.artifactory.schedule.quartz.QuartzCommand;
import org.artifactory.storage.db.locks.service.DbLocksService;
import org.artifactory.storage.db.servers.model.ArtifactoryServerInfo;
import org.artifactory.storage.db.servers.service.ArtifactoryServersCommonService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.artifactory.common.ConstantValues.haHeartbeatStaleIntervalSecs;

/**
 * @author gidis
 */
@Service
public class LockServiceImpl implements LockService {

    private TaskService taskService;

    @Autowired
    public LockServiceImpl(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostConstruct
    private void init() {
        //TODO [by shayb]: check if we can remove this service as we will only need the job to run on HA with db/optimistic locking provider anyway and for that we already have a service
        if (LockingProviderTypeEnum.isDb() || LockingProviderTypeEnum.isOptimistic()) {
            TaskBase task = TaskUtils.createRepeatingTask(ReleaseExpiredLocksJob.class,
                    TimeUnit.MINUTES.toMillis(ConstantValues.dbLockCleanupJobIntervalSec.getLong()),
                    TimeUnit.MINUTES.toMillis(ConstantValues.dbLockCleanupJobStaleIntervalSec.getLong()));
            taskService.startTask(task, true);
        }
    }

    @JobCommand(singleton = true, runOnlyOnPrimary = false, description = "Release expired and orphan locks job",
            schedulerUser = TaskUser.SYSTEM, manualUser = TaskUser.SYSTEM)
    public static class ReleaseExpiredLocksJob extends QuartzCommand {
        private static final Logger log = LoggerFactory.getLogger(ReleaseExpiredLocksJob.class);

        @Override
        protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
            try {
                ArtifactoryContext context = ContextHelper.get();
                if (context == null) {
                    log.warn("Context is not bound.");
                    return;
                }
                releaseExpiredStaleLocks(context);
            } catch (Exception e) {
                log.error("Expired locks cleanup job could not be completed. {}", e.getMessage());
                log.debug("Expired locks cleanup job could not be completed.", e);
            }
        }

        private void releaseExpiredStaleLocks(ArtifactoryContext context) {
            ArtifactoryServersCommonService serversService = context.beanForType(ArtifactoryServersCommonService.class);
            DbLocksService dbLocksService = context.beanForType(DbLocksService.class);
            AddonsManager addonsManager = context.beanForType(AddonsManager.class);
            HaAddon haAddon = addonsManager.addonByType(HaAddon.class);
            if (haAddon.isHaEnabled() && haAddon.isPrimary() || serversService.getActiveRunningHaPrimary() == null) {
                try {
                    cleanOrphanLocks(serversService, dbLocksService);
                } catch (Exception e) {
                    log.error("Failed cleaning orphan locks. {}", e.getMessage());
                    log.debug("Failed cleaning orphan locks. ", e);
                }
                try {
                    dbLocksService.cleanDbExpiredLocks();
                } catch (Exception e) {
                    log.warn("Failed cleaning expired locks. {}", e.getMessage());
                    log.debug("Failed cleaning expired locks.", e);
                }
            }
            try {
                dbLocksService.cleanCachedExpiredLocks();
            } catch (Exception e) {
                log.warn("Failed cleaning cached orphan locks. {}", e.getMessage());
                log.debug("Failed cleaning cached  orphan locks.", e);
            }
        }

        private void cleanOrphanLocks(ArtifactoryServersCommonService serversService, DbLocksService dbLocksService) {
            List<String> otherActiveServers = serversService.getAllArtifactoryServersInfo().stream()
                    .filter(this::isActiveWithGraceTime)
                    .map(ArtifactoryServerInfo::getServerId)
                    .collect(Collectors.toList());
            dbLocksService.cleanOrphanLocks(otherActiveServers);
        }

        private boolean isActiveWithGraceTime(ArtifactoryServerInfo server) {
            return server.isActive(haHeartbeatStaleIntervalSecs.getInt() * 3);
        }
    }
}
