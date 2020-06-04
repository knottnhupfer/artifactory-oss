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

import ch.qos.logback.classic.Level;
import org.artifactory.api.repo.WorkItem;
import org.artifactory.test.TestUtils;
import org.springframework.util.ReflectionUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.mockito.Mockito.*;

/**
 * @author gidis
 */
@Test
public class NonBlockingOnWriteQueueTest extends WorkQueueTestBase {

    @BeforeClass
    public void setup() {
        TestUtils.setLoggingLevel("org.artifactory.work.queue", Level.WARN);
    }

    @Test
    public void addConcurrentAddRemoveTest() throws InterruptedException {
        Double maxWorkItemNumber = 100d;
        Integer numberOfProducerThreads = 28;
        Integer numberOfInputs = 10000;
        Integer numberOfConsumerThreads = 4;
        ExecutorService producerExecutor = Executors.newFixedThreadPool(numberOfProducerThreads);
        NonBlockingOnWriteQueue<IntegerWorkItem> queue = new NonBlockingOnWriteQueue<>(QUEUE_NAME);
        // Create producer threads that will fill the queue
        for (int i = 0; i < numberOfProducerThreads; i++) {
            producerExecutor.submit(() -> {
                for (int j = 0; j < numberOfInputs; j++) {
                    IntegerWorkItem workItem = new IntegerWorkItem((int) (Math.random() * maxWorkItemNumber));
                    queue.addToPending(workItem, null);
                }
            });
        }
        // Create Consumer threads that will execute the jobs
        AtomicInteger counter = new AtomicInteger();
        ExecutorService consumerExecutor = Executors.newFixedThreadPool(numberOfConsumerThreads);
        for (int i = 0; i < numberOfConsumerThreads; i++) {
            consumerExecutor.submit(() -> {
                while (!producerExecutor.isTerminated() || queue.getQueueSize() > 0 || queue.getRunningSize() > 0) {
                    WorkQueuePromotedItem<IntegerWorkItem> promotion = queue.promote();
                    if (promotion != null) {
                        queue.remove(promotion);
                        counter.getAndIncrement();
                    }
                }
            });
        }

        producerExecutor.shutdown();
        producerExecutor.awaitTermination(2, MINUTES);
        consumerExecutor.shutdown();
        consumerExecutor.awaitTermination(2, MINUTES);
        Assert.assertEquals(queue.getRunningSize(), 0);
        Assert.assertEquals(queue.getQueueSize(), 0);
        Assert.assertTrue(counter.get() >= 100);
        Assert.assertTrue(counter.get() <= 1000);
        System.out.println("Number of successful promotions (removals): " + counter.get());
    }

    public void testIdenticalWorkItemsIsEmpty(){
        Assert.assertTrue(new DummyWorkItem("Im dummy").getIdenticalWorkItems().isEmpty());
    }

    public void testIdenticalWorkItems() throws NoSuchMethodException {
        NonBlockingOnWriteQueue<DummyWorkItem> queueSpy = spy(new NonBlockingOnWriteQueue<>(QUEUE_NAME));

        Method testMethod = NonBlockingOnWriteQueueTest.class.getMethod("testIdenticalWorkItems");
        queueSpy.addToPending(new DummyWorkItem("Im dummy"), testMethod);

        WorkItemWrapper workItemWrapperMock = mock(WorkItemWrapper.class);
        when(queueSpy.acquireLockOnKey()).thenReturn(workItemWrapperMock);
        when(workItemWrapperMock.getWorkItem()).thenReturn(new DummyWorkItem("Im dummy"));
        when(workItemWrapperMock.getMethod()).thenReturn(testMethod);

        WorkQueuePromotedItem<DummyWorkItem> promotedItem = queueSpy.promote();
        List<WorkItem> identicalWorkItems = promotedItem.workItem.getIdenticalWorkItems();
        Assert.assertEquals(identicalWorkItems.size(), 1);
        Assert.assertEquals(identicalWorkItems.get(0).getUniqueKey(), promotedItem.workItem.getUniqueKey());
    }

    public void testNotIdenticalWorkItems() {
        NonBlockingOnWriteQueue<DummyWorkItem> queueSpy = spy(new NonBlockingOnWriteQueue<>(QUEUE_NAME));

        Method testMethod = ReflectionUtils.findMethod(NonBlockingOnWriteQueueTest.class, "testIdenticalWorkItems");
        queueSpy.addToPending(new DummyWorkItem("Im not dummy"), testMethod);

        WorkItemWrapper workItemWrapperMock = mock(WorkItemWrapper.class);
        when(queueSpy.acquireLockOnKey()).thenReturn(workItemWrapperMock);
        when(workItemWrapperMock.getWorkItem()).thenReturn(new DummyWorkItem("Im dummy"));
        when(workItemWrapperMock.getMethod()).thenReturn(testMethod);

        WorkQueuePromotedItem<DummyWorkItem> promotedItem = queueSpy.promote();
        List<WorkItem> identicalWorkItems = promotedItem.workItem.getIdenticalWorkItems();
        Assert.assertNotNull(identicalWorkItems);
        Assert.assertEquals(identicalWorkItems.size(), 0);
    }
}

