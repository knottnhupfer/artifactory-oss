package org.artifactory.event.priority.service.strategy;

import org.artifactory.event.priority.service.NodeEventPriorityChart;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Uriah Levy
 */
public class ExecutionPredicatePriorityChartStrategy implements PriorityChartStrategy {
    private static final Logger log = LoggerFactory.getLogger(ExecutionPredicatePriorityChartStrategy.class);
    private Predicate<PrioritizedNodeEvent> executionPredicate;

    public ExecutionPredicatePriorityChartStrategy(Predicate<PrioritizedNodeEvent> executionPredicate) {
        this.executionPredicate = executionPredicate;
    }

    @Override
    public void apply(NodeEventPriorityChart priorityChart) {
        List<PrioritizedNodeEvent> events = priorityChart.getEvents().stream()
                .filter(executionPredicate)
                .collect(Collectors.toList());
        log.debug("Execution predicate strategy filtered {} events", priorityChart.getEvents().size() - events.size());
        priorityChart.setEvents(events);
    }
}
