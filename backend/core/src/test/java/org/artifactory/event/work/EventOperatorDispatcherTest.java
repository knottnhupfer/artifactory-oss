package org.artifactory.event.work;

import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.event.priority.service.NodeEventPriorityService;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.artifactory.storage.event.EventType;
import org.jfrog.common.ClockUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

/**
 * @author Uriah Levy
 */
@Test
public class EventOperatorDispatcherTest {

    @Mock
    NodeEventPriorityService priorityService;

    @Mock
    NodeEventOperator operator;

    @Mock
    HaCommonAddon haCommonAddon;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
    }

    public void testRun() {
        when(operator.getOperatorId()).thenReturn("metadata-event-operator");
        when(operator.getNumberOfEventThreads()).thenReturn(1);
        when(operator.isOperatorEnabled()).thenReturn(true);
        PrioritizedNodeEvent prioritizedNodeEvent = new PrioritizedNodeEvent(123L, "a/b/c", EventType.props,
                "metadata-event-operator", 0, ClockUtils.epochMillis(), 1, PrioritizedNodeEvent.EventStatus.PENDING);
        final List<PrioritizedNodeEvent> nodeEvents = Collections.singletonList(prioritizedNodeEvent);
        when(priorityService.getCurrentPriorityBatch("metadata-event-operator")).thenReturn(nodeEvents);
        EventOperatorDispatcher eventOperatorDispatcher = spy(
                new EventOperatorDispatcher(priorityService, null, operator,
                        haCommonAddon.getConflictsGuard("node-event-task-manager"),
                        operator.getNumberOfEventThreads()));
        when(priorityService.isDistributed()).thenReturn(true);
        doNothing().when(eventOperatorDispatcher).submitSweeper(any());
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(eventOperatorDispatcher);
        verify(eventOperatorDispatcher, timeout(TimeUnit.SECONDS.toMillis(10)).times(1)).submitSweeper(
                any());
    }
}