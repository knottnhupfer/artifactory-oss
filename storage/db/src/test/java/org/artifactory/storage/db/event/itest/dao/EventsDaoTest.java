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

package org.artifactory.storage.db.event.itest.dao;

import org.artifactory.storage.db.event.dao.EventsDao;
import org.artifactory.storage.db.event.entity.EventRecord;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.event.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Units tests for {@link EventsDao}.
 *
 * @author Yossi Shaul
 */
@Test
public class EventsDaoTest extends DbBaseTest {

    @Autowired
    private EventsDao dao;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes.sql");
    }

    public void countEvents() throws SQLException {
        assertEquals(dao.getEventsCount(), 18);
    }

    public void allEvents() throws SQLException {
        assertThat(dao.loadAll()).hasSize(18);
    }

    public void allEventsStream() throws SQLException {
        CompletableFuture<SQLException> onException = new CompletableFuture<>();
        List<EventRecord> lst = dao.loadAllStream(onException).collect(toList());
        if (onException.isDone()) throw onException.join();
        assertThat(lst).hasSize(18);
    }

    public void allEventsOrdered() throws SQLException {
        List<EventRecord> result = dao.loadAll();
        EventRecord[] ordered = result.stream().sorted().toArray(EventRecord[]::new);
        assertThat(result).hasSize(18).containsExactly((Object[]) ordered);
    }

    @Test(dependsOnMethods = {"countEvents", "allEvents", "allEventsOrdered"})
    public void createDirectoryNode() throws SQLException {
        EventRecord e = new EventRecord(50, System.currentTimeMillis(), EventType.create, "repo5/land/");
        dao.create(e);
    }

    public void loadNewerEventsAll() throws SQLException {
        List<EventRecord> result = dao.loadNewer(0L, "repo1");
        assertThat(result).hasSize(11);
        result.forEach(e -> {
            assertTrue(e.getPath().startsWith("repo1/"));
            assertTrue(e.getEventId() > 0);
            assertTrue(e.getTimestamp() > 0);
        });
    }

    // tests that the results from the database are ordered according to the event timestamp and id
    public void loadNewerEventsOrder() throws SQLException {
        List<EventRecord> result = dao.loadNewer(67676732L, "repo1");
        EventRecord[] ordered = result.stream().sorted().toArray(EventRecord[]::new);
        assertThat(result).hasSize(11).containsExactly((Object[]) ordered);
    }

    public void loadNewerStreamEventsOrder() throws SQLException {
        CompletableFuture<SQLException> onException = new CompletableFuture<>();
        List<EventRecord> result = dao.loadNewerStream(67676732L, "repo1", onException).collect(toList());
        if (onException.isDone()) throw onException.join();
        EventRecord[] ordered = result.stream().sorted().toArray(EventRecord[]::new);
        assertThat(result).hasSize(11).containsExactly((Object[]) ordered);
    }

    public void loadNewerEventsIsExclusive() throws SQLException {
        List<EventRecord> result = dao.loadNewer(1515653250003L, "repo2");
        assertThat(result).hasSize(2);
        result.forEach(e -> assertTrue(e.getTimestamp() > 1515653250003L));
    }

    public void loadNewerStreamEventsIsExclusive() throws SQLException {
        CompletableFuture<SQLException> onException = new CompletableFuture<>();
        List<EventRecord> result = dao.loadNewerStream(1515653250003L, "repo2", onException).collect(toList());
        if (onException.isDone()) throw onException.join();
        assertThat(result).hasSize(2);
        result.forEach(e -> assertTrue(e.getTimestamp() > 1515653250003L));
    }

    public void loadNewerOrSameEventsIsInclusive() throws SQLException {
        List<EventRecord> result = dao.loadNewerOrSame(1515653250003L, "repo2");
        assertThat(result).hasSize(3);
        result.forEach(e -> assertTrue(e.getTimestamp() >= 1515653250003L));
    }

    public void loadNewerOrSameStreamEventsIsInclusive() throws SQLException {
        CompletableFuture<SQLException> onException = new CompletableFuture<>();
        List<EventRecord> result = dao.loadNewerOrSameStream(1515653250003L, "repo2", onException).collect(toList());
        if (onException.isDone()) throw onException.join();
        assertThat(result).hasSize(3);
        result.forEach(e -> assertTrue(e.getTimestamp() >= 1515653250003L));
    }

    public void loadNewerEventsNoEvents() throws SQLException {
        assertThat(dao.loadNewer(1515653255555L, "repo2")).hasSize(0);
    }

    public void firstEventTimestamp() throws SQLException {
        assertThat(dao.getFirstEventTimestamp()).isEqualTo(1515653241184L);
    }

    public void lastEventTimestamp() throws SQLException {
        assertThat(dao.getLastEventTimestamp()).isEqualTo(1515653250004L);
    }

    public void loadEventsBetweenLoadAll() {
        assertThat(dao.loadIntervalStream(0, Long.MAX_VALUE, "repo1", 100, new CompletableFuture<>())
                .count())
                .isEqualTo(11);
    }

    public void loadEventsBetweenTestNonInclusive() {
        assertThat(dao.loadIntervalStream(1515653241184L, 1515653241293L, "repo1", 100, new CompletableFuture<>())
                .count())
                .isEqualTo(7);
    }

    public void loadEventsBetweenTestNonInclusiveAndLimit() {
        assertThat(dao.loadIntervalStream(1515653241184L, 1515653241293L, "repo1", 2, new CompletableFuture<>())
                .count())
                .isEqualTo(2);
    }

    public void loadEventsBetweenTestSorted() {
        List<EventRecord> events =
                dao.loadIntervalStream(1515653241184L, 1515653241293L, "repo1", 100, new CompletableFuture<>())
                        .collect(Collectors.toList());
        List<EventRecord> sortedEvents = new ArrayList<>(events);
        Collections.sort(sortedEvents);
        assertThat(events).isEqualTo(sortedEvents);
    }
}