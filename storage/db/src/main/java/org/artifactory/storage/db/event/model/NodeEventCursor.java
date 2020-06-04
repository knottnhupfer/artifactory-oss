package org.artifactory.storage.db.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.storage.db.event.entity.NodeEventCursorType;

/**
 * @author Uriah Levy
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeEventCursor {
    private String operatorId;
    private long eventMarker;
    private NodeEventCursorType type;
}