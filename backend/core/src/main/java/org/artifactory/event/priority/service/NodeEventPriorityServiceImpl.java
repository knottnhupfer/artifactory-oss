package org.artifactory.event.priority.service;

import com.google.common.collect.ImmutableList;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.artifactory.event.priority.service.strategy.*;
import org.artifactory.event.provider.EventProvider;
import org.artifactory.event.work.NodeEventOperator;
import org.artifactory.storage.db.event.model.NodeEventCursor;
import org.artifactory.storage.db.event.service.NodeEventCursorService;
import org.artifactory.storage.event.NodeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author Uriah Levy
 */
@Service
public class NodeEventPriorityServiceImpl implements NodeEventPriorityService {
    public static final int MAX_RETRIES = 5;
    private static final Logger log = LoggerFactory.getLogger(NodeEventPriorityServiceImpl.class);
    private NodeEventCursorService eventCursorService;
    private NodeEventPriorityStorageService eventPriorityStorageService;

    private List<PriorityChartStrategy> basePriorityChartStrategies;

    @Autowired
    public NodeEventPriorityServiceImpl(NodeEventCursorService eventCursorService,
            NodeEventPriorityStorageService eventPriorityStorageService) {
        this.eventCursorService = eventCursorService;
        this.eventPriorityStorageService = eventPriorityStorageService;
        basePriorityChartStrategies = ImmutableList
                .of(new BasePriorityChartStrategy(), new IdenticalPathChartStrategy());
    }

    @Override
    public List<PrioritizedNodeEvent> getCurrentPriorityBatch(String operatorID) {
        return eventPriorityStorageService.getCurrentPriorityBatch(operatorID);
    }

    @Override
    public void createAndSaveEventPrioritiesIfNeeded(NodeEventOperator operator,
            EventProvider<? extends NodeEvent> eventProvider) {
        NodeEventPriorityChart priorityChart = NodeEventPriorityChartHelper
                .chartFromEvents(getNextBatch(operator, eventProvider), operator.getOperatorId());
        if (!priorityChart.isEmpty()) {
            log.debug("Creating new event priority list for operator '{}'", operator.getOperatorId());
            final long finalTimestamp = priorityChart.getFinalTimestamp();
            applyPriorityStrategies(priorityChart, operator);
            saveChartAndUpdateCursor(operator, priorityChart, finalTimestamp);
        }
    }

    @Override
    public boolean priorityExists(PrioritizedNodeEvent prioritizedNodeEvent) {
        return eventPriorityStorageService.priorityExists(prioritizedNodeEvent);
    }

    @Override
    public void deleteEventPriority(PrioritizedNodeEvent prioritizedNodeEvent) {
        eventPriorityStorageService.deleteEventPriority(prioritizedNodeEvent);
    }

    @Override
    public boolean priorityListIsEmpty(String operatorId) {
        return eventPriorityStorageService.priorityListIsEmpty(operatorId);
    }

    @Override
    public void saveRetryCount(PrioritizedNodeEvent prioritizedNodeEvent) {
        eventPriorityStorageService.saveRetryCount(prioritizedNodeEvent);
    }

    @Override
    public boolean isDistributed() {
        return eventPriorityStorageService.isDistributed();
    }

    private <T extends NodeEvent> List<T> getNextBatch(NodeEventOperator operator, EventProvider<T> eventProvider) {
        NodeEventCursor lastOperatorCursor = eventCursorService.cursorForOperator(operator.getOperatorId());
        if (lastOperatorCursor != null) {
            return eventProvider.provideNextBatch(lastOperatorCursor);
        }
        return Collections.emptyList();
    }

    private void applyPriorityStrategies(NodeEventPriorityChart nodeEventPriorityChart, NodeEventOperator operator) {
        // Operator execution predicate
        ExecutionPredicatePriorityChartStrategy operatorExecutionStrategy = new ExecutionPredicatePriorityChartStrategy(
                operator.getExecutionPredicate());
        operatorExecutionStrategy.apply(nodeEventPriorityChart);
        // Opt-in squash by path and type
        if (operator.isSquashEventsByPathAndTypeCombination()) {
            new IdenticalTypeAndPathDeduplicationStrategy().apply(nodeEventPriorityChart);
        }
        basePriorityChartStrategies.forEach(strategy -> strategy.apply(nodeEventPriorityChart));
    }

    private void saveChartAndUpdateCursor(NodeEventOperator operator,
            NodeEventPriorityChart nodeEventPriorityChart, long finalTimestamp) {
        if (!nodeEventPriorityChart.isEmpty()) {
            eventPriorityStorageService.savePriorityChart(nodeEventPriorityChart);
        }
        eventCursorService.updateCursor(operator.getOperatorId(), finalTimestamp);
    }
}
