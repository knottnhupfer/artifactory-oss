package org.artifactory.event.provider;

import org.artifactory.storage.db.event.model.NodeEventCursor;

import java.util.List;

/**
 * @author Uriah Levy
 * A contract for event providers. Event providers produce a list of events from a source
 * (i.e event_log / nodes table, etc)
 */
public interface EventProvider<T> {

    /**
     * Provide the next batch of events according to the current cursor
     * @param currentCursor - the current cursor containing the timestamp of the last handled event
     * @return - the next batch of events
     */
    List<T> provideNextBatch(NodeEventCursor currentCursor);

    ProviderType getType();

    enum ProviderType {
        NODES,
        EVENT_LOG
    }
}
