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

package org.artifactory.storage.jobs.migration;

import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.AsyncWorkQueueProviderService;
import org.artifactory.aql.AqlConverts;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.exception.CancelException;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.rest.resource.task.BackgroundTask;
import org.artifactory.rest.resource.task.BackgroundTasks;
import org.artifactory.schedule.quartz.QuartzCommand;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.service.ArtifactoryServersCommonService;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.version.converter.DBSqlConverter;
import org.artifactory.storage.spring.StorageContextHelper;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.jfrog.storage.DbType;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.artifactory.schedule.TaskBase.TaskState.CANCELED;
import static org.artifactory.schedule.TaskUtils.pauseOrBreak;

/**
 * Common logic for the v550 migration jobs (sha256), v660 migration job (build info), v690 migration job (Conan V2).
 *
 * @author Dan Feldman
 */
public abstract class MigrationJobBase extends QuartzCommand {
    private static final Logger log = LoggerFactory.getLogger(MigrationJobBase.class);

    //Configurable job params
    protected long sleepInterval = ConstantValues.migrationJobSleepIntervalMillis.getLong();
    protected int batchThreshold = ConstantValues.migrationJobBatchSize.getInt();
    protected int queryLimit = ConstantValues.migrationJobDbQueryLimit.getInt();
    protected int waitForClusterSleepInterval = ConstantValues.migrationJobWaitForClusterSleepIntervalMillis.getInt();
    private final ConcurrentMap<String, Boolean> submittedTasks = Maps.newConcurrentMap();

    protected long lastEntityId = 0;                  // Last entity id returned by the db query, used for offsetting the next query
    protected long endlessLoopPreventionEntryId = 0;  // Detects if the migration loop is running on the same id and cannot advance

    @Override
    protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
        initParams(callbackContext);
        try {
            if (jobRunsOnOtherNode()) {
                return;
            }
            if (!init()) {
                return;
            }
            waitForClusterUpgrade();
            runMigration();
        } catch (CancelException e) {
            log().info("Caught stop signal in main loop, {} is exiting.", jobName());
        } catch (EndlessLoopPreventionException elp) {
            log.error("", elp);
        } catch (Exception e) {
            log.debug("", e);
            log().error("Caught unexpected exception during " + jobName() + " job, operation will break.", e);
        }
    }

    private boolean jobRunsOnOtherNode() {
        return jobRunsOnOtherNode(this.getClass().getName());
    }

    protected void runMigration() {
        migrationLoop();
        additionalSteps();
        waitForSubmittedTasks();
        if (hasErrors()) {
            //we had errors for sure, time to stop.
            finishedWithErrors();
        } else if (stateOk()) { //run verification on counters before final db conversion
            markCompletion();
        } else {
            // There are still nulls in the db, retry the entire op another time.
            retry();
        }
    }

    protected final void migrationLoop() {
        // As long as calculation tasks are submitted there may be recoverable errors that require a retry
        boolean tasksWereSubmitted = true;
        while (tasksWereSubmitted) {
            tasksWereSubmitted = false;
            //Starting from the second loop - give a chance to run again on nodes that had errors we can recover from.
            //Unrecoverable nodes are filtered out of the query results when running the db query.
            lastEntityId = 0;
            endlessLoopPreventionEntryId = 0;
            while (lastEntityId > -1) { //-1 is set by the db query mechanism when no more nodes are returned
                waitForWorkQueue(1000);
                tasksWereSubmitted = migrationLogic();
                waitBetweenBatchesIfNeeded();
                logProgress();
                log().debug("Finished one loop, last returned entity id: {}", lastEntityId);
            }
            // Wait here for all tasks to at least be clear of the queue (maybe some are still working though) before
            // trying the db query a second (or more) time - lest the next db query will return stuff that's still in work.
            waitForWorkQueue(0);
        }
        waitForWorkQueue(0);
        logProgress();
    }

    /**
     * initialize job params from job context
     * @param callbackContext - the job context to get the params from
     */
    protected abstract void initParams(JobExecutionContext callbackContext);

    /**
     * @return the minimal {@link ArtifactoryVersion} required for the migration job to run.
     * The job will hold until all nodes in the cluster are of that version at least.
     */
    protected abstract ArtifactoryVersion getMinimalVersion();

    /**
     * @return true if the job is setup to run
     */
    protected abstract boolean jobEnabled();

    /**
     * The init phase makes sure all prerequisites for the job's execution hold and inits all required states.
     */
    protected abstract boolean init();

    /**
     * Actual logic the job needs to execute inside the {@link #migrationLoop}
     */
    protected abstract boolean migrationLogic();

    /**
     * Allow any additional logic the job may want to run
     */
    protected abstract void additionalSteps();

    /**
     * Lets the job specify if it had any errors
     */
    protected abstract boolean hasErrors();

    /**
     * Lets the job report errors, perform extra logic if needed
     */
    protected abstract void finishedWithErrors();

    /**
     * Lets the job determine if all went well.
     */
    protected abstract boolean stateOk();

    /**
     * Lets the job report success, perform extra logic if needed
     */
    protected abstract void markCompletion();

    /**
     * Lets the job execute retry logic.
     */
    protected abstract void retry();

    /**
     * Returns the job's log.
     */
    protected abstract Logger log();

    protected abstract void logProgress();

    protected abstract String jobName();

    /**
     * How many calculations already done in current batch
     */
    protected abstract AtomicInteger currentBatchCount();

    /**
     * Workqueue name that is attached to this job.
     */
    protected abstract String workQueueCallbackName();

    /**
     * Waits for {@link ConstantValues#migrationJobSleepIntervalMillis} if the current batch count has
     * exceeded the {@link ConstantValues#migrationJobBatchSize}.
     * The wait here is an approximation as all tasks that update the counter are async and it makes no sense to put
     * each of them to sleep, instead no new tasks will be submitted if the counter is pass the threshold
     */
    protected void waitBetweenBatchesIfNeeded() {
        if (currentBatchCount().get() >= batchThreshold) {
            pauseOrBreakIfNeeded();
            try {
                log().trace("Current batch reached sleep threshold, going to sleep for {} millis", sleepInterval);
                Thread.sleep(sleepInterval);
            } catch (Exception e) {
                log.trace("", e);
                log().trace("{} job thread interrupted while waiting between batches: {}, resuming...", jobName(), e.getMessage());
            }
            //reset batch counter.
            currentBatchCount().set(0);
            logProgress();
        }
    }

    /**
     * Checks if this job needs to pause or break, if pause was signaled this method blocks until released by the task service.
     * @throws CancelException if job was signaled to stop.
     */
    protected void pauseOrBreakIfNeeded() {
        boolean needToStop = false;
        String now = new Date().toString();
        try {
            log().trace("Testing if current task execution should pause, current time: {}", now);
            needToStop = pauseOrBreak();
            log().trace("Done Testing for pause state, current time: {}", now);
        } catch (Exception e) {
            log().debug("Caught exception trying to verify if current task should stop: ", e);
        }
        if (needToStop) {
            if (log().isWarnEnabled()) {
                log().warn("{} job received stop signal, aborting.", jobName());
            }
            throw new CancelException("STOP!", 777);
        }
    }

    /**
     * {@link #lastEntityId} is maintained to offset each db query so that we don't reiterate on already-failed nodes.
     */
    protected void markLastNodeId(List<FileInfo> missingSha2Nodes, Logger migrationLog) {
        lastEntityId = missingSha2Nodes.stream()
                .map(ItemInfo::getId)
                .max(Comparator.naturalOrder())
                .orElse(-1L);
        migrationLog.info("Marked last node id = {}", lastEntityId);
    }

    protected List<FileInfo> mapResultsToFileInfo(AqlEagerResult<AqlItem> aqlResults) {
        return aqlResults.getResults().stream()
                .filter(Objects::nonNull)
                .map(AqlConverts.toFileInfo)
                .collect(Collectors.toList());
    }

    /**
     * Polls on the job's work queue (approximately) until there are {@param targetSize} (or less) tasks in it.
     */
    private void waitForWorkQueue(int targetSize) {
        pauseOrBreakIfNeeded();
        int workQueuePendingSize = getWorkQueuePendingSize();
        while (workQueuePendingSize > targetSize) {
            pauseOrBreakIfNeeded();
            try {
                log().debug("There are still {} pending calculation tasks, waiting for {} millis until queue size is {}",
                        workQueuePendingSize, sleepInterval, targetSize);
                Thread.sleep(sleepInterval);
            } catch (Exception e) {
                log().debug("Interrupted while waiting for tasks to finish, resuming...", e);
            }
            workQueuePendingSize = getWorkQueuePendingSize();
        }
    }

    private int getWorkQueuePendingSize() {
        int size = 0;
        try {
            size = ContextHelper.get().beanForType(AsyncWorkQueueProviderService.class)
                    .getEstimatedPendingTasksSize(workQueueCallbackName());
        } catch (Exception e) {
            //work queue is initialized only when a task is first pushed, this might get called too soon on the first few times
            log().debug("Work queue for callback {} still not initialized, will try next time.", workQueueCallbackName());
        }
        return size;
    }

    /**
     * Queries all other nodes in the cluster (when run in HA env) and verifies all nodes are on the minimal
     * required version, execution waits here until condition is met.
     */
    protected void waitForClusterUpgrade() {
        String sleepMsg = "'{}' waiting for all other nodes in the cluster to upgrade to minimal required version {}. " +
                        "Sleep interval set to {} milliseconds";
        String taskToken = currentTaskToken();
        String minimalRequiredVersion = getMinimalVersion().getVersion();
        while (nonReadyNodesExist()) {
            log().info(sleepMsg, taskToken, minimalRequiredVersion, waitForClusterSleepInterval);
            try {
                log().trace("{}: going to sleep for {}ms", taskToken, waitForClusterSleepInterval);
                Thread.sleep(waitForClusterSleepInterval);
            } catch (InterruptedException e) {
                log().debug(taskToken + " interrupted while waiting for other nodes to upgrade, resuming...", e);
            }
        }
        log().info("{}: all nodes reached minimal version '{}', continuing execution", taskToken, minimalRequiredVersion);
    }

    private boolean nonReadyNodesExist() {
        ArtifactoryServersCommonService serversService = serversService();
        return serversService != null && serversService.getOtherActiveMembers()
                .stream()
                .map(artifactoryServer -> ArtifactoryVersionProvider
                        .get(artifactoryServer.getArtifactoryVersion(), artifactoryServer.getArtifactoryRevision()))
                .anyMatch(version -> version == null || version.before(getMinimalVersion()));
    }

    protected void executeDbConversion(String conversionName) {
        JdbcHelper jdbcHelper = jdbcHelper();
        DbType dbType = ContextHelper.get().beanForType(ArtifactoryDbProperties.class).getDbType();
        new DBSqlConverter(conversionName).convert(jdbcHelper, dbType);
    }

    /**
     * Fake reload in order to trigger the state verifier at the other active nodes (propagate the state change)
     */
    protected void triggerConfigReload() {
        MutableCentralConfigDescriptor configDescriptor = centralConfigService().getMutableDescriptor();
        centralConfigService().saveEditedDescriptorAndReload(configDescriptor);
    }

    /**
     * In HA mode, system prop with force node id may have been set on multiple nodes by mistake - verify it runs only on one.
     *
     * @param jobClassName The job name to look for in other nodes
     */
    public static boolean jobRunsOnOtherNode(String jobClassName) {
        if (haAddon().isHaEnabled()) {
            String otherNodeWithThisJobRunning = getOtherNodeWithThisJobRunning(jobClassName);
            if (isNotBlank(otherNodeWithThisJobRunning)) {
                String warnMsg = "{} job was found active on another node in the cluster (ID: '{}'), although it was " +
                        "setup to run on this node ('{}') as well. it will be aborted on this node.";
                log.warn(warnMsg, jobClassName, otherNodeWithThisJobRunning, ContextHelper.get().getServerId());
                return true;
            }
        }
        return false;
    }

    /**
     * @return the first node id found running this job, if any. null otherwise.
     */
    @Nullable
    private static String getOtherNodeWithThisJobRunning(String jobClassName) {
        ArtifactoryServersCommonService serversService = serversService();
        if (serversService == null) {
            log.warn("Context not ready yet, can't check for other nodes running this task '{}'", jobClassName);
            return null;
        }
        List<ArtifactoryServer> otherRunningHaMembers = serversService.getOtherRunningHaMembers();
        if (CollectionUtils.isEmpty(otherRunningHaMembers)) {
            //Don't go through all of the hassle in HaAddon if there are no members, it does quite a lot before verifying this
            return null;
        }
        return haAddon().propagateTasksList(otherRunningHaMembers, BackgroundTasks.class)
                .stream()
                .filter(Objects::nonNull)
                .map(BackgroundTasks::getTasks)
                .flatMap(Collection::stream)
                .filter(MigrationJobBase::taskIsActive)
                .filter(task -> jobClassName.equalsIgnoreCase(task.getType()))
                .map(BackgroundTask::getNodeId)
                .findAny().orElse(null);
    }

    private static boolean taskIsActive(BackgroundTask task) {
        return !CANCELED.name().equalsIgnoreCase(task.getState());
    }

    protected static HaAddon haAddon() {
        return ContextHelper.get().beanForType(AddonsManager.class).addonByType(HaAddon.class);
    }

    protected AqlService aqlService() {
        return ContextHelper.get().beanForType(AqlService.class);
    }

    protected NodesDao nodesDao() {
        return ContextHelper.get().beanForType(NodesDao.class);
    }

    protected InternalDbService dbService() {
        return StorageContextHelper.get().beanForType(InternalDbService.class);
    }

    protected InternalRepositoryService repoService() {
        return ContextHelper.get().beanForType(InternalRepositoryService.class);
    }

    protected void waitForSubmittedTasks() {
        while (!submittedTasks.isEmpty()) {
            try {
                if (log.isTraceEnabled()) {
                    submittedTasks.keySet().forEach(job -> log.trace("Waiting for {}", job));
                }
                pauseOrBreakIfNeeded();
                Thread.sleep(sleepInterval);
            } catch (InterruptedException e) {
                log.trace("", e);
                log().trace("{} job thread interrupted while waiting between batches: {}, resuming...", jobName(),
                        e.getMessage());
            }
        }
    }

    public void markTaskAsFinished(String taskId) {
        submittedTasks.remove(taskId);
    }

    protected void markTaskAsSubmitted(String taskId) {
        submittedTasks.putIfAbsent(taskId, true);
    }

    private static ArtifactoryServersCommonService serversService() {
        ArtifactoryContext context = ContextHelper.get();
        return context != null ? context.beanForType(ArtifactoryServersCommonService.class) : null;
    }

    private CentralConfigService centralConfigService() {
        return ContextHelper.get().beanForType(CentralConfigService.class);
    }

    private JdbcHelper jdbcHelper() {
        return StorageContextHelper.get().beanForType(JdbcHelper.class);
    }
}
