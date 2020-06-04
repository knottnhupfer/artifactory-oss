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

import org.artifactory.api.repo.WorkItem;
import org.artifactory.api.repo.WorkQueue;
import org.artifactory.common.ConstantValues;
import org.artifactory.work.queue.mbean.WorkQueueMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Semaphore;

/**
 * A work queue that eliminates jobs based on a passed criteria.<p>
 * The thread offering work for this queue might start working on the queue, and may work on all the queued items.
 *
 * @author Yossi Shaul
 */
public class WorkQueueImpl<T extends WorkItem> implements WorkQueue<T>, WorkQueueMBean {
    private static final Logger log = LoggerFactory.getLogger(WorkQueueImpl.class);

    private final NonBlockingOnWriteQueue<T> queue;
    private final String name;
    private final int workers;
    private final Semaphore semaphore;
    private final Object target;

    /**
     * Creates a new work queue with the given max workers.<p>
     * If the max workers is greater than one, the provider work executor must be thread safe.
     *
     * @param name         Symbolic name for the work queue
     * @param workers      Maximum workers allowed to work on this queue
     * @param target       The target object to execute the method invocation
     */
    public WorkQueueImpl(String name, int workers, Object target) {
        this.name = name;
        this.workers = workers;
        this.target = target;
        this.semaphore = new Semaphore(workers);
        this.queue = new NonBlockingOnWriteQueue<>(name);
    }

    @Override
    public boolean offerWork(T workItem, Method method) {
        log.trace("adding {}: to '{}'", workItem, name);
        boolean added = queue.addToPending(workItem, method);
        if (!added) {
            log.trace("{}: already contains '{}'", name, workItem);
        } else {
            log.trace("{}: successfully added to '{}'", workItem, name);
        }
        return added;
    }

    @Override
    public int availablePermits() {
        return semaphore.availablePermits();
    }

    @Override
    public void doJobs() {
        // Limit number of workers by semaphore
        if (!semaphore.tryAcquire()) {
            log.debug("{}: max workers already processing ({})", name, workers);
            return;
        }
        try {
            debugStep("start processing");
            WorkQueuePromotedItem<T> promotedWorkItem;
            while ((promotedWorkItem = queue.promote()) != null) {
                T workItem = promotedWorkItem.workItem;
                traceStep("started working", workItem);
                try {
                    invoke(promotedWorkItem, workItem);
                    traceStep("finished working", workItem);
                } catch (Exception e) {
                    log.error("{}: failed to process {}", name, workItem, e);
                } finally {
                    boolean removed = queue.remove(promotedWorkItem);
                    if (removed) {
                        traceStep("successfully removed", workItem);
                    } else {
                        log.error("unexpected state: failed to remove {} from {}. Queue size pending={} running={}",
                                workItem, name, queue.getQueueSize(), queue.getRunningSize());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error occurred while promoting queue items. {}", e.getMessage());
            log.debug("Error occurred while promoting queue items.", e);
            throw new RuntimeException(e);
        }
        finally {
            log.trace("Releasing semaphore");
            semaphore.release();
        }
    }

    private void invoke(WorkQueuePromotedItem<T> promotedWorkItem, T workItem) {
        try {
            promotedWorkItem.method.invoke(target, workItem);
        } catch (Exception e) {
            String origMsg = e.getMessage();
            if (e instanceof InvocationTargetException && e.getCause() != null) {
                origMsg = e.getCause().getMessage();
            }
            String msg = "Failed to call work queue '" + name + "' callback due to :" + origMsg;
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    private void debugStep(String stepName) {
        // queue size is expensive, and returns current running tasks from other nodes as well in HA.
        if (log.isDebugEnabled()) {
            log.debug("{}: {}. Queue size pending={} running={}", name, stepName, queue.getQueueSize(),
                    queue.getRunningSize());
        }
    }

    private void traceStep(String stepName, T workItem) {
        // queue size is expensive, and returns current running tasks from other nodes as well in HA.
        if (log.isTraceEnabled()) {
            log.trace("{}: {} on {}. Queue size pending={} running={}", name, stepName, workItem, queue.getQueueSize(),
                    queue.getRunningSize());
        }
    }

    @Override
    public void stopQueue() {
        queue.stop();
    }

    @Override
    public int getQueueSize() {
        return queue.getQueueSize();
    }

    @Override
    public int getNumberOfWorkers() {
        return workers - semaphore.availablePermits();
    }

    @Override
    public int getMaxNumberOfWorkers() {
        return workers;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void waitForItemDone(T workItem) {
        if (queue.contains(workItem)) {
            long timeout = ConstantValues.workQueueSyncExecutionTimeoutMillis.getLong();
            // Pending or running => Wait on work item
            synchronized (workItem) {
                if (queue.contains(workItem)) {
                    try {
                        workItem.wait(timeout);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(
                                "Work Item " + workItem + " did not finished completion in " + timeout + "ms", e);
                    }
                }
            }
        }
    }
}
