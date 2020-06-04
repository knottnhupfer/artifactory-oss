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

import org.artifactory.rest.common.util.RestUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link BackgroundTask}.
 *
 * @author Yossi Shaul
 */
@Test
public class BackgroundTaskTest {

    public void backgroundTaskWithNoStartedDate() {
        BackgroundTask task = new BackgroundTask("1", "type", "running", "test", 0);
        assertEquals(task.getStartedMillis(), 0);
    }

    public void backgroundTaskWithStartedDate() {
        long started = System.currentTimeMillis() - 1000;
        BackgroundTask task = new BackgroundTask("1", "type", "running", "test", started);
        assertEquals(task.getStartedMillis(), started);
        assertEquals(RestUtils.fromIsoDateString(task.getStarted()), started);
    }
}