package org.artifactory.storage.event;

/**
 * @author Uriah Levy
 */
public interface NodeEvent {
    String getPath();

    EventType getType();

    long getTimestamp();
}