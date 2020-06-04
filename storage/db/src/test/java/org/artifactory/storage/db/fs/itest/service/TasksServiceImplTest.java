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

package org.artifactory.storage.db.fs.itest.service;

import com.google.common.collect.ImmutableList;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.fs.service.TasksService;
import org.artifactory.storage.fs.service.XrayTask;
import org.artifactory.storage.task.TaskInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

/**
 * Tests the {@link org.artifactory.storage.fs.service.TasksService}
 *
 * @author Yossi Shaul
 */
@Test
public class TasksServiceImplTest extends DbBaseTest {

    @Autowired
    private TasksService tasksService;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes-for-service.sql");
    }

    public void getIndexTasks() {
        List<RepoPath> tasks = tasksService.getIndexTasks();
        assertNotNull(tasks);
        assertEquals(tasks.size(), 2);
        assertTrue(tasks.contains(InternalRepoPathFactory.createRepoPath("repo1:ant/ant/1.5/ant-1.5.jar")));
        assertTrue(tasks.contains(InternalRepoPathFactory.createRepoPath("reponone:ant/ant/1.5/ant-1.5.jar")));
    }

    public void hasIndexTask() {
        assertTrue(tasksService.hasIndexTask(InternalRepoPathFactory.createRepoPath("repo1:ant/ant/1.5/ant-1.5.jar")));
    }

    @Test(dependsOnMethods = "getIndexTasks")
    public void addIndexTask() {
        RepoPath repoPath = InternalRepoPathFactory.createRepoPath("repo2:test");
        assertFalse(tasksService.hasIndexTask(repoPath));
        tasksService.addIndexTask(repoPath);
        assertTrue(tasksService.hasIndexTask(repoPath));
    }

    @Test(dependsOnMethods = "addIndexTask")
    public void removeIndexTask() {
        assertTrue(tasksService.removeIndexTask(InternalRepoPathFactory.createRepoPath("repo2:test")));
        assertFalse(tasksService.hasIndexTask(InternalRepoPathFactory.createRepoPath("repo2:test")));
    }

    @Test(dependsOnMethods = "hasIndexTask")
    public void removeIndexTaskByRepoPath() {
        assertTrue(
                tasksService.removeIndexTask(InternalRepoPathFactory.createRepoPath("repo1:ant/ant/1.5/ant-1.5.jar")));
    }

    public void removeIndexTaskByRepoPathNotExist() {
        assertFalse(tasksService.removeIndexTask(InternalRepoPathFactory.createRepoPath("nosuch:path.txt")));
    }

    public void getXrayEventOrderedTasks() {
        List<XrayTask> events = tasksService.getXrayEventTasks();
        assertNotNull(events);
        assertEquals(events.size(), 2);
        assertEquals(events.get(0).getTaskContext(), "xray-event-1");
        assertEquals(events.get(1).getTaskContext(), "xray-event-2");
    }

    public void hasXrayEventTask() {
        assertTrue(tasksService.hasXrayEventTask("xray-event-1"));
        assertFalse(tasksService.hasXrayEventTask("xray-event-999"));
    }

    @Test(dependsOnMethods = "getXrayEventOrderedTasks")
    public void addXrayEventTask() {
        tasksService.addXrayEventTask("xray-event-3");
        assertTrue(tasksService.hasXrayEventTask("xray-event-3"));
    }

    @Test(dependsOnMethods = "addXrayEventTask")
    public void removeXrayEventTask() {
        assertTrue(tasksService.removeXrayEventTask("xray-event-3"));
        assertFalse(tasksService.hasXrayEventTask("xray-event-3"));
    }

    public void removeXrayEventByKeyNotExist() {
        assertFalse(tasksService.removeXrayEventTask("nosuchevent"));
    }

    public void addTasks() {
        List<TaskInfo> tasks = ImmutableList.of(
                new TaskInfo("DEB:X", "a/b/c", 99L),
                new TaskInfo("DEB:X", "a/x/c", 99L),
                new TaskInfo("DEB:X", "z/x/c", 99L),
                new TaskInfo("DEB:Y", "a/b/c", 99L)
        );
        tasksService.addTasks(tasks);

        // all 'DEB:X' with prefix of 'a'
        List<TaskInfo> results = tasksService.getTaskByTypeAndContextPrefix("DEB:X", "a");
        assertEquals(results.size(), 2);
        List<TaskInfo> expectedTasks = tasks.stream()
                .filter(task -> task.getTaskType().equals("DEB:X") && task.getTaskContext().startsWith("a"))
                .collect(Collectors.toList());
        assertTrue(results.containsAll(expectedTasks));

        // all 'DEB:X' with prefix of 'z'
        results = tasksService.getTaskByTypeAndContextPrefix("DEB:X", "z");
        assertEquals(results.size(), 1);
        assertEquals(results.get(0), (new TaskInfo("DEB:X", "z/x/c", 99L)));

        // all 'DEB:Y' with prefix of 'a'
        results = tasksService.getTaskByTypeAndContextPrefix("DEB:Y", "a");
        assertEquals(results.size(), 1);
        assertEquals(results.get(0),(new TaskInfo("DEB:Y", "a/b/c", 99L)));
    }

    public void addEmptyTasks() {
        tasksService.addTasks(null);
        tasksService.addTasks(new ArrayList<>());
    }

    public void getTasksByPrefix() {
        List<TaskInfo> tasks = tasksService
                .getTaskByTypeAndContextPrefix("DEB", "deb-local::trusty");
        List<TaskInfo> expectedTasks = ImmutableList.of(
                new TaskInfo("DEB", "deb-local::trusty/main/i386::ADD::dddd88fc2a043c2479a6de676a2f7179e9eaddac",
                        1340285601421L),
                new TaskInfo("DEB", "deb-local::trusty/main/i386::ADD::aaaa88fc2a043c2479a6de676a2f7179e9dddd",
                        1340285601422L),
                new TaskInfo("DEB", "deb-local::trusty/main/i386::DELETE::dddd88fc2a043c2479a6de676a2f7179e9eaddac",
                        1340285601426L),
                new TaskInfo("DEB", "deb-local::trusty/main/i386::ADD::aaaa88fc2a043c2479a6de676a2f7179e9dddd",
                        1340285601427L)
        );
        assertEquals(tasks, expectedTasks);
    }

    public void removeTasks() {
        List<TaskInfo> tasks = ImmutableList.of(
                new TaskInfo("DEB", "deb-local::precise/main/i386::ADD::aaaa88fc2a043c2479a6de676a2f7179e9dddd",
                        1340285601423L),
                new TaskInfo("DEB", "deb-local::precise/main/i386::DELETE::aaaa88fc2a043c2479a6de676a2f7179e9dddd",
                        1340285601425L));
        List<TaskInfo> resultedTasks = tasksService
                .getTaskByTypeAndContextPrefix("DEB", "deb-local::precise");
        assertEquals(resultedTasks.size(), 2);
        assertTrue(resultedTasks.containsAll(tasks));

        // trying to delete similar tasks but with different dates, nothing should be deleted
        List<TaskInfo> similarTasks = ImmutableList.of(
                new TaskInfo("DEB", "deb-local::precise/main/i386::ADD::aaaa88fc2a043c2479a6de676a2f7179e9dddd",
                        1340285601421L),
                new TaskInfo("DEB", "deb-local::precise/main/i386::DELETE::aaaa88fc2a043c2479a6de676a2f7179e9dddd",
                        1340285601421L));
        tasksService.removeTasks(similarTasks);
        assertEquals(resultedTasks.size(), 2);
        assertTrue(resultedTasks.containsAll(tasks));

        tasksService.removeTasks(tasks);
        resultedTasks = tasksService
                .getTaskByTypeAndContextPrefix("DEB", "deb-local::precise");
        assertEquals(resultedTasks.size(), 0);

        List<TaskInfo> otherTasks = tasksService
                .getTaskByTypeAndContextPrefix("DEB", "deb-local::trusty");
        assertEquals(otherTasks.size(), 4);
    }
}
