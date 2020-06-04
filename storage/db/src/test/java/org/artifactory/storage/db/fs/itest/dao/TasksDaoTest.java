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

package org.artifactory.storage.db.fs.itest.dao;

import org.artifactory.storage.db.fs.dao.TasksDao;
import org.artifactory.storage.db.fs.entity.TaskRecord;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Tests the {@link org.artifactory.storage.db.fs.dao.TasksDao}.
 *
 * @author Yossi Shaul
 */
@Test
public class TasksDaoTest extends DbBaseTest {

    @Autowired
    private TasksDao tasksDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes.sql");
    }

    public void loadByPrefix() throws SQLException {
        List<TaskRecord> tasks = tasksDao.loadByPrefix("DEBIAN_INDEX", "trusty/main");
        assertEquals(tasks.size(), 4);

        TaskRecord resultTask = tasks.get(0);
        assertEquals(resultTask.getTaskType(), "DEBIAN_INDEX");
        assertEquals(resultTask.getTaskContext(), "trusty/main/i386::ADD::path/to/file41");
        assertEquals(resultTask.getCreated(), 1578315161000L);

        resultTask = tasks.get(1);
        assertEquals(resultTask.getTaskType(), "DEBIAN_INDEX");
        assertEquals(resultTask.getTaskContext(), "trusty/main/i386::ADD::path/to/file42");
        assertEquals(resultTask.getCreated(), 1578315162000L);

        resultTask = tasks.get(2);
        assertEquals(resultTask.getTaskType(), "DEBIAN_INDEX");
        assertEquals(resultTask.getTaskContext(), "trusty/main/i386::ADD::path/to/file45");
        assertEquals(resultTask.getCreated(), 1578315165000L);

        resultTask = tasks.get(3);
        assertEquals(resultTask.getTaskType(), "DEBIAN_INDEX");
        assertEquals(resultTask.getTaskContext(), "trusty/main/i386::ADD::path/to/file47");
        assertEquals(resultTask.getCreated(), 1578315167000L);
    }

    public void deleteTaskRecord() throws SQLException {
        assertTrue(tasksDao.exist("X_TYPE", "some::task::with::context"));
        boolean deleted = tasksDao.delete(new TaskRecord("X_TYPE", "some::task::with::context", 11));
        assertFalse(deleted);
        assertTrue(tasksDao.exist("X_TYPE", "some::task::with::context"));

        deleted = tasksDao.delete(new TaskRecord("X_TYPE", "some::task::with::context", 1340299999999L));
        assertTrue(deleted);
        assertFalse(tasksDao.exist("X_TYPE", "some::task::with::context"));
    }

    public void loadByType() throws SQLException {
        List<TaskRecord> tasks = tasksDao.load("INDEX");
        assertNotNull(tasks);
        assertEquals(tasks.size(), 2);
        for (TaskRecord task : tasks) {
            assertEquals(task.getTaskType(), "INDEX");
        }
    }

    public void loadOrdered() throws SQLException {
        List<TaskRecord> tasks = tasksDao.load("INDEX");
        assertNotNull(tasks);
        assertEquals(tasks.size(), 2);
        assertEquals(tasks.get(0).getTaskContext(), "reponone:test");
        assertEquals(tasks.get(1).getTaskContext(), "repo1:ant/ant/1.5/ant-1.5.jar");
    }

    public void exist() throws SQLException {
        assertTrue(tasksDao.exist("INDEX", "repo1:ant/ant/1.5/ant-1.5.jar"));
        assertFalse(tasksDao.exist("INDEX", "repo1:ant/ant/1.9/ant-1.9.jar"));
    }

    public void create() throws SQLException {
        assertFalse(tasksDao.exist("MYTEST", "123"));
        long now = System.currentTimeMillis();
        tasksDao.create("MYTEST", "123", now);
        List<TaskRecord> myTestTasks = tasksDao.load("MYTEST");
        assertEquals(myTestTasks.size(), 1);
        assertEquals(myTestTasks.iterator().next().getTaskType(), "MYTEST");
        assertEquals(myTestTasks.iterator().next().getTaskContext(), "123");
        assertEquals(myTestTasks.iterator().next().getCreated(), now);
    }

    public void createDuplicate() throws SQLException {
        // until decided otherwise the unique constraint is removed
        tasksDao.create("DUPLICATE", "DUP", 0);
        tasksDao.create("DUPLICATE", "DUP", 0);
    }

    public void delete() throws SQLException {
        assertTrue(tasksDao.exist("MMC", "this/is/a/test"));
        assertTrue(tasksDao.delete("MMC", "this/is/a/test"));
        assertFalse(tasksDao.exist("MMC", "this/is/a/test"));
    }
}
