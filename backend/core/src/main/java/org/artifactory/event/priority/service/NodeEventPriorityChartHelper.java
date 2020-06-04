package org.artifactory.event.priority.service;

import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.artifactory.storage.event.NodeEvent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Uriah Levy
 */
public class NodeEventPriorityChartHelper {

    private NodeEventPriorityChartHelper () {
        // helper
    }

    public static NodeEventPriorityChart chartFromEvents(List<? extends NodeEvent> eventList, String operatorId) {
        List<PrioritizedNodeEvent> prioritizedNodeEvents = eventList.stream()
                .map(event -> new PrioritizedNodeEvent(event, operatorId))
                .collect(Collectors.toList());
        return new NodeEventPriorityChart(prioritizedNodeEvents);
    }
}
