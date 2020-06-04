package org.artifactory.event.priority.service.strategy;


import org.artifactory.event.priority.service.NodeEventPriorityChart;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.artifactory.storage.db.fs.util.NodeUtils;
import org.artifactory.storage.event.EventType;
import org.artifactory.util.RepoPathUtils;

import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * When parallelized, delete events need to be prioritized to preserve data integrity / consistency.
 * This strategy prioritizes the events on the {@link NodeEventPriorityChart} based on the delete events it contains.
 * The prioritized list preserves the original event log timeline.
 *
 * Example #1:
 * +------+--------+-------------+-----------------+----------------+
 * | Time |  Type  |    Path     | Priority Before | Priority After |
 * +------+--------+-------------+-----------------+----------------+
 * | T    | DEL    | a/          |               1 |              1 |
 * | T+1  | DEL    | a/b/        |               1 |              2 |
 * | T+2  | CREATE | a/b/foo.txt |               1 |              3 |
 * | T+3  | CREATE | a/b/bar.txt |               1 |              3 |
 * +------+--------+-------------+-----------------+----------------+
 *
 * Example #2:
 * +------+--------+-------------+-----------------+----------------+
 * | Time |  Type  |    Path     | Priority Before | Priority After |
 * +------+--------+-------------+-----------------+----------------+
 * | T    | CREATE | a/b/foo.txt |               1 |              1 |
 * | T+1  | DEL    | a/b/        |               1 |              2 |
 * | T+2  | CREATE | a/b/bar.txt |               1 |              3 |
 * | T+3  | DEL    | a/          |               1 |              4 |
 * +------+--------+-------------+-----------------+----------------+
 *
 * @author Uriah Levy
 */
public class DeletedAncestorsChartStrategy implements PriorityChartStrategy {
    @Override
    public void apply(NodeEventPriorityChart priorityChart) {
        List<PrioritizedNodeEvent> deleteEvents = getEventsByPredicate(priorityChart.getEvents(), this::isDeleteEvent);
        deleteEvents.stream()
                .sorted(Comparator.comparingInt(this::getEventPathDepth).reversed()) // by-depth, bottom-up
                .forEach(deleteEvent -> rankDeletePredecessorsAndSuccessors(priorityChart, deleteEvent));
    }

    /**
     * Ranks (sets the priority) of the predecessor / successor events that are adjacent to this delete
     * event on the timeline
     */
    private void rankDeletePredecessorsAndSuccessors(NodeEventPriorityChart priorityChart,
            PrioritizedNodeEvent deleteEvent) {
        // Predecessors
        List<PrioritizedNodeEvent> children = getChildren(priorityChart, deleteEvent);
        OptionalInt maxChildrenPriority = getEventsByPredicate(children,
                child -> child.getTimestamp() < deleteEvent.getTimestamp())
                .stream()
                .mapToInt(PrioritizedNodeEvent::getPriority)
                .max();
        maxChildrenPriority.ifPresent(value -> deleteEvent.setPriority(value + 1));
        // Successors
        getEventsByPredicate(children, child -> child.getTimestamp() >= deleteEvent.getTimestamp())
                .forEach(child -> child.setPriority(deleteEvent.getPriority() + 1));
    }

    private List<PrioritizedNodeEvent> getChildren(NodeEventPriorityChart priorityChart,
            PrioritizedNodeEvent deleteEvent) {
        return getEventsByPredicate(priorityChart.getEvents(),
                event -> RepoPathUtils.isAncestorOf(deleteEvent.getRepoPath(), event.getRepoPath()));
    }

    private List<PrioritizedNodeEvent> getEventsByPredicate(List<PrioritizedNodeEvent> events,
            Predicate<PrioritizedNodeEvent> predicate) {
        return events.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    private boolean isDeleteEvent(PrioritizedNodeEvent event) {
        return event.getType().equals(EventType.delete);
    }

    private int getEventPathDepth(PrioritizedNodeEvent event) {
        return NodeUtils.getDepth(event.getRepoPath().getPath());
    }

}
