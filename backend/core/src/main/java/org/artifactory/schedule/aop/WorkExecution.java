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

package org.artifactory.schedule.aop;

import org.artifactory.api.repo.WorkItem;
import org.artifactory.api.repo.WorkQueue;

/**
 * @author gidis
 */
class WorkExecution {
    final WorkQueue<WorkItem> workQueue;
    final WorkItem workItem;
    final boolean blockUntilFinished; //Signifies a thread is polling on this work item to finish.

    WorkExecution(WorkQueue<WorkItem> workQueue, WorkItem workItem, boolean blockUntilFinished) {
        this.workQueue = workQueue;
        this.workItem = workItem;
        this.blockUntilFinished = blockUntilFinished;
    }
}