package org.artifactory.storage.db.event.service;

import org.artifactory.storage.db.event.entity.NodeEventCursorType;
import org.artifactory.storage.db.event.model.NodeEventCursor;

/**
 * @author Uriah Levy
 * A bussiness service that manages the node events cursor table. This service
 * keeps track and updates an indepedant cursor for the different event-based operations,
 * based on a unique operation identifier provided by the caller.
 */
public interface NodeEventCursorService {

    /**
     * Insert a new event operator cursor
     *
     * @param operationId the operation Id of the new cursor
     */
    void insertCursor(String operationId, NodeEventCursorType nodeEventCursorType);

    /**
     * Update the current cursor position.
     *
     * @param eventCursor the {@link NodeEventCursor} to set
     */
    void updateOrInsertCursor(NodeEventCursor eventCursor);

    /**
     * Get the current {@link NodeEventCursor}
     *
     * @param operationId the identifier of the event operation (metadata, replication, etc)
     * @return the current cursor position.
     */
    NodeEventCursor cursorForOperator(String operationId);

    void deleteCursor(String operationId);

    long oldestTimestampForEventLogOperator();

    /**
     * Update the cursor by ID
     */
    void updateCursor(String operatorId, long timestamp);
}