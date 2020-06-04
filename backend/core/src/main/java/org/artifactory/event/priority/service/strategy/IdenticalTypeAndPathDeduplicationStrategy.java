package org.artifactory.event.priority.service.strategy;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.event.priority.service.NodeEventPriorityChart;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns an optionally modified chart that squashes sequential events of the same path and type into one event
 *
 * @author Uriah Levy
 */
public class IdenticalTypeAndPathDeduplicationStrategy implements PriorityChartStrategy {
    private static final Logger log = LoggerFactory.getLogger(IdenticalTypeAndPathDeduplicationStrategy.class);
    @Override
    public void apply(NodeEventPriorityChart priorityChart) {
        if (!validChart(priorityChart)) {
            // illegal state for strategy processing
            log.debug("Unable to apply deduplication strategy on chart, at least one invalid event was found");
            return;
        }
        List<PrioritizedNodeEvent> deduplicatedList = new ArrayList<>();
        PrioritizedNodeEvent currentBoundary = null;
        for (PrioritizedNodeEvent event : priorityChart.getEvents()) {
            if (shouldSkipCurrentEvent(priorityChart.getEvents(), currentBoundary, event)) {
                // Within the bounds of myself and the previous boundary, skip
                continue;
            }
            // Squash only sequential events. First, find the boundary of the current event
            currentBoundary = findNextBoundaryByTypeAndPath(event, getRemainingList(event, priorityChart.getEvents()));
            if (currentBoundary != null) {
                deduplicatedList.add(event);
            }
        }
        log.debug("Squashed {} events", priorityChart.getEvents().size() - deduplicatedList.size());
        priorityChart.setEvents(deduplicatedList);
    }

    private boolean validChart(NodeEventPriorityChart priorityChart) {
        return priorityChart.getEvents()
                .stream()
                .noneMatch(this::isInvalidEvent);
    }

    private boolean shouldSkipCurrentEvent(List<PrioritizedNodeEvent> fullEventList,
            PrioritizedNodeEvent currentBoundary,
            PrioritizedNodeEvent event) {
        return currentBoundary != null &&
                (fullEventList.lastIndexOf(currentBoundary) > fullEventList.indexOf(event) || currentBoundary == event);
    }

    private List<PrioritizedNodeEvent> getRemainingList(PrioritizedNodeEvent event,
            List<PrioritizedNodeEvent> fullEventList) {
        int from = fullEventList.indexOf(event);
        return fullEventList.subList(from, fullEventList.size());
    }

    PrioritizedNodeEvent findNextBoundaryByTypeAndPath(PrioritizedNodeEvent event, List<PrioritizedNodeEvent> events) {
        // Find the last event which is not me, and has the same type and path as me
        PrioritizedNodeEvent lastBoundary = null;
        for (PrioritizedNodeEvent otherEvent : events) {
            if (otherEvent.getType().equals(event.getType()) && otherEvent.getPath().equals(event.getPath())) {
                lastBoundary = otherEvent;
            } else {
                return lastBoundary;
            }
        }
        return lastBoundary;
    }

    List<PrioritizedNodeEvent> getSubListByBoundary(PrioritizedNodeEvent firstEvent,
            PrioritizedNodeEvent boundary, List<PrioritizedNodeEvent> events) {
        int lastIndexOfBoundary = events.lastIndexOf(boundary);
        if (lastIndexOfBoundary == -1) {
            // If the boundary is not found, the first event is the boundary
            return Lists.newArrayList(firstEvent);
        }
        return events.subList(events.indexOf(firstEvent), lastIndexOfBoundary + 1);
    }

    private boolean isInvalidEvent(PrioritizedNodeEvent event) {
        return StringUtils.isBlank(event.getPath()) || event.getType() == null;
    }
}
