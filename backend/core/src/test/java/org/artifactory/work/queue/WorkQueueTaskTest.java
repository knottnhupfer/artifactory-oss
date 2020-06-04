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
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.repo.AsyncWorkQueueProviderService;
import org.artifactory.api.repo.WorkItem;
import org.artifactory.schedule.*;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.storage.fs.lock.provider.JvmConflictsGuard;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.test.mock.MockUtils;
import org.quartz.Scheduler;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.artifactory.work.queue.AsyncWorkQueueServiceImpl.JOB_QUEUE_PARAM;
import static org.easymock.EasyMock.*;

/**
 * RTFACT-17035 - Items might stuck in the workqueue for a long time until execution
 *
 * @author Shay Bagants
 */
@Test
public class WorkQueueTaskTest extends ArtifactoryHomeBoundTest {
    private final String QUEUE_NAME = "queueName";
    private TaskServiceImpl taskService = new TaskServiceImpl();
    private CachedThreadPoolTaskExecutor executorPool;
    private InternalArtifactoryContext context;
    private WorkQueueImpl<WorkItem> queue;
    private AsyncWorkQueueProviderService queueService;

    /**
     * 1. Insert workItem to a workQueue without calling {@link WorkQueueImpl#doJobs()}
     * 2. Ensure item stuck in the queue
     * 3. trigger task {@link org.artifactory.work.queue.AsyncWorkQueueServiceImpl.WorkQueueJob} to invoke queue tasks
     * 4. Ensure task is no longer in the queue
     */
    public void testItemStuckInQueue() throws Exception {
        mockContextAndBeans();
        // put WorkItem in queue without sending thread to execute it
        Method method = SleepingDummyService.class.getMethod("serve", DummyWorkItem.class);
        DummyWorkItem workItem = new DummyWorkItem("x");
        queue.offerWork(workItem, method);
        Assert.assertEquals(queue.getQueueSize(), 1);

        // trigger job and ensure task is no longer stuck in a queue
        triggerWorkQueueTask();
        waitInConditionUntilTaskProcessed(workItem);
        Assert.assertEquals(queue.getQueueSize(), 0, "Job didn't invoke the task");
    }

    private void mockContextAndBeans() throws Exception {
        // mocking context
        context = MockUtils.getThreadBoundedMockContext();
        expect(context.getArtifactoryHome()).andReturn(homeStub).anyTimes();
        expect(context.isOffline()).andReturn(false).anyTimes();
        expect(context.getTaskService()).andReturn(taskService).anyTimes();
        executorPool = new CachedThreadPoolTaskExecutor();
        expect(context.beanForType(CachedThreadPoolTaskExecutor.class)).andReturn(executorPool);

        // mock addon and beans
        AddonsManager addonsManager = createMock(AddonsManager.class);
        expect(context.beanForType(AddonsManager.class)).andReturn(addonsManager).anyTimes();
        HaAddon haAddon = createMock(HaAddon.class);
        JvmConflictsGuard lockingMapFactory = new JvmConflictsGuard(120);
        expect(haAddon.getConflictsGuard(QUEUE_NAME)).andReturn(lockingMapFactory).anyTimes();
        expect(haAddon.isHaEnabled()).andReturn(false).anyTimes();
        expect(addonsManager.addonByType(HaAddon.class)).andReturn(haAddon).anyTimes();
        expect(addonsManager.addonByType(HaCommonAddon.class)).andReturn(haAddon).anyTimes();

        ArtifactorySchedulerFactoryBean schedulerFactory = new ArtifactorySchedulerFactoryBean();
        schedulerFactory.setTaskExecutor(executorPool);
        schedulerFactory.afterPropertiesSet();
        Scheduler scheduler = schedulerFactory.getObject();
        expect(context.beanForType(Scheduler.class)).andReturn(scheduler).anyTimes();

        replay(context, addonsManager, haAddon);
        // creating queue and puting it on a mock AsyncWorkQueueProviderService
        SleepingDummyService dummyService = new SleepingDummyService();
        queue = new WorkQueueImpl<>(QUEUE_NAME, 5, dummyService);
        queueService = createMock(AsyncWorkQueueProviderService.class);
        expect(queueService.getExistingWorkQueues()).andReturn(ImmutableMap.of("Test Queue", queue)).anyTimes();

        schedulerFactory.setApplicationContext(context);
        schedulerFactory.start();

        replay(queueService);
    }

    private void triggerWorkQueueTask() {
        TaskBase workQueueDoJobTask = TaskUtils.createManualTask(AsyncWorkQueueServiceImpl.WorkQueueJob.class, 0);
        workQueueDoJobTask.addAttribute(JOB_QUEUE_PARAM, queueService);
        String taskToken = taskService.startTask(workQueueDoJobTask, true);
        taskService.waitForTaskCompletion(taskToken);
    }

    // conditional wait for 5 seconds
    //TODO [by shayb]: We need Condition (wait in condition) to be available for unit tests
    private void waitInConditionUntilTaskProcessed(DummyWorkItem workItem) throws InterruptedException {
        long startedTime = System.currentTimeMillis();
        while (!workItem.isProcessed() && (System.currentTimeMillis() - startedTime < 5000)) {
            Thread.sleep(10);
        }
    }

    @AfterMethod
    private void cleanup() {
        if (executorPool != null) {
            executorPool.destroy();
        }
    }
}
