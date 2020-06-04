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

package org.artifactory.storage.db.event.itest.service;

import com.google.common.collect.Lists;
import org.artifactory.storage.db.event.service.InternalEventsService;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.event.EventInfo;
import org.artifactory.storage.event.EventType;
import org.artifactory.storage.event.EventsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Comparator;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Low level integration tests for the events service.
 *
 * @author Yossi Shaul
 */
@Test
public class EventsServiceImplITest extends DbBaseTest {

    @Autowired
    private EventsService service;

    @Autowired
    private InternalEventsService internal;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes-for-service.sql");
    }

    public void countEvents() {
        assertEquals(service.getEventsCount(), 18);
    }

    public void allEvents() {
        assertThat(internal.getAllEvents()).hasSize(18);
    }

    @Test(dependsOnMethods = {"countEvents", "allEvents"})
    public void createSingleEvent() {
        EventInfo e = new EventInfo(System.currentTimeMillis(), EventType.create, "repo5/land/");
        service.appendEvents(Lists.newArrayList(e));
        assertEquals(service.getEventsCount(), 19);
    }

    @Test(dependsOnMethods = "createSingleEvent")
    public void createMultipleEvents() {
        EventInfo e1 = new EventInfo(System.currentTimeMillis(), EventType.create, "repo5/land/");
        EventInfo e2 = new EventInfo(System.currentTimeMillis(), EventType.delete, "repo5/land/");
        service.appendEvents(Lists.newArrayList(e1, e2));
        assertEquals(service.getEventsCount(), 21);
    }

    public void firstEventTimestamp() {
        assertThat(service.getFirstEventTimestamp()).isEqualTo(1515653241184L);
    }

    public void getEventsAfterAll() {
        List<EventInfo> result = service.getEventsAfter(0L, "repo1");
        assertThat(result).hasSize(11);
        result.forEach(e -> {
            assertTrue(e.getPath().startsWith("repo1/"));
            assertTrue(e.getTimestamp() > 0);
        });
    }

    // tests that the results from the database are ordered according to the event timestamp
    public void getEventsAfterOrder() {
        List<EventInfo> result = service.getEventsAfter(67676732L, "repo1");
        EventInfo[] ordered = result.stream().sorted(Comparator.comparingLong(EventInfo::getTimestamp))
                .toArray(EventInfo[]::new);
        assertThat(result).hasSize(11).containsExactly((Object[]) ordered);
    }

    // tests that the results from the database are ordered according to the event timestamp and id
    public void getEventsAfterOrderByIdSecondary() {
        List<EventInfo> result = service.getEventsAfter(67676732L, "repo1");
        // the vents in the imported file has the same timestamp, and `repo1/ant/ant/1.5/ant-1.5.jar` appears before
        // 'repo1/ant/ant/1.5/' but has a lower id
        assertEquals(result.get(4).getPath(), "repo1/ant/ant/1.5/");
        assertEquals(result.get(5).getPath(), "repo1/ant/ant/1.5/ant-1.5.jar");
    }


    public void getEventsAfterIsExclusive() {
        List<EventInfo> result = service.getEventsAfter(1515653250003L, "repo2");
        assertThat(result).hasSize(2);
        result.forEach(e -> assertTrue(e.getTimestamp() > 1515653250003L));
    }

    public void getEventsAfterNoEvents() {
        assertThat(service.getEventsAfter(1515653255555L, "repo2")).hasSize(0);
    }

}
