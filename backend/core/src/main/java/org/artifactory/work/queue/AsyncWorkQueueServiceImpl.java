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

package org.artifactory.work.queue;

import com.google.common.collect.ImmutableMap;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.AsyncWorkQueueProviderService;
import org.artifactory.api.repo.WorkItem;
import org.artifactory.api.repo.WorkQueue;
import org.artifactory.common.ConstantValues;
import org.artifactory.mbean.MBeanRegistrationService;
import org.artifactory.schedule.*;
import org.artifactory.schedule.quartz.QuartzCommand;
import org.artifactory.spring.ContextCreationListener;
import org.artifactory.util.CollectionUtils;
import org.artifactory.work.queue.mbean.WorkQueueMBean;
import org.artifactory.work.queue.mbean.buildEvent.BuildRetentionWorkQueue;
import org.artifactory.work.queue.mbean.buildinfo.BuildInfoMigrationCalculationJobWorkQueue;
import org.artifactory.work.queue.mbean.checksum.ChecksumCalculationJobWorkQueue;
import org.artifactory.work.queue.mbean.chef.ChefMetadataWorkQueue;
import org.artifactory.work.queue.mbean.composer.ComposerExtractorWorkQueue;
import org.artifactory.work.queue.mbean.composer.ComposerIndexerWorkQueue;
import org.artifactory.work.queue.mbean.conan.ConanV2MigrationCalculationJobWorkQueue;
import org.artifactory.work.queue.mbean.conan.ConanWorkQueue;
import org.artifactory.work.queue.mbean.conda.CondaWorkQueue;
import org.artifactory.work.queue.mbean.cran.CranWorkQueue;
import org.artifactory.work.queue.mbean.debian.DebianCacheCoordinatesWorkQueue;
import org.artifactory.work.queue.mbean.debian.DebianVirtualWorkQueue;
import org.artifactory.work.queue.mbean.debian.DebianWorkQueue;
import org.artifactory.work.queue.mbean.ha.HaMessageWorkQueue;
import org.artifactory.work.queue.mbean.helm.HelmVirtualWorkQueue;
import org.artifactory.work.queue.mbean.helm.HelmWorkQueue;
import org.artifactory.work.queue.mbean.maven.MavenMetadataWorkQueue;
import org.artifactory.work.queue.mbean.maven.MavenPluginMetadataWorkQueue;
import org.artifactory.work.queue.mbean.npm.conda.NpmReindexWorkQueue;
import org.artifactory.work.queue.mbean.npm.conda.NpmWorkQueue;
import org.artifactory.work.queue.mbean.puppet.PuppetRepoWorkQueue;
import org.artifactory.work.queue.mbean.puppet.PuppetWorkQueue;
import org.artifactory.work.queue.mbean.replication.ReplicationEventQueue;
import org.artifactory.work.queue.mbean.repo.path.checksum.RepoPathChecksumCalculationJobWorkQueue;
import org.artifactory.work.queue.mbean.yum.RpmWorkQueue;
import org.artifactory.work.queue.mbean.yum.YumVirtualWorkQueue;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author gidis
 */
@Service
public class AsyncWorkQueueServiceImpl implements AsyncWorkQueueProviderService, ContextCreationListener {
    private static final Logger log = LoggerFactory.getLogger(AsyncWorkQueueServiceImpl.class);
    private final Map<String, WorkQueue<WorkItem>> workQueueMap = new ConcurrentHashMap<>();
    public static final String JOB_QUEUE_PARAM = "queueService";

    @Autowired
    private TaskService taskService;

    @Override
    public void onContextCreated() {
        if (ConstantValues.workQueueJobEnabled.getBoolean()) {
            TaskBase workQueueDoJobTask = TaskUtils.createRepeatingTask(WorkQueueJob.class,
                    TimeUnit.SECONDS.toMillis(ConstantValues.workQueueDoJobIntervalSecs.getLong()),
                    TimeUnit.SECONDS.toMillis(ConstantValues.workQueueDoJobIntervalSecs.getLong()));
            workQueueDoJobTask.addAttribute(JOB_QUEUE_PARAM, this);
            taskService.startTask(workQueueDoJobTask, false);
        }
    }

    @PreDestroy
    public void destroy() {
        taskService.cancelTasks(WorkQueueJob.class, true);
    }

    @Override
    public void closeAllQueues() {
        for (Map.Entry<String, WorkQueue<WorkItem>> queueEntry : workQueueMap.entrySet()) {
            WorkQueue<WorkItem> workQueue = queueEntry.getValue();
            try {
                workQueue.stopQueue();
            } catch (Exception e) {
                log.error("Failed to close queue " + queueEntry.getKey(), e);
            }
        }
    }

    @Override
    public int getEstimatedPendingTasksSize(String workQueueCallbackName) {
        WorkQueue<WorkItem> workQueue = workQueueMap.get(workQueueCallbackName);
        if (workQueue != null) {
            return workQueue.getQueueSize();
        }
        throw new RuntimeException("No work queue matches callback '" + workQueueCallbackName + "'");
    }

    @Override
    public WorkQueue<WorkItem> getWorkQueue(Method workQueueCallback, Object target) {
        String name = workQueueCallback.getName();
        WorkQueue<WorkItem> workQueue = workQueueMap.get(name);
        if (workQueue == null) {
            synchronized (workQueueMap) {
                workQueue = workQueueMap.get(name);
                if (workQueue == null) {
                    try {
                        WorkQueueInfo info = getWorkerInfo(workQueueCallback);
                        // if queue already exists for another method (under different key in the 'workQueueMap' map),
                        // return the existing queue and add it to the map for the current method name as well
                        WorkQueue<WorkItem> existingQueue = searchQueueUnderDifferentKey(info);
                        if (existingQueue != null) {
                            workQueue = existingQueue;
                        } else {
                            // the workqueue is limited to work per class. I think that we shoulc pass the target class like we do with the method as a param to the workqueue and not cache it in the workqueue object attributes
                            workQueue = new WorkQueueImpl<>(info.getDisplayName(), info.getMaxNumberOfThreads(), target);
                            WorkQueueMBean mBean = createMBean(workQueue, info);
                            ContextHelper.get().beanForType(MBeanRegistrationService.class).register(mBean, "Work Queue", info.displayName);
                        }
                        workQueueMap.put(name, workQueue);
                    } catch (Exception e) {
                        log.error("Failed to initialize work queue {}", name, e);
                    }
                }

            }
        }
        return workQueue;
    }

    /**
     * Search for queue with the same name as the info object under any kind of key in the workQueueMap map.
     *
     * @return WorkQueue if found, null otherwise
     */
    private WorkQueue<WorkItem> searchQueueUnderDifferentKey(WorkQueueInfo info) {
        Collection<WorkQueue<WorkItem>> queues = workQueueMap.values();
        if (CollectionUtils.isNullOrEmpty(queues)) {
            return null;
        }
        return queues.stream()
                .filter(queue -> queue.getName().equals(info.getDisplayName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Map<String, WorkQueue<WorkItem>> getExistingWorkQueues() {
        return workQueueMap;
    }

    private WorkQueueMBean createMBean(WorkQueue<WorkItem> workQueue, WorkQueueInfo info)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        try {
            Constructor constructor = info.getmBeanClass().getConstructor(WorkQueueMBean.class);
            return (WorkQueueMBean) constructor.newInstance(workQueue);
        } catch (Exception e) {
            log.error("Failed to create mbean for work queue {}", workQueue.getName());
            throw e;
        }
    }

    private WorkQueueInfo getWorkerInfo(Method workQueueCallback) {
        String name = workQueueCallback.getName();
        String className = workQueueCallback.getDeclaringClass().getName();
        switch (name) {
            case "calculateMavenMetadataAsync": {
                int maxNumberOfThreads = ConstantValues.mvnMetadataCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Maven Metadata", MavenMetadataWorkQueue.class);
            }
            case "calculateMavenPluginsMetadataAsync": {
                int maxNumberOfThreads = ConstantValues.mvnMetadataPluginCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Plugin Maven Metadata", MavenPluginMetadataWorkQueue.class);
            }
            case "calculateYumVirtualMetadataAsync":
            case "calculateYumVirtualMetadata": {
                int maxNumberOfThreads = ConstantValues.yumVirtualMetadataCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Yum Virtual Metadata", YumVirtualWorkQueue.class);
            }
            case "calculateRpmMetadataAsync":
            case "calculateRpmMetadataSync": {
                int maxNumberOfThreads = ConstantValues.rpmMetadataCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Rpm Metadata", RpmWorkQueue.class);
            }
            case "calculateHelmMetadataAsync":
            case "calculateHelmMetadataSync": {
                int maxNumberOfThreads = ConstantValues.helmMetadataCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Helm Metadata", HelmWorkQueue.class);
            }
            case "calculateHelmVirtualMetadataSync": {
                int maxNumberOfThreads = ConstantValues.helmVirtualMetadataCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Helm Virtual Metadata", HelmVirtualWorkQueue.class);
            }
            case "calculateConanMetadataAsync": {
                int maxNumberOfThreads = ConstantValues.conanMetadataCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Conan Metadata", ConanWorkQueue.class);
            }
            case "calculateCranMetadataSync":
            case "calculateCranMetadataAsync": {
                int maxNumberOfThreads = ConstantValues.cranMetadataCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "CRAN Metadata", CranWorkQueue.class);
            }
            case "calculateCranVirtualMetadataSync": {
                int maxNumberOfThreads = ConstantValues.cranMetadataCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "CRAN virtual Metadata", CranWorkQueue.class);
            }
            case "calculateCondaMetadataSync":
            case "calculateCondaMetadataAsync": {
                int maxNumberOfThreads = ConstantValues.condaMetadataCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Conda Metadata", CondaWorkQueue.class);
            }
            case "calculateNpmMetadataAsync": {
                int maxNumberOfThreads = ConstantValues.npmMetadataCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Npm Metadata", NpmWorkQueue.class);
            }
            case "reindexNpmMetadataAsync": {
                int maxNumberOfThreads = ConstantValues.npmReindexMetadataCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Npm Reindex Metadata", NpmReindexWorkQueue.class);
            }
            case "handlePackageDeployment":
            case "handlePackageDeletion": {
                int maxNumberOfThreads = ConstantValues.composerMetadataExtractorWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Composer Metadata Extraction", ComposerExtractorWorkQueue.class);
            }
            case "extractAndIndexAllComposerPackages":
            case "indexComposerPackageAndRepo": {
                int maxNumberOfThreads = ConstantValues.composerMetadataIndexWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Composer Metadata", ComposerIndexerWorkQueue.class);
            }
            case "calculateCachedDebianCoordinates": {
                int maxNumberOfThreads = ConstantValues.debianCoordinatesCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Debian Coordinates Metadata", DebianCacheCoordinatesWorkQueue.class);
            }
            case "calculateDebianMetadataInternalAsync":
            case "calculateDebianMetadataInternalSync": {
                int maxNumberOfThreads = ConstantValues.debianMetadataCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Debian Metadata", DebianWorkQueue.class);
            }
            case "calculateDebianVirtualMetadataAsync":
            case "calculateDebianVirtualMetadataSync": {
                int maxNumberOfThreads = ConstantValues.debianVirtualMetadataCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Debian Virtual Metadata", DebianVirtualWorkQueue.class);
            }
            case "calculatePuppetMetadataAsync": {
                int maxNumberOfThreads = ConstantValues.puppetMetadataCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Puppet Metadata", PuppetWorkQueue.class);
            }
            case "calculatePuppetRepoMetadataAsync": {
                int maxNumberOfThreads = ConstantValues.puppetRepoMetadataCalculationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Puppet Repository Metadata", PuppetRepoWorkQueue.class);
            }
            case "extractAndIndexChefCookbooks":
            case "extractAndIndexSingleChefCookbook":
            case "calculateVirtualRepoMetadata": {
                int maxNumberOfThreads = ConstantValues.chefMetadataIndexWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Chef Metadata", ChefMetadataWorkQueue.class);
            }
            case "offerRemoteReplicationEventInternal":
            case "putInboundReplicationEventsInternal": {
                int maxNumberOfThreads = ConstantValues.eventBasedReplicationWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Event Based Replication", ReplicationEventQueue.class);
            }
            case "deleteBuildAsync": {
                int maxNumberOfThreads = ConstantValues.buildRetentionWorkers.getInt();
                return new WorkQueueInfo(maxNumberOfThreads, "Build Retention Job", BuildRetentionWorkQueue.class);
            }
            case "updateSha2": {
                return new WorkQueueInfo(ConstantValues.sha2MigrationJobQueueWorkers.getInt(), "SHA256 Migration", ChecksumCalculationJobWorkQueue.class);
            }
            case "migrateBuildInfo": {
                return new WorkQueueInfo(ConstantValues.buildInfoMigrationJobQueueWorkers.getInt(), "Build Info Migration", BuildInfoMigrationCalculationJobWorkQueue.class);
            }
            case "migrateConanToV2": {
                return new WorkQueueInfo(ConstantValues.conanV2MigrationJobQueueWorkers.getInt(), "Conan V2 Migration", ConanV2MigrationCalculationJobWorkQueue.class);
            }
            case "updateRepoPathChecksum": {
                return new WorkQueueInfo(ConstantValues.pathChecksumMigrationJobQueueWorkers.getInt(), "Path Checksum Migration", RepoPathChecksumCalculationJobWorkQueue.class);
            }
            case "notifyAsync": {
                return new WorkQueueInfo(ConstantValues.haMessagesWorkers.getInt(), "HA event messages", HaMessageWorkQueue.class);
            }
            default: {
                throw new RuntimeException(
                        "Unsupported work queue: the work queue: class" + className + " method: " + name +
                                " is not supported by this service");
            }
        }
    }

    // Job that scan work queues that has pending "stuck" tasks with no thread to execute them, will trigger thread to execute the "stuck" task
    @JobCommand(description = "Invoke work queue tasks", singleton = true,
            runOnlyOnPrimary = false, schedulerUser = TaskUser.SYSTEM, manualUser = TaskUser.SYSTEM)
    public static class WorkQueueJob extends QuartzCommand {

        @Override
        protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
            try {
                Map<String, WorkQueue<WorkItem>> queues = getQueues(callbackContext);
                log.trace("Found map with {} queues", queues.size());
                invokeIfNeeded(queues);
            } catch (Exception e) {
                log.error("An error occurred while running {} job. {}", this.getClass().getName(), e.getMessage());
                log.debug("An error occurred while running job.", e);
            }
        }

        private Map<String, WorkQueue<WorkItem>> getQueues(JobExecutionContext callbackContext) {
            Map<String, WorkQueue<WorkItem>> queues = null;
            log.debug("Searching for job details");
            JobDetail jobDetail = callbackContext.getJobDetail();
            if (jobDetail != null) {
                JobDataMap jobDataMap = jobDetail.getJobDataMap();
                if (jobDataMap != null) {
                    log.debug("Resolving queues from the queue service");
                    AsyncWorkQueueProviderService queueService = (AsyncWorkQueueProviderService) jobDataMap
                            .get(JOB_QUEUE_PARAM);
                    queues = queueService.getExistingWorkQueues();
                }
            }
            return queues != null ? queues : ImmutableMap.of();
        }

        private void invokeIfNeeded(Map<String, WorkQueue<WorkItem>> queues) {
            if (queues != null && !queues.isEmpty()) {
                queues.values().forEach(queue -> {
                    //queue.availablePermits should never be bigher than getMaxNumberOfWorkers, unless if there is a bug that we do release() more than we need. this is why >= and not ==
                    if (queue.getQueueSize() > 0 && queue.availablePermits() >= queue.getMaxNumberOfWorkers()) {
                        //It has been decided to log on info. Re-consider in the future to convert to debug log
                        log.info("Invoking tasks on {}", queue.getName());
                        CachedThreadPoolTaskExecutor executor = ContextHelper.get().beanForType(CachedThreadPoolTaskExecutor.class);
                        executor.execute(queue::doJobs);
                    }
                });
            }
        }
    }

    private class WorkQueueInfo {
        private int maxNumberOfThreads;
        private String displayName;
        private Class mBeanClass;

        public WorkQueueInfo(int maxNumberOfThreads, String displayName, Class mBeanClass) {
            this.maxNumberOfThreads = maxNumberOfThreads;
            this.displayName = displayName;
            this.mBeanClass = mBeanClass;
        }

        public int getMaxNumberOfThreads() {
            return maxNumberOfThreads;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Class getmBeanClass() {
            return mBeanClass;
        }
    }
}
