package org.artifactory.event.provider;

import lombok.Value;
import org.artifactory.storage.event.EventType;
import org.artifactory.storage.event.NodeEvent;

/**
 * @author Uriah Levy
 */
@Value
public class NodesTableEvent implements NodeEvent {
    private String path;
    private long nodeId;

    public EventType getType() {
        return EventType.create;
    }

    @Override
    public long getTimestamp() {
        return nodeId;
    }
}