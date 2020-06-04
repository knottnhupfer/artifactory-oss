package org.artifactory.event.work;

import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;

import java.util.function.Predicate;

/**
 * @author Uriah Levy
 *
 * Defines a contract for an Event Operator. An Event Operator provides the bussiness logic for processing a node event.
 */
public interface NodeEventOperator {

    /**
     * Check whether this operator is enabled
     *
     * @return true if the operator is enabled
     */
    boolean isOperatorEnabled();

    /**
     * Process a single node event
     *
     * @param nodeEvent - a Prioritized event instance that contains the required information for processing the event.
     */
    void operateOnEvent(PrioritizedNodeEvent nodeEvent);

    /**
     * Determine whether the event should be processed by this operator
     *
     * @param nodeEvent - an event, candidate for processing
     * @return - true if the event meets the operator's criteria for execution
     */
    boolean meetsExecutionCriteria(PrioritizedNodeEvent nodeEvent);

    /**
     * Whether or not this operator should opt-in for squashing events by path and type. If this operator has no semantic
     * value in duplicate events that carry the same path and type, the {@link org.artifactory.event.priority.service.NodeEventPriorityService}
     * may apply a de-duplication strategy and remove them
     */
    boolean isSquashEventsByPathAndTypeCombination();

    /**
     * Dictates the dispatch-loop frequency of the {@link EventOperatorDispatcher}
     * @return the length of a each dispatcher sleep cycle, in seconds
     */
    long getDispatcherIntervalSecs();

    /**
     * True if this operator is during a migration process
     */
    boolean isMigrating();

    /**
     * Initialize the operator cursor in the DB, if needed.
     */
    void initOperatorCursor();

    String getOperatorId();

    int getNumberOfEventThreads();

    void onErrorThresholdReached();

    default Predicate<PrioritizedNodeEvent> getExecutionPredicate() {
        return x -> true;
    }
}
