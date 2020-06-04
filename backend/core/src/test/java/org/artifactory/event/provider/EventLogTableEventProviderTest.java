package org.artifactory.event.provider;

import com.google.common.collect.ImmutableList;
import org.artifactory.storage.db.event.entity.NodeEventCursorType;
import org.artifactory.storage.db.event.model.NodeEventCursor;
import org.artifactory.storage.event.EventInfo;
import org.artifactory.storage.event.EventType;
import org.artifactory.storage.event.EventsService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.common.ClockUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Uriah Levy
 */
@Test
public class EventLogTableEventProviderTest extends ArtifactoryHomeBoundTest {

    @Mock
    EventsService eventsService;

    @BeforeMethod
    private void beforeMethod() {
        MockitoAnnotations.initMocks(this);
    }

    public void testIdenticalTimestampEventAdded() {
        EventLogTableEventProvider eventLogTableEventProvider = new EventLogTableEventProvider(eventsService);

        long timestamp = ClockUtils.epochMillis();
        ImmutableList<EventInfo> eventList = ImmutableList
                .of(new EventInfo(timestamp - TimeUnit.MINUTES.toMillis(5), EventType.create, "foo/bar.txt"),
                        new EventInfo(timestamp - TimeUnit.MINUTES.toMillis(6), EventType.props,
                                "foo/bar.txt"));

        when(eventsService.getEventsSince(anyLong())).thenReturn(eventList.stream());

        List<EventInfo> eventInfos = eventLogTableEventProvider
                .provideNextBatch(new NodeEventCursor("metadata-event-operator", timestamp, NodeEventCursorType.METADATA_MIGRATION));

        assertEquals(eventInfos.size(), 2);
        assertTrue(eventInfos.stream().anyMatch(e -> e.getTimestamp() == timestamp - TimeUnit.MINUTES.toMillis(5)));
        assertTrue(eventInfos.stream()
                .anyMatch(e -> e.getTimestamp() == timestamp - TimeUnit.MINUTES.toMillis(6)));
    }

    public void allEventTypesSupported() {
        EventLogTableEventProvider eventLogTableEventProvider = new EventLogTableEventProvider(eventsService);

        long eventsAfterTimestamp = ClockUtils.epochMillis();
        ImmutableList<EventInfo> eventList = ImmutableList
                .of(new EventInfo(eventsAfterTimestamp - TimeUnit.MINUTES.toMillis(5), EventType.create, "foo/bar.txt")
                        , new EventInfo(eventsAfterTimestamp - TimeUnit.MINUTES.toMillis(5), EventType.props,
                                "foo/bar.txt")
                        , new EventInfo(eventsAfterTimestamp - TimeUnit.MINUTES.toMillis(5), EventType.update,
                                "foo/bar.txt")
                        , new EventInfo(eventsAfterTimestamp - TimeUnit.MINUTES.toMillis(5), EventType.delete,
                                "foo/bar.txt"));

        when(eventsService.getEventsSince(anyLong())).thenReturn(eventList.stream());

        List<EventInfo> eventInfos = eventLogTableEventProvider
                .provideNextBatch(new NodeEventCursor("metadata-event-operator", eventsAfterTimestamp, NodeEventCursorType.METADATA_MIGRATION));
        assertEquals(eventInfos.size(), 4, "Event types [create/props/update/delete] should be supported");
        assertTrue(eventInfos.stream().anyMatch(e -> e.getType().equals(EventType.create)));
        assertTrue(eventInfos.stream().anyMatch(e -> e.getType().equals(EventType.props)));
        assertTrue(eventInfos.stream().anyMatch(e -> e.getType().equals(EventType.update)));
        assertTrue(eventInfos.stream().anyMatch(e -> e.getType().equals(EventType.delete)));
    }
}