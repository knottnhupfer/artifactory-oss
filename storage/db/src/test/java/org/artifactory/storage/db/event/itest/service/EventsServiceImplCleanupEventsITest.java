package org.artifactory.storage.db.event.itest.service;

import org.artifactory.storage.db.event.service.InternalEventsService;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.event.EventInfo;
import org.artifactory.storage.event.EventType;
import org.artifactory.storage.event.EventsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class EventsServiceImplCleanupEventsITest extends DbBaseTest {

    @Autowired
    private InternalEventsService internalEventsService;

    @Autowired
    private EventsService eventsService;

    @BeforeMethod
    public void setup() {
        List<EventInfo> events = new ArrayList<>();
        events.add(new EventInfo(0L, EventType.create, "/a/b"));
        events.add(new EventInfo(1L, EventType.create, "/a/b"));
        events.add(new EventInfo(2L, EventType.create, "/a/b"));
        events.add(new EventInfo(3L, EventType.create, "/a/b"));
        events.add(new EventInfo(4L, EventType.create, "/a/b"));
        events.add(new EventInfo(5L, EventType.create, "/a/b"));
        events.add(new EventInfo(6L, EventType.create, "/a/b"));
        events.add(new EventInfo(7L, EventType.create, "/a/b"));
        events.add(new EventInfo(8L, EventType.create, "/a/b"));
        events.add(new EventInfo(9L, EventType.create, "/a/b"));
        eventsService.appendEvents(events);
    }

    @AfterMethod
    public void cleanup() {
        internalEventsService.deleteAll();
    }

    @Test
    public void testDeleteSinceBeginningOfTime() {
        eventsService.deleteRange(2L, 5L);
        assertEquals(7, internalEventsService.getAllEvents().size());
    }
}
