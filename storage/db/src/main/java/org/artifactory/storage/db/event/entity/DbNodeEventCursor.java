package org.artifactory.storage.db.event.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jfrog.common.mapper.Validatable;

import static org.jfrog.common.ArgUtils.*;

/**
 * @author Uriah Levy
 */
@Data
@NoArgsConstructor
@Builder(toBuilder = true, builderClassName = "Builder")
public class DbNodeEventCursor implements Validatable {

    private String operatorId;
    private long eventMarker;
    private NodeEventCursorType type;

    public DbNodeEventCursor(String operatorId, long eventMarker, NodeEventCursorType type) {
        validate(operatorId, eventMarker, type);
    }

    @Override
    public void validate() {
        validate(operatorId, eventMarker, type);
    }

    public void validate(String operationId, long eventTimestamp, NodeEventCursorType type) {
        this.operatorId = requireNonBlank(operationId, "Operation ID is required");
        this.eventMarker = requireNonNegative(eventTimestamp, "Event Marker is required");
        this.type = requireNonNull(type, "Type is required");
    }
}
