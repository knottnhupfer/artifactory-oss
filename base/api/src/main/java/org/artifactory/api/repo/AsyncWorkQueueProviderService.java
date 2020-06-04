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
import java.util.Map;

/**
 * @author gidis
 */
public interface AsyncWorkQueueProviderService {

    /**
     * Get a work queue. If no work queue exists yet, create one.
     */
    WorkQueue<WorkItem> getWorkQueue(Method workQueueCallback, Object target);

    /**
     * Get all existing work queues.
     * @return all existing work queues.
     */
    Map<String, WorkQueue<WorkItem>> getExistingWorkQueues();

    void closeAllQueues();

    int getEstimatedPendingTasksSize(String workQueueCallbackName);
}