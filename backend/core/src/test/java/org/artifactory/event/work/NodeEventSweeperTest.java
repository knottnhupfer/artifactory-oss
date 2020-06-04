package org.artifactory.event.work;

import org.artifactory.event.priority.service.NodeEventPriorityService;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.artifactory.storage.event.EventType;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.common.ClockUtils;
import org.jfrog.metadata.client.exception.MetadataClientException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Uriah Levy
 */
@Test
public class NodeEventSweeperTest extends ArtifactoryHomeBoundTest {

    @Mock
    NodeEventPriorityService priorityService;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("unchecked")
    public void testRun() {
        PrioritizedNodeEvent prioritizedNodeEvent = new PrioritizedNodeEvent(123L, "a/b/c", EventType.props,
                "metadata-event-operator", 0, ClockUtils.epochMillis(), 1, PrioritizedNodeEvent.EventStatus.PENDING);
        final List<PrioritizedNodeEvent> nodeEvents = Collections.singletonList(prioritizedNodeEvent);
        NodeEventOperator operator = mock(NodeEventOperator.class);
        when(operator.getOperatorId()).thenReturn("metadata-event-operator");
        when(operator.isOperatorEnabled()).thenReturn(true);
        when(priorityService.getCurrentPriorityBatch("metadata-event-operator"))
                .thenReturn(nodeEvents, Collections.emptyList());
        EventOperatorDispatcher dispatcher = mock(EventOperatorDispatcher.class);
        NodeEventSweeper nodeEventSweeper = spy(new NodeEventSweeper(priorityService, operator,
                dispatcher, nodeEvents));
        doNothing().when(nodeEventSweeper).tryToProcessEvent(nodeEvents.get(0));
        nodeEventSweeper.run();
        verify(dispatcher).sweeperDone();
        verify(nodeEventSweeper).tryToProcessEvent(nodeEvents.get(0));
    }

    public void testRunWithProcessedEvent() {
        PrioritizedNodeEvent prioritizedNodeEvent = new PrioritizedNodeEvent(123L, "a/b/c", EventType.props,
                "metadata-event-operator", 0, ClockUtils.epochMillis(), 1, PrioritizedNodeEvent.EventStatus.PROCESSED);
        final List<PrioritizedNodeEvent> nodeEvents = Collections.singletonList(prioritizedNodeEvent);
        NodeEventOperator operator = mock(NodeEventOperator.class);
        when(operator.getOperatorId()).thenReturn("metadata-event-operator");
        when(operator.isOperatorEnabled()).thenReturn(true);
        EventOperatorDispatcher dispatcher = mock(EventOperatorDispatcher.class);
        NodeEventSweeper nodeEventSweeper = spy(new NodeEventSweeper(priorityService, operator,
                dispatcher, nodeEvents));
        nodeEventSweeper.run();
        verify(dispatcher).sweeperDone();
        verify(nodeEventSweeper, times(0)).tryToProcessEvent(nodeEvents.get(0));
    }

    @SuppressWarnings("unchecked")
    public void testRetries() throws InterruptedException {
        PrioritizedNodeEvent prioritizedNodeEvent = new PrioritizedNodeEvent(123L, "a/b/c", EventType.props,
                "metadata-event-operator", 0, ClockUtils.epochMillis(), 0, PrioritizedNodeEvent.EventStatus.PENDING);
        final List<PrioritizedNodeEvent> nodeEvents = Collections.singletonList(prioritizedNodeEvent);
        NodeEventOperator operator = mock(NodeEventOperator.class);
        when(operator.getOperatorId()).thenReturn("metadata-event-operator");
        when(operator.isOperatorEnabled()).thenReturn(true);
        when(priorityService.getCurrentPriorityBatch("metadata-event-operator"))
                .thenReturn(nodeEvents, Collections.emptyList());
        Semaphore semaphore = new Semaphore(5);
        semaphore.acquire();
        assertEquals(semaphore.availablePermits(), 4);
        EventOperatorDispatcher dispatcher = mock(EventOperatorDispatcher.class);
        NodeEventSweeper nodeEventSweeper = spy(new NodeEventSweeper(priorityService, operator,
                dispatcher, nodeEvents));
        doAnswer(a -> {
            nodeEventSweeper.executeWithRetry(() -> nodeEventSweeper.doProcessEvent(prioritizedNodeEvent),
                    prioritizedNodeEvent);
            return null;
        }).when(dispatcher).runExclusive(anyString(), any());
        doThrow(MetadataClientException.class).when(nodeEventSweeper).doProcessEvent(prioritizedNodeEvent);
        nodeEventSweeper.run();
        verify(nodeEventSweeper, times(5)).doProcessEvent(prioritizedNodeEvent);
        verify(dispatcher, times(5)).batchError();
        verify(priorityService).saveRetryCount(any());
    }
}