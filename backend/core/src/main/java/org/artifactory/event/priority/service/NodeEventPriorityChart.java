package org.artifactory.event.priority.service;

import com.google.common.collect.Iterables;
import lombok.Data;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Uriah Levy
 */
@Data
public class NodeEventPriorityChart {
    List<PrioritizedNodeEvent> events;

    public NodeEventPriorityChart(List<PrioritizedNodeEvent> prioritizedNodeEvents) {
        this.events = prioritizedNodeEvents.stream()
                .sorted(Comparator.comparingLong(PrioritizedNodeEvent::getTimestamp))
                .collect(Collectors.toList());
    }

    boolean isEmpty() {
        return events.isEmpty();
    }

    long getFinalTimestamp() {
        if (!isEmpty()) {
            return Iterables.getLast(events).getTimestamp();
        }
        throw new UnsupportedOperationException("Unable to get the last timestamp from an empty chart.");
    }
}
