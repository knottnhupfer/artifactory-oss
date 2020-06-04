package org.artifactory.event.priority.service.strategy;

import com.google.common.collect.ImmutableList;
import org.artifactory.event.priority.service.NodeEventPriorityChart;
import org.artifactory.event.priority.service.NodeEventPriorityChartHelper;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.artifactory.metadata.service.MetadataEntityFacade;
import org.artifactory.metadata.service.MetadataEventService;
import org.artifactory.metadata.service.MetadataEventServiceImpl;
import org.artifactory.storage.event.EventInfo;
import org.artifactory.storage.event.EventType;
import org.jfrog.common.ClockUtils;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Uriah Levy
 */
@Test
public class ChainedPriorityStrategiesTest {

    public void testFilterAndDeduplication() {
        final long now = ClockUtils.epochMillis();
        EventInfo createGemEvent = new EventInfo(now, EventType.create, "gem-local/foo.gem");
        EventInfo propsGemEvent = new EventInfo(now, EventType.props, "gem-local/foo.gem");
        EventInfo nonGemEvent = new EventInfo(now, EventType.create, "gem-local/foo.gemspec.rz");
        EventInfo anotherGemEvent = new EventInfo(now, EventType.props, "gem-local/foo.gem");
        ImmutableList<EventInfo> events = ImmutableList.of(createGemEvent, propsGemEvent, nonGemEvent, anotherGemEvent);
        // chart
        NodeEventPriorityChart chart = NodeEventPriorityChartHelper
                .chartFromEvents(events, "metadata-event-operator");
        // Base chart
        new BasePriorityChartStrategy().apply(chart);
        assertTrue(chart.getEvents().stream().allMatch(event -> event.getPriority() == 1));

        MetadataEventService metadataEventService = Mockito.mock(MetadataEventService.class);
        when(metadataEventService.meetsExecutionCriteria(argThat(arg -> arg.getPath().endsWith(".gem"))))
                .thenReturn(true);
        // Operator execution predicate
        new ExecutionPredicatePriorityChartStrategy(event -> event.getPath().endsWith(".gem")).apply(chart);
        new IdenticalPathChartStrategy().apply(chart);
        new IdenticalTypeAndPathDeduplicationStrategy().apply(chart);

        assertEquals(2, chart.getEvents().size());
        assertEquals(chart.getEvents().get(0).getPath(), "gem-local/foo.gem");
        assertEquals(chart.getEvents().get(0).getType(), EventType.create);

        assertEquals(chart.getEvents().get(1).getPath(), "gem-local/foo.gem");
        assertEquals(chart.getEvents().get(1).getType(), EventType.props);
    }
}
