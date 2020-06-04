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

package org.artifactory.storage.fs.service;

import org.artifactory.repo.RepoPath;
import org.artifactory.storage.task.TaskInfo;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A business service to interact with the tasks table.
 *
 * @author Yossi Shaul
 */
public interface TasksService {

    /**
     * Unique name of the archive indexing task type
     */
    String TASK_TYPE_INDEX = "INDEX";
    String TASK_TYPE_XRAY_EVENT = "XRAY_EVENT";
    String TASK_TYPE_DEBIAN_INDEX = "DEBIAN_INDEX";

    /**
     * @return All the repo paths currently pending for indexing.
     */
    @Nonnull
    List<RepoPath> getIndexTasks();

    /**
     * Get all tasks by type and context prefix.
     * For example, task:'DEBIAN_EVENT' and context:'a/b/c' can be queries using 'taskType=DEBIAN_EVENT' and
     * 'prefix=a'
     *
     * @param taskType          taskType to return
     * @param taskContextPrefix textual prefix of the task
     * @return task by type and context prefix
     */
    List<TaskInfo> getTaskByTypeAndContextPrefix(String taskType, String taskContextPrefix);

    /**
     * @param repoPath The repo path to check
     * @return True if there is a pending index request for this checksum
     */
    boolean hasIndexTask(RepoPath repoPath);

    /**
     * Adds an index task for the given repo path.
     *
     * @param repoPath The repo path to index
     */
    void addIndexTask(RepoPath repoPath);

    /**
     * @param tasks tasks to add
     */
    void addTasks(List<TaskInfo> tasks);

    void removeTasks(List<TaskInfo> taskInfos);

    /**
     * Removes an index task.
     *
     * @param repoPath The repo path to remove
     * @return True if removed from the database.
     */
    boolean removeIndexTask(RepoPath repoPath);
    /**
     * Adds an index task for the given repo path.
     *
     * @param xrayEvent Xray event for indexing
     */
    @Transactional
    void addXrayEventTask(String xrayEvent);

    /**
     * Removes an index task.
     *
     * @param xrayEvent Xray event for indexing
     * @return True if removed from the database.
     */
    @Transactional
    boolean removeXrayEventTask(String xrayEvent);

    /**
     * Removes all xray index tasks.
     */
    @Transactional
    void removeAllXrayEventTasks();

    /**
     * @return All the xray event tasks.
     */
    @Nonnull
    List<XrayTask> getXrayEventTasks();

    /**
     * @param xrayEvent Xray event for indexing
     * @return True if there is a pending index request for this checksum
     */
    boolean hasXrayEventTask(String xrayEvent);

}
