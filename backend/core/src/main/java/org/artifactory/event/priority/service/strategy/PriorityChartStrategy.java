package org.artifactory.event.priority.service.strategy;

import org.artifactory.event.priority.service.NodeEventPriorityChart;

/**
 * @author Uriah Levy
 * A {@link NodeEventPriorityChart} strategy. Returns the (possibly) modified chart, with priorities set.
 */
public interface PriorityChartStrategy {

    void apply(NodeEventPriorityChart priorityChart);
}
