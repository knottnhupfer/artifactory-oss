package org.artifactory.event.priority.service;

import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.artifactory.event.priority.service.strategy.DeletedAncestorsChartStrategy;
import org.artifactory.event.provider.EventProvider;
import org.artifactory.event.work.NodeEventOperator;
import org.artifactory.storage.event.NodeEvent;

import java.util.List;

/**
 * A business service that prioritizes event executions. The prioritized list determines the execution order of events.
 * The event list is a {@link NodeEventPriorityChart}, that is persisted as an atomic unit.
 * multiple {@link org.artifactory.event.priority.service.strategy.PriorityChartStrategy} can be used to mutate the
 * priorities of the events in the chart before it is persisted. For a concrete example, look at
 * {@link DeletedAncestorsChartStrategy}
 *
 * @author Uriah Levy
 */
public interface NodeEventPriorityService {

    List<PrioritizedNodeEvent> getCurrentPriorityBatch(String operatorID);

    /**
     * Creates a new chart, and saves it in the DB.
     */
    void createAndSaveEventPrioritiesIfNeeded(NodeEventOperator operator, EventProvider<? extends NodeEvent> eventProvider);

    /**
     * Checks whether a priority with the given ID still exists
     */
    boolean priorityExists(PrioritizedNodeEvent prioritizedNodeEvent);

    /**
     * Delete a single entry from the priorities table
     *
     * @param prioritizedNodeEvent - the event to delete
     */
    void deleteEventPriority(PrioritizedNodeEvent prioritizedNodeEvent);

    boolean priorityListIsEmpty(String operatorId);

    void saveRetryCount(PrioritizedNodeEvent priorityId);

    boolean isDistributed();
}
