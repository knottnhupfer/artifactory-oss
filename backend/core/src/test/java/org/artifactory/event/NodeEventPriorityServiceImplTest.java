package org.artifactory.event;

import com.google.common.collect.ImmutableList;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.event.priority.service.NodeEventPriorityServiceImpl;
import org.artifactory.event.priority.service.NodeEventPriorityStorageService;
import org.artifactory.event.provider.EventLogTableEventProvider;
import org.artifactory.event.provider.NodesTableEventProvider;
import org.artifactory.event.work.NodeEventOperator;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.storage.db.event.entity.NodeEventCursorType;
import org.artifactory.storage.db.event.model.NodeEventCursor;
import org.artifactory.storage.db.event.service.NodeEventCursorService;
import org.artifactory.storage.event.EventInfo;
import org.artifactory.storage.event.EventType;
import org.artifactory.storage.event.EventsService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.common.ClockUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

/**
 * @author Uriah Levy
 */
@Test
public class NodeEventPriorityServiceImplTest extends ArtifactoryHomeBoundTest {

    @Mock
    private EventsService eventService;

    @Mock
    private NodeEventCursorService cursorService;

    @Mock
    private InternalDbService dbService;

    @Mock
    private NodeEventPriorityStorageService eventPriorityStorageService;

    @Mock
    AqlEagerResult<AqlItem> result;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
    }

    public void testCreateEventPrioritiesFromEventLog() {
        NodeEventOperator eventOperator = mock(NodeEventOperator.class);
        EventLogTableEventProvider eventProvider = new EventLogTableEventProvider(eventService);
        String operatorId = "metadata-event-operator";
        NodeEventPriorityServiceImpl nodeEventPriorityService = spy(
                new NodeEventPriorityServiceImpl(cursorService, eventPriorityStorageService));
        NodeEventCursor currentCursor = new NodeEventCursor(operatorId, ClockUtils.epochMillis(), NodeEventCursorType.METADATA_EVENTS);
        ImmutableList<EventInfo> events = ImmutableList
                .of(new EventInfo(ClockUtils.epochMillis() - TimeUnit.MINUTES.toMillis(5), EventType.props, "a/b/c/foo.rpm"));

        when(eventOperator.getOperatorId()).thenReturn(operatorId);
        when(eventOperator.getExecutionPredicate()).thenReturn(x -> true);
        when(eventOperator.meetsExecutionCriteria(any())).thenReturn(true);
        when(cursorService.cursorForOperator(operatorId)).thenReturn(
                currentCursor);
        when(eventService.getEventsSince(currentCursor.getEventMarker()))
                .thenReturn(events.stream());
        when(dbService.nextId()).thenReturn(1L);
        doNothing().when(eventPriorityStorageService).savePriorityChart(any());

        nodeEventPriorityService.createAndSaveEventPrioritiesIfNeeded(eventOperator, eventProvider);
        verify(eventPriorityStorageService).savePriorityChart(argThat(argument ->
                argument.getEvents().stream()
                        .anyMatch(event -> event.getPath().equals("a/b/c/foo.rpm") && event.getPriorityId() == 0 &&
                                event.getPriority() == 1) && argument.getEvents().size() == 1));
    }

    public void testUnstableMarginEventsExcluded() {
        NodeEventOperator eventOperator = mock(NodeEventOperator.class);
        EventLogTableEventProvider eventProvider = new EventLogTableEventProvider(eventService);
        String operatorId = "metadata-event-operator";
        NodeEventPriorityServiceImpl nodeEventPriorityService = spy(
                new NodeEventPriorityServiceImpl(cursorService, eventPriorityStorageService));
        NodeEventCursor currentCursor = new NodeEventCursor(operatorId, ClockUtils.epochMillis(), NodeEventCursorType.METADATA_EVENTS);
        ImmutableList<EventInfo> events = ImmutableList
                .of(new EventInfo(ClockUtils.epochMillis(), EventType.props, "a/b/c/foo.rpm"));

        when(eventOperator.getOperatorId()).thenReturn(operatorId);
        when(eventOperator.getExecutionPredicate()).thenReturn(x -> true);
        when(eventOperator.meetsExecutionCriteria(any())).thenReturn(true);
        when(cursorService.cursorForOperator(operatorId)).thenReturn(
                currentCursor);
        when(eventService.getEventsSince(currentCursor.getEventMarker()))
                .thenReturn(events.stream());
        when(dbService.nextId()).thenReturn(1L);
        doNothing().when(eventPriorityStorageService).savePriorityChart(any());

        nodeEventPriorityService.createAndSaveEventPrioritiesIfNeeded(eventOperator, eventProvider);
        // Within the unstable margin
        verify(eventPriorityStorageService, times(0)).saveRetryCount(any());
    }

    public void testCreateEventPrioritiesFromNodesTable() {
        NodeEventOperator eventOperator = mock(NodeEventOperator.class);
        final AqlService aqlService = mock(AqlService.class);
        String operatorId = "metadata-event-operator";
        NodeEventPriorityServiceImpl nodeEventPriorityService = spy(
                new NodeEventPriorityServiceImpl(cursorService, eventPriorityStorageService));
        final NodesTableEventProvider nodesTableEventProvider = new NodesTableEventProvider(aqlService, null, () -> {
        }, a -> new AqlApiItem(false));
        NodeEventCursor eventCursor = new NodeEventCursor(operatorId, ClockUtils.epochMillis(), NodeEventCursorType.METADATA_EVENTS);

        AqlItem aqlItem = mock(AqlItem.class);
        when(aqlItem.getCreated()).thenReturn(new Date(ClockUtils.epochMillis()));
        when(aqlItem.getPath()).thenReturn("a/b/c");
        when(aqlItem.getRepo()).thenReturn("rpm-local");
        when(aqlItem.getName()).thenReturn("foo.rpm");
        when(result.getResults()).thenReturn(ImmutableList.of(aqlItem));
        when(aqlService.executeQueryEager(any(AqlApiItem.class))).thenReturn(result);
        when(eventOperator.getOperatorId()).thenReturn(operatorId);
        when(eventOperator.getExecutionPredicate()).thenReturn(x -> true);
        when(eventOperator.meetsExecutionCriteria(any())).thenReturn(true);
        when(cursorService.cursorForOperator(operatorId)).thenReturn(
                eventCursor);
        when(dbService.nextId()).thenReturn(1L);
        doNothing().when(eventPriorityStorageService).savePriorityChart(any());

        nodeEventPriorityService.createAndSaveEventPrioritiesIfNeeded(eventOperator, nodesTableEventProvider);
        verify(eventPriorityStorageService).savePriorityChart(argThat(argument -> argument.getEvents().stream()
                .anyMatch(event -> event.getPath().equals("rpm-local/a/b/c/foo.rpm") && event.getPriorityId() == 0 &&
                        event.getPriority() == 1)));
    }

}