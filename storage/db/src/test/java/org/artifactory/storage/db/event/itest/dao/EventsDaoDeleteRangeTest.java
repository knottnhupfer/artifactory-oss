package org.artifactory.storage.db.event.itest.dao;

import org.artifactory.storage.db.event.dao.EventsDao;
import org.artifactory.storage.db.event.entity.EventRecord;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.event.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventsDaoDeleteRangeTest extends DbBaseTest {

    @Autowired
    private EventsDao dao;

    @BeforeMethod
    public void setup() throws SQLException {
        dao.create(new EventRecord(0L,0L, EventType.create, "/a/b/c"));
        dao.create(new EventRecord(1L,1L, EventType.create, "/a/b/c"));
        dao.create(new EventRecord(2L,2L, EventType.create, "/a/b/c"));
        dao.create(new EventRecord(3L,3L, EventType.create, "/a/b/c"));
        dao.create(new EventRecord(4L,4L, EventType.create, "/a/b/c"));
        dao.create(new EventRecord(5L,5L, EventType.create, "/a/b/c"));
        dao.create(new EventRecord(6L,6L, EventType.create, "/a/b/c"));
        dao.create(new EventRecord(7L,7L, EventType.create, "/a/b/c"));
        dao.create(new EventRecord(8L,8L, EventType.create, "/a/b/c"));
        dao.create(new EventRecord(9L,9L, EventType.create, "/a/b/c"));
        assertEquals(10, dao.loadAll().size());
    }

    @AfterMethod
    public void cleanup() throws SQLException {
        dao.deleteAll();
    }

    @Test
    public void testDeleteRange() throws SQLException {
        dao.deleteRange(5L, 8L);
        List<EventRecord> eventRecordsAfterDelete = dao.loadAll();
        assertEquals(7, eventRecordsAfterDelete.size());
        assertTrue(eventRecordsAfterDelete.stream().anyMatch(r -> r.getTimestamp() == 8L));
        assertTrue(eventRecordsAfterDelete.stream().noneMatch(r -> r.getTimestamp() == 5L));
        assertTrue(eventRecordsAfterDelete.stream().noneMatch(r -> r.getTimestamp() == 6L));
        assertTrue(eventRecordsAfterDelete.stream().noneMatch(r -> r.getTimestamp() == 7L));
    }

    @Test
    public void testDeleteOlderThanMaxLongNewerThanZero() throws SQLException {
        dao.deleteRange(0L, Long.MAX_VALUE);
        List<EventRecord> eventRecordsAfterDelete = dao.loadAll();
        assertEquals(0, eventRecordsAfterDelete.size());
    }

    @Test
    public void testOutOfRangeRangeIsLarge() throws SQLException {
        dao.deleteRange(Long.MAX_VALUE, Long.MAX_VALUE - 10);
        List<EventRecord> eventRecordsAfterDelete = dao.loadAll();
        assertEquals(10, eventRecordsAfterDelete.size());
    }

    @Test
    public void testOutOfRangeRangeIsSmall() throws SQLException {
        dao.deleteRange(Long.MIN_VALUE, Long.MIN_VALUE + 100);
        List<EventRecord> eventRecordsAfterDelete = dao.loadAll();
        assertEquals(10, eventRecordsAfterDelete.size());
    }

    @Test
    public void testDeleteOlderThanMaxTimestampInTable() throws SQLException {
        dao.deleteRange(100L, 110L);
        List<EventRecord> eventRecordsAfterDelete = dao.loadAll();
        assertEquals(10, eventRecordsAfterDelete.size());
    }

}
