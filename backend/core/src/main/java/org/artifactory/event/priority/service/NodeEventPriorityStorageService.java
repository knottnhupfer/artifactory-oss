package org.artifactory.event.priority.service;

import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;

import java.util.List;

/**
 * @author Uriah Levy
 */
public interface NodeEventPriorityStorageService {

    void savePriorityChart(NodeEventPriorityChart nodeEventPriorityChart);

    boolean priorityExists(PrioritizedNodeEvent priorityId);

    /**
     * Get a batch of the prioritized events that correspond to the minimal priority available (i.e the current workable batch).
     */
    List<PrioritizedNodeEvent> getCurrentPriorityBatch(String operatorId);

    void deleteEventPriority(PrioritizedNodeEvent prioritizedNodeEvent);

    boolean priorityListIsEmpty(String operatorId);

    void saveRetryCount(PrioritizedNodeEvent prioritizedNodeEvent);

    boolean isDistributed();
}