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

package org.artifactory.schedule;

import org.artifactory.backup.BackupJob;
import org.artifactory.repo.cleanup.IntegrationCleanupJob;
import org.artifactory.storage.binstore.service.BinaryStoreGarbageCollectorJob;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for {@link TaskUtils}.
 *
 * @author Yossi Shaul
 */
@Test
public class TaskUtilsTest {

    public void taskManualJobDetailsPopulation() {
        TaskBase t = TaskUtils.createManualTask(BackupJob.class, 0);
        assertTrue(t.getToken().startsWith("artifactory.BackupJob"));
        assertEquals(t.getLastStarted(), 0);
        assertEquals(t.getType(), BackupJob.class);
        assertTrue(t.isManuallyActivated());
        assertFalse(t.isRunning());
        JobCommand jobCommand = BackupJob.class.getAnnotation(JobCommand.class);
        assertEquals(t.isSingleton(), jobCommand.singleton());
        assertEquals(t.getDescription(), jobCommand.description() + " (manual trigger)");
    }

    public void taskRepeatingJobDetailsPopulation() {
        TaskBase t = TaskUtils.createRepeatingTask(IntegrationCleanupJob.class, 3, 4);
        t.addAttribute("custom", "attribute");
        assertTrue(t.getToken().startsWith("artifactory.IntegrationCleanupJob"));
        assertEquals(t.getLastStarted(), 0);
        assertEquals(t.getType(), IntegrationCleanupJob.class);
        assertFalse(t.isManuallyActivated());
        assertFalse(t.isRunning());
        JobCommand jobCommand = IntegrationCleanupJob.class.getAnnotation(JobCommand.class);
        assertEquals(t.isSingleton(), jobCommand.singleton());
        assertEquals(t.getDescription(), jobCommand.description());
    }

    public void taskCronJobDetailsPopulation() {
        TaskBase t = TaskUtils.createCronTask(BinaryStoreGarbageCollectorJob.class, "0 0 /6 * * ?");
        assertTrue(t.getToken().startsWith("artifactory.BinaryStoreGarbageCollectorJob"));
        assertEquals(t.getLastStarted(), 0);
        assertEquals(t.getType(), BinaryStoreGarbageCollectorJob.class);
        assertFalse(t.isManuallyActivated());
        assertFalse(t.isRunning());
        JobCommand jobCommand = BinaryStoreGarbageCollectorJob.class.getAnnotation(JobCommand.class);
        assertEquals(t.isSingleton(), jobCommand.singleton());
        assertEquals(t.getDescription(), jobCommand.description());
    }

    public void taskDescriptionOverride() {
        TaskBase t = TaskUtils.createManualTask(BinaryStoreGarbageCollectorJob.class, 2, "Another change");
        assertEquals(t.getDescription(), "Another change (manual trigger)");

        t = TaskUtils.createCronTask(BinaryStoreGarbageCollectorJob.class, "0 0 /6 * * ?",
                "Change the default");
        assertEquals(t.getDescription(), "Change the default");

        t = TaskUtils.createRepeatingTask(BinaryStoreGarbageCollectorJob.class, 1, 2,
                "My description");
        assertEquals(t.getDescription(), "My description");
    }
}