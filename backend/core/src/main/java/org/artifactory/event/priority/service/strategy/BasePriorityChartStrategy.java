package org.artifactory.event.priority.service.strategy;

import org.artifactory.event.priority.service.NodeEventPriorityChart;

/**
 * Base Priority Chart strategy. Inits the charted events with a DB ID, and sets the priority to zero.
 *
 * @author Uriah Levy
 */
public class BasePriorityChartStrategy implements PriorityChartStrategy {

    @Override
    public void apply(NodeEventPriorityChart priorityChart) {
        priorityChart.getEvents().forEach(nodeEvent -> {
            nodeEvent.setPriority(1);
            nodeEvent.setRetryCount(0);
        });

    }
}
