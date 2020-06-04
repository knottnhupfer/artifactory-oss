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

package org.artifactory.rest.resource.task;

import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * Simple list wrapper to return JSON string with list of tasks
 *
 * @author Yossi Shaul
 */
public class BackgroundTasks {

    private List<BackgroundTask> tasks = Lists.newArrayList();

    public BackgroundTasks(@JsonProperty("tasks") List<BackgroundTask> tasks) {
        this.tasks = tasks;
    }

    public List<BackgroundTask> getTasks() {
        return Lists.newArrayList(tasks);
    }

    public void addTasks(List<BackgroundTask> tasks) {
        this.tasks.addAll(tasks);
    }

    @Override
    public String toString() {
        return "BackgroundTasks{tasks=" + tasks + '}';
    }
}
