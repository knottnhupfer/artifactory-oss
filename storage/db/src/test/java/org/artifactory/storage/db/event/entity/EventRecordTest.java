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

package org.artifactory.storage.db.event.entity;

import org.artifactory.storage.event.EventType;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link EventRecord}
 *
 * @author Yossi Shaul
 */
@Test
public class EventRecordTest {

    public void compareDifferentTimestamp() {
        EventRecord e1 = new EventRecord(1, 1, EventType.create, "/");
        EventRecord e2 = new EventRecord(2, 223, EventType.create, "/");
        assertEquals(e1.compareTo(e2), -1, "e1 is before e2");
    }

    public void compareSameTimestampDifferentId() {
        EventRecord e1 = new EventRecord(2, 223, EventType.create, "/");
        EventRecord e2 = new EventRecord(1, 223, EventType.delete, "/");
        assertEquals(e1.compareTo(e2), 1, "e1 is after e2 according to id");
    }

    public void compareSameTimestampSameId() {
        EventRecord e1 = new EventRecord(1, 2, EventType.create, "/");
        EventRecord e2 = new EventRecord(1, 2, EventType.delete, "/");
        assertEquals(e1.compareTo(e2), 0, "e1 is same as e2");
    }

}