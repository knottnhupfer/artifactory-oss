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

package org.artifactory.api.repo;

import java.lang.reflect.Method;

/**
 * @author gidis
 */
public interface WorkQueue<T extends WorkItem> {

    /**
     * Offer a new work to the queue. If the work is accepted and there's no other worker thread, the offering thread
     * continues as the worker.
     *
     * @param workItem The work to perform
     * @return true if added to the work queue, false if an identical item already in queue
     */
    boolean offerWork(T workItem, Method method);

    int availablePermits();

    void doJobs();

    void stopQueue();

    String getName();

    void waitForItemDone(T workItem);

    /**
     * Returns the <b>estimated</b> size of pending tasks on this queue
     */
    int getQueueSize();

    int getMaxNumberOfWorkers();
}

