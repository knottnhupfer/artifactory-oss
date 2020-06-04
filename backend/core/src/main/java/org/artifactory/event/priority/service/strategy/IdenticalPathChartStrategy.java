package org.artifactory.event.priority.service.strategy;

import org.artifactory.event.priority.service.NodeEventPriorityChart;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Prioritizes events with identical paths (same artifact/directory) based on their timestamp.
 * Example:
 * +------+--------+-------------+-----------------+----------------+
 * | Time |  Type  |    Path     | Priority Before | Priority After |
 * +------+--------+-------------+-----------------+----------------+
 * | T    | DEL    | a/b/foo.txt |               1 |              1 |
 * | T+1  | DEL    | a/b/foo.txt |               1 |              2 |
 * | T+2  | PROPS  | a/b/foo.txt |               1 |              3 |
 * | T+3  | CREATE | a/b/foo.txt |               1 |              4 |
 * +------+--------+-------------+-----------------+----------------+
 *
 * @author Uriah Levy
 */
public class IdenticalPathChartStrategy implements PriorityChartStrategy {
    @Override
    public void apply(NodeEventPriorityChart priorityChart) {
        Map<String, List<PrioritizedNodeEvent>> pathsToEvents = priorityChart.getEvents()
                .stream()
                .sorted(Comparator.comparingLong(PrioritizedNodeEvent::getTimestamp))
                .collect(Collectors.groupingBy(PrioritizedNodeEvent::getPath));
        pathsToEvents.values().forEach(events -> {
            AtomicInteger priority = new AtomicInteger(0);
            events.forEach(event -> event.setPriority(event.getPriority() + priority.getAndIncrement()));
        });
    }
}
