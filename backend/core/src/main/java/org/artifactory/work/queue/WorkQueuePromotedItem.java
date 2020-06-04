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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * A promoted item signifies a work queue item that has been promoted from 'pending' to 'executing'.
 * It also holds a list of all other identical work items that have threads polling on them for the execution to finish.
 * The duplicates were collected during the change in state and have been deleted from the pending items queue so that
 * no duplicates remain when calculation of the item has started.
 *
 * At the end of the execution notifyAll() is called on each item in the 'polled' list.
 *
 * @author Gidi Shabat
 * @author Dan Feldman
 */
class WorkQueuePromotedItem<T extends WorkItem> {

    final T workItem;
    final Method method;
    final List<T> pendingWorkItemsAssociated;

    WorkQueuePromotedItem(T workItem, Method method, List<T> pendingWorkItemsAssociated) {
        this.workItem = Objects.requireNonNull(workItem, "Work item cannot be null");
        this.method = method;
        this.pendingWorkItemsAssociated = pendingWorkItemsAssociated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WorkQueuePromotedItem<?> that = (WorkQueuePromotedItem<?>) o;

        return workItem.equals(that.workItem);

    }

    @Override
    public int hashCode() {
        return workItem.hashCode();
    }
}
