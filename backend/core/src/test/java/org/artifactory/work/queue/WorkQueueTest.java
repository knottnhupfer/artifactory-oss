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

import com.google.common.collect.Lists;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Unit tests for the {@link WorkQueueImpl}.
 *
 * @author Yossi Shaul
 */
@Test
public class WorkQueueTest extends WorkQueueTestBase {

    /**
     * Test that if there is no worker then no job is being done
     */
    public void offerTheSameWorkWithZeroWorkers() throws InterruptedException, NoSuchMethodException {
        List<AtomicInteger> counts = Collections.nCopies(10, new AtomicInteger(0));
        Counter counter = new Counter(counts);
        Method method = counter.getClass().getMethod("count", IntegerWorkItem.class);
        WorkQueueImpl<IntegerWorkItem> wq = new WorkQueueImpl<>(QUEUE_NAME, 0, counter);
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                wq.offerWork(new IntegerWorkItem(i), method);
                executorService.submit(wq::doJobs);
            }
        }
        executorService.shutdown();
        executorService.awaitTermination(1500, TimeUnit.SECONDS);
        // We expect each job to be executed at least once
        counts.forEach(count -> {
            assertThat(count.get()).isEqualTo(0);
        });
    }

    public void offerTheSameWorkConcurrently() throws InterruptedException, NoSuchMethodException {
        List<AtomicInteger> counts = Collections.nCopies(10, new AtomicInteger(0));
        Counter counter = new Counter(counts);
        Method method = counter.getClass().getMethod("count", IntegerWorkItem.class);
        WorkQueueImpl<IntegerWorkItem> wq = new WorkQueueImpl<>("Test Queue", 1, counter);
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10000; j++) {
                wq.offerWork(new IntegerWorkItem(i), method);
                executorService.submit(wq::doJobs);
            }
        }

        executorService.shutdown();
        executorService.awaitTermination(1500, TimeUnit.SECONDS);
        // We expect each job to be executed at least once
        counts.forEach(count -> assertThat(count.get()).isGreaterThanOrEqualTo(1));
    }

    public void offerWorkWithNWorkers() throws InterruptedException, NoSuchMethodException {
        List<AtomicInteger> counts = Collections.nCopies(1000, new AtomicInteger(0));
        Counter counter = new Counter(counts);
        Method method = counter.getClass().getMethod("count", IntegerWorkItem.class);
        WorkQueueImpl<IntegerWorkItem> wq = new WorkQueueImpl<>("Test Queue", 4, counter);

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                wq.offerWork(new IntegerWorkItem(i), method);
                executorService.submit(wq::doJobs);
            }
        }

        executorService.shutdown();
        executorService.awaitTermination(1500, TimeUnit.SECONDS);
        // We expect each job to be executed at least once
        counts.forEach(count -> assertThat(count.get()).isGreaterThanOrEqualTo(1));
    }

    // When waiting for item (blockUntilFinished = true), the item we are waiting for can either be pending, or promoted state.
    // This tests a bug that when the item is promoted, we never actually waited for it to complete
    public void waitUntilWhenItemIsPromotedTest() throws NoSuchMethodException, InterruptedException {
        SleepingDummyService dummyService = new SleepingDummyService();
        Method method = dummyService.getClass().getMethod("serve", DummyWorkItem.class);

        WorkQueueImpl<DummyWorkItem> queue = new WorkQueueImpl<>("Test Queue", 4, dummyService);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 20; i++) {
            DummyWorkItem workItem = new DummyWorkItem("x" + i);
            queue.offerWork(workItem, method);
            if (queue.availablePermits() != 0) {
                // there's a small chance we will miss event (especially if the work queue contain only single worker)
                executorService.submit(queue::doJobs);
            }
            System.out.println("Waiting for workItem number " + i);
            queue.waitForItemDone(workItem);
            Assert.assertTrue(workItem.isProcessed(), "Request num " + i + " failed");
        }
        executorService.shutdown();
        executorService.awaitTermination(1500, TimeUnit.SECONDS);
    }

    public void waitUntilTest() throws InterruptedException, ExecutionException, NoSuchMethodException {
        List<AtomicInteger> counts = Collections.nCopies(10, new AtomicInteger(0));
        Counter counter = new Counter(counts);
        Method method = counter.getClass().getMethod("count", IntegerWorkItem.class);
        WorkQueueImpl<IntegerWorkItem> wq = new WorkQueueImpl<>("Test Queue", 4, counter);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        ExecutorService testExecutor = Executors.newFixedThreadPool(10);
        ArrayList<Future> futures = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            futures.add(testExecutor.submit(() -> {
                for (int j = 0; j < 10000; j++) {
                    IntegerWorkItem workItem = new IntegerWorkItem((int) (Math.random() * 10d));
                    wq.offerWork(workItem, method);
                    if (wq.availablePermits() != 0) {
                        // there's a small chance we will miss event (especially if the work queue contain only single worker)
                        executorService.submit(wq::doJobs);
                    }
                    wq.waitForItemDone(workItem);
                }
                return null;
            }));
        }
        for (Future future : futures) {
            future.get();
        }
        testExecutor.shutdown();
        testExecutor.awaitTermination(1500, TimeUnit.SECONDS);
        executorService.shutdown();
        executorService.awaitTermination(1500, TimeUnit.SECONDS);
        // We expect each job to be executed at least once
        counts.forEach(count -> assertThat(count.get()).isGreaterThanOrEqualTo(1));
    }
}
