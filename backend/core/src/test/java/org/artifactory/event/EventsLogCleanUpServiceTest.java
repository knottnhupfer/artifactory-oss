package org.artifactory.event;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.schedule.TaskService;
import org.artifactory.storage.db.event.service.NodeEventCursorService;
import org.artifactory.storage.event.EventsService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

import static java.util.concurrent.TimeUnit.HOURS;
import static org.artifactory.common.ConstantValues.ageOfEventsLogEntriesToDiscardDays;
import static org.mockito.Mockito.*;

@Test
public class EventsLogCleanUpServiceTest extends ArtifactoryHomeBoundTest {

    @Mock
    private EventsService eventsService;

    @Mock
    private TaskService taskService;

    @Mock
    NodeEventCursorService nodeEventCursorService;

    private EventsLogCleanUpService eventsLogCleanUpService;

    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
        eventsLogCleanUpService = new EventsLogCleanUpService(taskService, eventsService, nodeEventCursorService);
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(ageOfEventsLogEntriesToDiscardDays.getPropertyName(), "0");
    }

    @Test
    public void testCleanUpLargeRange() {
        when(eventsService.getFirstEventTimestamp()).thenReturn(new Date().getTime() - HOURS.toMillis(10));
        when(eventsService.getEventsCount()).thenReturn(10L);
        eventsLogCleanUpService.cleanup();
        verify(eventsService, atLeast(3)).deleteRange(anyLong(), anyLong());
    }

    @Test
    public void testCleanUpLargeRangeOldestMarkerIsFromBeforeAllEvents() {
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(ageOfEventsLogEntriesToDiscardDays.getPropertyName(), "1");
        when(eventsService.getFirstEventTimestamp()).thenReturn(new Date().getTime() - HOURS.toMillis(10));
        when(nodeEventCursorService.oldestTimestampForEventLogOperator()).thenReturn((new Date().getTime() - HOURS.toMillis(11)));
        when(eventsService.getEventsCount()).thenReturn(10L);
        eventsLogCleanUpService.cleanup();
        verify(eventsService, times(0)).deleteRange(anyLong(), anyLong());
    }

    @Test
    public void testCleanUpSmallRange() {
        when(eventsService.getFirstEventTimestamp()).thenReturn(new Date().getTime() - HOURS.toMillis(1));
        when(eventsService.getEventsCount()).thenReturn(10L);
        eventsLogCleanUpService.cleanup();
        verify(eventsService, times(1)).deleteRange(anyLong(), anyLong());
    }

    @Test
    public void testCleanUpAllEventsAreTooYoung() {
        when(eventsService.getFirstEventTimestamp()).thenReturn(new Date().getTime() + 1000);
        when(eventsService.getEventsCount()).thenReturn(10L);
        eventsLogCleanUpService.cleanup();
        verify(eventsService, times(0)).deleteRange(anyLong(), anyLong());
    }
}