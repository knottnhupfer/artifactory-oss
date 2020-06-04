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

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.WorkItem;
import org.jfrog.storage.common.ConflictsGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Gidi Shabat
 * @author Dan Feldman
 */
public class NonBlockingOnWriteQueue<T extends WorkItem> {
    private static final Logger log = LoggerFactory.getLogger(NonBlockingOnWriteQueue.class);

    private final ConflictsGuard<String> conflictsGuard;
    private final ConcurrentLinkedQueue<WorkItemWrapper<T>> pendingQueue;
    private final Set<WorkQueuePromotedItem<T>> promotedWorkItems;
    private boolean running = true;

    /**
     * Constructor package limited
     *
     * @param name used to name the HA map, if it will be created
     */
    NonBlockingOnWriteQueue(String name) {
        conflictsGuard = ContextHelper.get().beanForType(AddonsManager.class).addonByType(HaAddon.class).getConflictsGuard(name);
        pendingQueue = new ConcurrentLinkedQueue<>();
        promotedWorkItems = new CopyOnWriteArraySet<>();
    }

    /**
     * Adds new Item to queue. (pending)
     */
    boolean addToPending(T workItem, Method method) {
        if (!running) {
            log.warn("Adding work item {} while queue is stopped", workItem);
            return false;
        }
        log.trace("adding {}: to queue", workItem);
        WorkItemWrapper<T> wrapper = new WorkItemWrapper<>(workItem, method);
        boolean result = pendingQueue.add(wrapper);
        log.trace("added {} to queue, success: {}", workItem, result);
        return result;
    }

    /**
     * Changes the workItem state from pending to running
     */
    public WorkQueuePromotedItem<T> promote() {
        if (!running) {
            log.debug("Trying to promote a queue item when queue being stopped");
            return null;
        }
        WorkItemWrapper<T> workItemWrapper = acquireLockOnKey();
        if (workItemWrapper != null) {
            T workItem = workItemWrapper.getWorkItem();
            log.trace("promoting  {}: workItem", workItem);
            WorkQueuePromotedItem<T> promotedItem = new WorkQueuePromotedItem<>(workItem, workItemWrapper.getMethod(),
                    pendingQueue.stream()
                            .filter(pendingItem -> pendingItem.getWorkItem().equals(workItem))
                            .map(WorkItemWrapper::getWorkItem)
                            .collect(Collectors.toList()));
            promotedItem.workItem.addIdenticalWorkItems(promotedItem.pendingWorkItemsAssociated);
            if (!promotedWorkItems.add(promotedItem)) {
                // There should be only one promoted item with the same key
                // Removing lock to make sure it does not leak
                conflictsGuard.unlock(workItem.getUniqueKey());
                throw new IllegalStateException("There can be only one process running work for " + workItem);
            }
            pendingQueue.removeIf(t -> {
                if (t.getWorkItem().equals(workItem)) {
                    // Be aware that ref equality is what's needed here
                    for (T t1 : promotedItem.pendingWorkItemsAssociated) {
                        if (t.getWorkItem() == t1) {
                            return true;
                        }
                    }
                }
                return false;
            });
            return promotedItem;
        }
        return null;
    }

    /**
     * The method tries to acquire lock on one of the queue workItems
     */
     WorkItemWrapper<T> acquireLockOnKey() {
        if (!running) {
            log.debug("Trying to acquire lock on a queue item when queue being stopped");
            return null;
        }
        // Some of the workItems might be temporary locked by other workers
        // Try to acquire lock on one of the workItems in pendingQueue
        for (WorkItemWrapper<T> workItemWrapper : pendingQueue) {
            T workItem = workItemWrapper.getWorkItem();
            boolean acquired = false;
            try {
                acquired = conflictsGuard.tryToLock(workItem.getUniqueKey(), 0, SECONDS);
            } catch (InterruptedException e) {
                log.error("Failed to acquire lock for workItem {}", workItem);
            }
            // Successfully acquire lock on workItem the return the workItem else try another workItem
            if (acquired) {
                return workItemWrapper;
            }
        }
        return null;
    }

    /**
     * Remove workItem from queue
     */
    public boolean remove(WorkQueuePromotedItem<T> promotedItem) {
        try {
            promotedWorkItems.remove(promotedItem);
            finishedWithPromotedWorkItem(promotedItem);
        } catch (Exception e) {
            log.info("Exception while notifying work item  waiters " + promotedItem.workItem + " on remove: " +
                    e.getMessage(), e);
        }
        try {
            log.trace("removing workItem {}.", promotedItem.workItem);
            conflictsGuard.unlock(promotedItem.workItem.getUniqueKey());
            log.trace("removed workItem {}.", promotedItem.workItem);
            return true;
        } catch (Exception e) {
            log.info("Exception while unlocking work item " + promotedItem.workItem + " on remove: " + e.getMessage(),
                    e);
            return false;
        }
    }

    /**
     * {@param promotedWorkItem} may contain identical work items, each of them may have threads polling on them to
     * complete.
     * This method notifies all polling threads on any work item that is marked as being polled on.
     */
    private void finishedWithPromotedWorkItem(WorkQueuePromotedItem<T> promotedWorkItem) {
        promotedWorkItem.pendingWorkItemsAssociated
                .forEach(polledWorkItem -> {
                    synchronized (polledWorkItem) {
                        log.trace("Notifying polling threads on Work Item with unique key: {} and object reference: {}",
                                polledWorkItem.getUniqueKey(), polledWorkItem);
                        polledWorkItem.notifyAll();
                    }
                });
    }

    /**
     * Stop and clean the queue
     */
    public void stop() {
        running = false;
        pendingQueue.clear();
        List<WorkQueuePromotedItem<T>> list = new ArrayList<>(promotedWorkItems);
        for (WorkQueuePromotedItem<T> promotedItem : list) {
            remove(promotedItem);
        }
    }

    /**
     * Returns the running size of the queue
     *
     * BEWARE: This method calls non-constant time operations on the queue and the locking map, in HA scenarios the
     * results from the locking map reflect tasks running on this node and others as well as well as the pending tasks
     * on this node alone - refrain from using this outside of tests!
     */
    public int getRunningSize() {
        int result;
        log.trace("getting the size of the queue");
        result = conflictsGuard.size();
        log.trace("successfully got the size of the queue.");
        return result;
    }

    /**
     * Returns the pending size of the queue
     *
     * BEWARE: This method calls non-constant time operations on the queue and the locking map, in HA scenarios the
     * results from the locking map reflect tasks running on this node and others as well as well as the pending tasks
     * on this node alone - refrain from using this outside of tests!
     */
    public int getQueueSize() {
        return pendingQueue.size();
    }

    /**
     * Check if a specific work item is in pending or working queue
     *
     * @param workItem the exact work item by Java reference
     * @return true if pending, false otherwise
     */
    public boolean contains(T workItem) {
        if (!running) {
            log.debug("Trying to check for contains a queue item when queue being stopped");
            return false;
        }
        for (WorkItemWrapper<T> itemWrapper : pendingQueue) {
            WorkItem item = itemWrapper.getWorkItem();
            // Be aware that ref equality is what's needed here
            if (item == workItem) {
                return true;
            }
        }
        // The lock is used to make sure a promotion is not half in process
        if (conflictsGuard.isLocked(workItem.getUniqueKey())) {
            PromotedFoundItems promotedFoundItems = new PromotedFoundItems(workItem);
            if (!promotedFoundItems.isFoundCurrentWork()) {
                // Lock but no promoted entry, means still not inserted in promoted map
                Thread.yield();
                if (conflictsGuard.isLocked(workItem.getUniqueKey())) {
                    // Try one more time only
                    promotedFoundItems = new PromotedFoundItems(workItem);
                    return promotedFoundItems.isFoundExactWorkItem();
                } else {
                    // Work done
                    return false;
                }
            }
            return promotedFoundItems.isFoundExactWorkItem();
        }
        return false;
    }

    private class PromotedFoundItems {
        private boolean foundCurrentWork = false;
        private boolean foundExactWorkItem = false;

        PromotedFoundItems(T workItem) {
            for (WorkQueuePromotedItem<T> promotedItem : promotedWorkItems) {
                // Be aware that here it's unique key equality. There should be only one here
                if (promotedItem.workItem.equals(workItem)) {
                    foundCurrentWork = true;
                    // Be aware that ref equality is what's needed here
                    if (promotedItem.workItem == workItem) {
                        foundExactWorkItem = true;
                    } else {
                        // Find in associated work items
                        for (T item : promotedItem.pendingWorkItemsAssociated) {
                            if (item == workItem) {
                                foundExactWorkItem = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        boolean isFoundCurrentWork() {
            return foundCurrentWork;
        }

        boolean isFoundExactWorkItem() {
            return foundExactWorkItem;
        }

        public PromotedFoundItems invoke() {
            foundCurrentWork = false;
            foundExactWorkItem = false;
            return this;
        }
    }
}
