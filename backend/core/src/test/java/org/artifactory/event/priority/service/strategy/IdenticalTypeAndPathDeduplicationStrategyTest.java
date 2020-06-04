package org.artifactory.event.priority.service.strategy;

import com.google.common.collect.ImmutableList;
import org.artifactory.event.priority.service.NodeEventPriorityChart;
import org.artifactory.event.priority.service.NodeEventPriorityChartHelper;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.storage.event.EventInfo;
import org.artifactory.storage.event.EventType;
import org.jfrog.common.ClockUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Uriah Levy
 */
@Test
public class IdenticalTypeAndPathDeduplicationStrategyTest {

    @BeforeMethod
    void beforeMethod() {
        MockitoAnnotations.initMocks(this);
    }

    public void testFindNextBoundaryByTypeAndPath() {
        IdenticalTypeAndPathDeduplicationStrategy strategy = new IdenticalTypeAndPathDeduplicationStrategy();
        final long now = ClockUtils.epochMillis();
        PrioritizedNodeEvent prioritizedNodeEvent = getPrioritizedNodeEvent(now, "rpm-local/a/b/foo.txt",
                EventType.create);
        PrioritizedNodeEvent boundary = strategy.findNextBoundaryByTypeAndPath(
                prioritizedNodeEvent, ImmutableList.of(prioritizedNodeEvent));

        // First event is the boundary
        assertEquals(boundary, prioritizedNodeEvent);

        PrioritizedNodeEvent expectedBoundary = getPrioritizedNodeEvent(now + 10, "rpm-local/a/b/foo.txt", EventType.create);
        boundary = strategy.findNextBoundaryByTypeAndPath(
                prioritizedNodeEvent, ImmutableList.of(prioritizedNodeEvent, expectedBoundary));
        // Same path and type, new boundary
        assertEquals(boundary, expectedBoundary);

        boundary = strategy.findNextBoundaryByTypeAndPath(
                prioritizedNodeEvent, ImmutableList.of(prioritizedNodeEvent, expectedBoundary,
                        getPrioritizedNodeEvent(now + 10, "rpm-local/a/b/bar.txt", EventType.create)));
        // Different event path, boundary stays the same
        assertEquals(boundary, expectedBoundary);

        boundary = strategy.findNextBoundaryByTypeAndPath(
                prioritizedNodeEvent, ImmutableList.of(prioritizedNodeEvent, expectedBoundary,
                        getPrioritizedNodeEvent(now + 10, "rpm-local/a/b/foo.txt", EventType.props)));
        // Different event type, boundary stays the same
        assertEquals(boundary, expectedBoundary);
    }

    public void testGetSublistByBoundary() {
        IdenticalTypeAndPathDeduplicationStrategy strategy = new IdenticalTypeAndPathDeduplicationStrategy();
        final long now = ClockUtils.epochMillis();
        PrioritizedNodeEvent prioritizedNodeEvent = getPrioritizedNodeEvent(now, "rpm-local/a/b/foo.txt",
                EventType.create);
        PrioritizedNodeEvent boundary = getPrioritizedNodeEvent(now + 10, "rpm-local/a/b/foo.txt", EventType.create);
        List<PrioritizedNodeEvent> subListByBoundary = strategy
                .getSubListByBoundary(prioritizedNodeEvent, boundary, ImmutableList.of(prioritizedNodeEvent, boundary));
        assertEquals(subListByBoundary.size(), 2);
        assertEquals(subListByBoundary.get(0), prioritizedNodeEvent);
        assertEquals(subListByBoundary.get(1), boundary);

        subListByBoundary = strategy
                .getSubListByBoundary(prioritizedNodeEvent, boundary, ImmutableList.of(prioritizedNodeEvent));
        assertEquals(subListByBoundary.size(), 1);
        assertEquals(subListByBoundary.get(0), prioritizedNodeEvent);
    }

    public void testIdenticalPathsAndTypeDeduplicated() {
        // events
        final long now = ClockUtils.epochMillis();
        ImmutableList<EventInfo> events = ImmutableList.of(
                new EventInfo(now, EventType.create, "rpm-local/a/b/foo.txt"),
                new EventInfo(now + 1, EventType.create, "rpm-local/a/b/foo.txt"),
                new EventInfo(now + 2, EventType.create, "rpm-local/a/b/foo.txt"),
                new EventInfo(now + 3, EventType.delete, "rpm-local/a/b/foo.txt"),
                new EventInfo(now + 3, EventType.delete, "rpm-local/a/b/foo.txt"),
                new EventInfo(now - 1, EventType.props, "rpm-local/a/b/foo.txt"),
                new EventInfo(now - 1, EventType.props, "rpm-local/a/b/foo.txt"));
        // chart
        NodeEventPriorityChart chart = NodeEventPriorityChartHelper
                .chartFromEvents(events, "metadata-event-operator");
        // Base chart
        new BasePriorityChartStrategy().apply(chart);
        assertTrue(chart.getEvents().stream().allMatch(event -> event.getPriority() == 1));

        new IdenticalTypeAndPathDeduplicationStrategy().apply(chart);
        assertEquals(chart.getEvents().size(), 3);

        assertEquals(chart.getEvents().get(0).getType(), EventType.props);
        assertEquals(chart.getEvents().get(1).getType(), EventType.create);
        assertEquals(chart.getEvents().get(2).getType(), EventType.delete);
    }

    public void testOnlySequentialSquashAndOrderPreserved() {
        // events
        final long now = ClockUtils.epochMillis();
        ImmutableList<EventInfo> events = ImmutableList.of(
                new EventInfo(now, EventType.create, "rpm-local/a/b/foo.txt"),
                new EventInfo(now + 1, EventType.delete, "rpm-local/a/b/foo.txt"),
                new EventInfo(now + 2, EventType.create, "rpm-local/a/b/foo.txt"));
        // chart
        NodeEventPriorityChart chart = NodeEventPriorityChartHelper
                .chartFromEvents(events, "metadata-event-operator");
        // Base chart
        new BasePriorityChartStrategy().apply(chart);
        assertTrue(chart.getEvents().stream().allMatch(event -> event.getPriority() == 1));

        new IdenticalTypeAndPathDeduplicationStrategy().apply(chart);
        assertEquals(chart.getEvents().size(), 3);

        assertEquals(chart.getEvents().get(0).getType(), EventType.create);
        assertEquals(chart.getEvents().get(1).getType(), EventType.delete);
        assertEquals(chart.getEvents().get(2).getType(), EventType.create);
    }

    public void testIdenticalPathsAndTypeDeduplicatedUnmodifiedList() {
        // events
        final long now = ClockUtils.epochMillis();
        ImmutableList<EventInfo> events = ImmutableList.of(
                new EventInfo(now, EventType.create, "rpm-local/a/b/foo.txt"),
                new EventInfo(now + 1, EventType.delete, "rpm-local/a/b/foo.txt"),
                new EventInfo(now - 1, EventType.props, "rpm-local/a/b/foo.txt"));
        // chart
        NodeEventPriorityChart chart = NodeEventPriorityChartHelper
                .chartFromEvents(events, "metadata-event-operator");
        // Base chart
        new BasePriorityChartStrategy().apply(chart);
        assertTrue(chart.getEvents().stream().allMatch(event -> event.getPriority() == 1));

        new IdenticalTypeAndPathDeduplicationStrategy().apply(chart);
        assertEquals(chart.getEvents().size(), 3);

        assertEquals(chart.getEvents().get(0).getType(), EventType.props);
        assertEquals(chart.getEvents().get(1).getType(), EventType.create);
        assertEquals(chart.getEvents().get(2).getType(), EventType.delete);
    }

    public void testIdenticalPathsAndTypeDeduplicatedNoEvents() {
        // chart
        NodeEventPriorityChart chart = NodeEventPriorityChartHelper
                .chartFromEvents(new ArrayList<>(), "metadata-event-operator");
        // Base chart
        new BasePriorityChartStrategy().apply(chart);
        assertTrue(chart.getEvents().stream().allMatch(event -> event.getPriority() == 1));

        new IdenticalTypeAndPathDeduplicationStrategy().apply(chart);
        assertEquals(chart.getEvents().size(), 0);
    }

    public void testInvalidEventsInChart() {
        final long now = ClockUtils.epochMillis();
        ImmutableList<EventInfo> events = ImmutableList.of(
                new EventInfo(now, EventType.create, null),
                new EventInfo(now + 1, null, "rpm-local/a/b/foo.txt"),
                new EventInfo(now - 1, EventType.props, "rpm-local/a/b/foo.txt"));
        // chart
        NodeEventPriorityChart chart = NodeEventPriorityChartHelper
                .chartFromEvents(events, "metadata-event-operator");
        // Base chart
        new BasePriorityChartStrategy().apply(chart);
        assertTrue(chart.getEvents().stream().allMatch(event -> event.getPriority() == 1));

        new IdenticalTypeAndPathDeduplicationStrategy().apply(chart);
        // Original size preserved (strategy escaped)
        assertEquals(chart.getEvents().size(), 3);
    }

    private PrioritizedNodeEvent getPrioritizedNodeEvent(long timestamp, String path, EventType type) {
        return new PrioritizedNodeEvent(new EventInfo(timestamp, type, path), "metadata-operator");
    }
}