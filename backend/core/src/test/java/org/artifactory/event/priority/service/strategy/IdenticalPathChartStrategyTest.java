package org.artifactory.event.priority.service.strategy;

import com.google.common.collect.ImmutableList;
import org.artifactory.event.priority.service.NodeEventPriorityChart;
import org.artifactory.event.priority.service.NodeEventPriorityChartHelper;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.storage.event.EventInfo;
import org.artifactory.storage.event.EventType;
import org.jfrog.common.ClockUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Uriah Levy
 */
@Test
public class IdenticalPathChartStrategyTest {

    @Mock
    private InternalDbService dbService;

    @BeforeMethod
    void beforeMethod() {
        MockitoAnnotations.initMocks(this);
    }

    public void testChartSortedByTimestamp() {
        // events
        final long now = ClockUtils.epochMillis();
        ImmutableList<EventInfo> events = ImmutableList.of(
                new EventInfo(now, EventType.create, "rpm-local/a/b/foo.txt"),
                new EventInfo(now + 1, EventType.delete, "rpm-local/a/b/foo.txt"),
                new EventInfo(now + 1, EventType.delete, "rpm-local/a/b/foo.txt"),
                new EventInfo(now - 1, EventType.props, "rpm-local/a/b/foo.txt"));
        // chart
        NodeEventPriorityChart chart = NodeEventPriorityChartHelper
                .chartFromEvents(events, "metadata-event-operator");
        when(dbService.nextId()).thenReturn(1L);
        // Base chart
        new BasePriorityChartStrategy().apply(chart);
        assertTrue(chart.getEvents().stream().allMatch(event -> event.getPriority() == 1));

        new IdenticalPathChartStrategy().apply(chart);
        assertEquals(chart.getEvents().get(0).getType(), EventType.props);
        assertEquals(chart.getEvents().get(1).getType(), EventType.create);
        assertEquals(chart.getEvents().get(2).getType(), EventType.delete);
        assertEquals(chart.getEvents().get(3).getType(), EventType.delete);
    }

    public void testChartUnmodifiedWithDifferentPaths() {
        // events
        final long now = ClockUtils.epochMillis();
        ImmutableList<EventInfo> events = ImmutableList.of(
                new EventInfo(now, EventType.create, "rpm-local/a/b/foo.txt"),
                new EventInfo(now + 1, EventType.delete, "rpm-local/a/b/bar.txt"),
                new EventInfo(now + 1, EventType.delete, "rpm-local/a/b/baz.txt"));
        // chart
        NodeEventPriorityChart chart = NodeEventPriorityChartHelper
                .chartFromEvents(events, "metadata-event-operator");
        when(dbService.nextId()).thenReturn(1L);
        // Base chart
        new BasePriorityChartStrategy().apply(chart);
        assertTrue(chart.getEvents().stream().allMatch(event -> event.getPriority() == 1));

        new IdenticalPathChartStrategy().apply(chart);
        assertEquals(chart.getEvents().get(0).getPriority(), 1);
        assertEquals(chart.getEvents().get(1).getPriority(), 1);
        assertEquals(chart.getEvents().get(2).getPriority(), 1);
    }

    public void testChartModifiedWithIdenticalPaths() {
        // events
        final long now = ClockUtils.epochMillis();
        ImmutableList<EventInfo> events = ImmutableList.of(
                new EventInfo(now, EventType.create, "rpm-local/a/b/foo.txt"),
                new EventInfo(now + 1, EventType.delete, "rpm-local/a/b/foo.txt"),
                new EventInfo(now + 1, EventType.delete, "rpm-local/a/b/foo.txt"),
                new EventInfo(now - 1, EventType.props, "rpm-local/a/b/foo.txt"));
        // chart
        NodeEventPriorityChart chart = NodeEventPriorityChartHelper
                .chartFromEvents(events, "metadata-event-operator");
        when(dbService.nextId()).thenReturn(1L);
        // Base chart
        new BasePriorityChartStrategy().apply(chart);
        assertTrue(chart.getEvents().stream().allMatch(event -> event.getPriority() == 1));

        new IdenticalPathChartStrategy().apply(chart);
        assertEquals(chart.getEvents().get(0).getPriority(), 1);
        assertEquals(chart.getEvents().get(0).getType(), EventType.props);
        assertEquals(chart.getEvents().get(1).getPriority(), 2);
        assertEquals(chart.getEvents().get(1).getType(), EventType.create);
        assertEquals(chart.getEvents().get(2).getPriority(), 3);
        assertEquals(chart.getEvents().get(2).getType(), EventType.delete);
        assertEquals(chart.getEvents().get(3).getPriority(), 4);
        assertEquals(chart.getEvents().get(3).getType(), EventType.delete);
    }

}