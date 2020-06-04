package org.artifactory.event.work;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.event.priority.service.NodeEventPriorityService;
import org.artifactory.event.provider.EventProvider;
import org.artifactory.event.provider.FiniteEventProvider;
import org.artifactory.event.provider.NodesTableEvent;
import org.artifactory.storage.event.EventInfo;
import org.artifactory.storage.event.NodeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Uriah Levy
 * An entrypoint for event management. Can also be used to start off migrations.
 */
@Component
public class NodeEventTaskManagerImpl implements NodeEventTaskManager {
    private static final Logger log = LoggerFactory.getLogger(NodeEventTaskManagerImpl.class);

    private NodeEventPriorityService nodeEventPriorityService;
    private AddonsManager addonsManager;
    private Map<String, EventOperatorDispatcher> operatorToDispatcherThread = new ConcurrentHashMap<>();

    @Autowired
    public NodeEventTaskManagerImpl(NodeEventPriorityService nodeEventPriorityService, AddonsManager addonsManager) {
        this.addonsManager = addonsManager;
        this.nodeEventPriorityService = nodeEventPriorityService;
    }

    @Override
    public void startWithInfiniteProvider(NodeEventOperator operator, EventProvider<EventInfo> eventProvider) {
        EventOperatorDispatcher operatorDispatcher = getOperatorDispatcher(operator, eventProvider, true);
        operatorDispatcher.setName("event-dispatcher-" + operator.getOperatorId());
        operatorDispatcher.start();
        log.info("Event management started on behalf of Event Operator with ID '{}'", operator.getOperatorId());
    }

    @Override
    public void startWithFiniteProvider(NodeEventOperator operator,
            FiniteEventProvider<NodesTableEvent> eventProvider) {
        EventOperatorDispatcher operatorDispatcher = getOperatorDispatcher(operator, eventProvider, false);
        operatorDispatcher.setName("migration-event-dispatcher-" + operator.getOperatorId());
        operatorDispatcher.start();
        log.info("Background migration started on behalf of Event Operator with ID '{}'", operator.getOperatorId());
    }

    @Override
    public void stopDispatcherThread(String operatorId) {
        try {
            EventOperatorDispatcher dispatcher = operatorToDispatcherThread.get(operatorId);
            log.info("Removing dispatcher thread with name '{}'", dispatcher.getName());
            dispatcher.stopDispatcher();
        } finally {
            operatorToDispatcherThread.remove(operatorId);
        }
    }

    private EventOperatorDispatcher getOperatorDispatcher(NodeEventOperator operator,
            EventProvider<? extends NodeEvent> provider, boolean infinite) {
        String operatorId = operator.getOperatorId();
        if (operatorToDispatcherThread.putIfAbsent(operatorId, newDispatcherThread(operator, provider, infinite)) != null) {
            throw new IllegalStateException("Operator with ID '" + operatorId + "' already exists.");
        }
        return operatorToDispatcherThread.get(operatorId);
    }

    private EventOperatorDispatcher newDispatcherThread(NodeEventOperator operator,
            EventProvider<? extends NodeEvent> eventProvider, boolean infinite) {
        EventOperatorDispatcher dispatcherThread = new EventOperatorDispatcher(nodeEventPriorityService, eventProvider,
                operator,
                addonsManager.addonByType(HaCommonAddon.class).getConflictsGuard("node-event-task-manager"),
                operator.getNumberOfEventThreads());
        dispatcherThread.setDaemon(true);
        if (infinite) {
            dispatcherThread.setName("event-dispatcher-" + operator.getOperatorId());
        } else {
            dispatcherThread.setName("migration-event-dispatcher-" + operator.getOperatorId());
        }
        return dispatcherThread;
    }
}
