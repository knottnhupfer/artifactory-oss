package org.artifactory.storage.db.event.entity;

import java.util.ArrayList;
import java.util.List;

public enum NodeEventCursorType {

    INCREMENTAL_REPLICATION_PROGRESS(true, "INCREMENTAL_REPLICATION_PROGRESS"),
    INCREMENTAL_REPLICATION_START(true, "INCREMENTAL_REPLICATION_START"),
    METADATA_MIGRATION(true, "METADATA_MIGRATION"),
    METADATA_EVENTS(false, "METADATA_EVENTS"),
    DELETE_MARKER(false, "DELETE_MARKER"),
    INCREMENTAL_REPLICATION_STATS_PROGRESS(false, "INCREMENTAL_REPLICATION_STATS_PROGRESS")
    ;

    private final boolean eventLog;

    private final String name;

    NodeEventCursorType(boolean eventLog, String name) {
        this.eventLog = eventLog;
        this.name = name;
    }

    public boolean isEventLog() {
        return eventLog;
    }

    public String getName() {
        return name;
    }

    public static NodeEventCursorType forName(String name) {
        for (NodeEventCursorType eventType : values()) {
            if (eventType.name.equals(name)) {
                return eventType;
            }
        }
        throw new IllegalArgumentException("Unable to find cursor type for name: " + name);
    }

    public static List<NodeEventCursorType> getEventLogTypes() {
        List<NodeEventCursorType> logTypes = new ArrayList<>();
        for (NodeEventCursorType type : values()) {
            if (type.isEventLog()) {
                logTypes.add(type);
            }
        }
        return logTypes;
    }
}


