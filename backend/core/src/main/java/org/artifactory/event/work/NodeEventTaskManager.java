package org.artifactory.event.work;

import org.artifactory.event.provider.EventProvider;
import org.artifactory.event.provider.FiniteEventProvider;
import org.artifactory.event.provider.NodesTableEvent;
import org.artifactory.storage.event.EventInfo;

/**
 * @author Uriah Levy
 * The event task manager is responsible for managing asynchronous node events on behalf of a {@link NodeEventOperator}.
 */
public interface NodeEventTaskManager {

    /**
     * Start managing node events.
     *
     * @param operator - the {@link NodeEventOperator} associated with this task manager
     */
    void startWithInfiniteProvider(NodeEventOperator operator, EventProvider<EventInfo> eventProvider);

    void startWithFiniteProvider(NodeEventOperator operator, FiniteEventProvider<NodesTableEvent> eventProvider);

    void stopDispatcherThread(String operatorId);

}
