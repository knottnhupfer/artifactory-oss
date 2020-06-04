package org.artifactory.storage.db.event.service.mapper;

import org.artifactory.storage.db.event.entity.DbNodeEventCursor;
import org.artifactory.storage.db.event.model.NodeEventCursor;
import org.mapstruct.Mapper;

/**
 * @author Uriah Levy
 */
@Mapper
public interface NodeEventCursorMapper {

    NodeEventCursor dbNodeEventCursorToNodeEventCursor(DbNodeEventCursor dbNodeEventCursor);

    DbNodeEventCursor nodeEventCursorToDbNodeEventCursor(NodeEventCursor nodeEventCursor);
}