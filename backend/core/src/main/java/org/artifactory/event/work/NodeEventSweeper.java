package org.artifactory.event.work;

import com.google.common.util.concurrent.MoreExecutors;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.event.priority.service.NodeEventPriorityService;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.jfrog.common.ExecutionUtils;
import org.jfrog.common.RetryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.artifactory.event.priority.service.NodeEventPriorityServiceImpl.MAX_RETRIES;

/**
 * @author Uriah Levy
 * An event worker. Invokes events via the {@link NodeEventOperator}
 */
class NodeEventSweeper implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(NodeEventSweeper.class);
    private NodeEventPriorityService nodeEventPriorityService;
    private EventOperatorDispatcher dispatcher;
    private NodeEventOperator operator;
    private List<PrioritizedNodeEvent> eventList;

    NodeEventSweeper(NodeEventPriorityService priorityService, NodeEventOperator operator,
            EventOperatorDispatcher dispatcher, List<PrioritizedNodeEvent> eventList) {
        this.nodeEventPriorityService = priorityService;
        this.operator = operator;
        this.dispatcher = dispatcher;
        this.eventList = eventList;
    }

    @Override
    public void run() {
        try {
            if (!eventList.isEmpty()) {
                eventList.stream()
                        .filter(PrioritizedNodeEvent::isPending)
                        .forEach(this::tryToProcessEvent);
            }
        } finally {
            notifyDoneAndReleaseSemaphore();
        }
    }

    void tryToProcessEvent(PrioritizedNodeEvent prioritizedEvent) {
        if (operator.isOperatorEnabled() &&
                prioritizedEvent.isPending()) { // Minor optimization to spare a few locking related queries
            dispatcher.runExclusive(prioritizedEvent.getPath(), () -> {
                if (shouldExecute(prioritizedEvent)) { // Double check, the status may have changed
                    executeWithRetry(() -> doProcessEvent(prioritizedEvent), prioritizedEvent);
                }
            });
        }
    }

    private boolean shouldExecute(PrioritizedNodeEvent prioritizedEvent) {
        if (ArtifactoryHome.get().isHaConfigured() &&
                !nodeEventPriorityService.priorityExists(prioritizedEvent)) {
            // Check for priority existence, don't handle leftover events handled by a cluster peer
            prioritizedEvent.processed();
            return false;
        }
        return operator.isOperatorEnabled() && prioritizedEvent.isPending();
    }

    void doProcessEvent(PrioritizedNodeEvent prioritizedEvent) {
        operator.operateOnEvent(prioritizedEvent);
        nodeProcessed(prioritizedEvent);
    }

    void executeWithRetry(Runnable execution, PrioritizedNodeEvent event) {
        ExecutionUtils.RetryOptions retryOptions = getRetryOptions();
        ExecutionUtils.retry(() -> {
            try {
                execution.run();
                return null;
            } catch (Exception e) {
                log.error("Unable to execute event on path {} due to: {}", event.getPath(), e.getMessage());
                log.debug("", e);
                handleRetryableError(event, retryOptions, e);
                return null;
            }
        }, retryOptions, MoreExecutors.newDirectExecutorService());
    }

    private void handleRetryableError(PrioritizedNodeEvent event, ExecutionUtils.RetryOptions retryOptions,
            Exception e) throws RetryException {
        dispatcher.batchError();
        event.setRetryCount(event.getRetryCount() + 1);
        if (event.getRetryCount() >= retryOptions.getNumberOfRetries()) {
            nodeEventPriorityService.saveRetryCount(event);
        }
        if (event.getRetryCount() >= MAX_RETRIES) {
            event.processed();
        }
        throw new RetryException("Unable to execute event on path " + event.getPath(), e);
    }

    private void nodeProcessed(PrioritizedNodeEvent nodeEvent) {
        log.debug("Node processing done on event with path '{}', and type '{}'", nodeEvent.getPath(),
                nodeEvent.getType());
        nodeEventPriorityService.deleteEventPriority(nodeEvent);
        nodeEvent.processed();
    }

    private void notifyDoneAndReleaseSemaphore() {
        dispatcher.sweeperDone();
    }

    private ExecutionUtils.RetryOptions getRetryOptions() {
        return ExecutionUtils.RetryOptions.builder()
                .backoffMaxDelay(6000)
                .exponentialBackoffMultiplier(3)
                .numberOfRetries(MAX_RETRIES)
                .timeout(1500)
                .build();
    }
}
