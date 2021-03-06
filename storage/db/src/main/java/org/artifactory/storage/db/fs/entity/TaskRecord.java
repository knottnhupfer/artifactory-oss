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

package org.artifactory.storage.db.fs.entity;

import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;

/**
 * Represents a record in the tasks table.
 *
 * @author Yossi Shaul
 */
public class TaskRecord {

    private final String taskType;
    private final String taskContext;
    private final long created;

    public TaskRecord(String taskType, String taskContext, long created) {
        if (StringUtils.isBlank(taskType)) {
            throw new IllegalArgumentException("Task type cannot be empty");
        }
        this.taskType = taskType;
        this.taskContext = StringUtils.trimToEmpty(taskContext);
        this.created = created;
    }

    @Nonnull
    public String getTaskType() {
        return taskType;
    }

    @Nonnull
    public String getTaskContext() {
        return taskContext;
    }

    public long getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return "TaskRecord{" +
                "taskType='" + taskType + '\'' +
                ", taskContext='" + taskContext + '\'' +
                ", created=" + created +
                '}';
    }
}
