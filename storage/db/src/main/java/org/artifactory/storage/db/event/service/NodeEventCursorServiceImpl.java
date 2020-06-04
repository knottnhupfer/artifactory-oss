package org.artifactory.storage.db.event.service;

import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.event.dao.NodeEventsCursorDao;
import org.artifactory.storage.db.event.entity.DbNodeEventCursor;
import org.artifactory.storage.db.event.entity.NodeEventCursorType;
import org.artifactory.storage.db.event.model.NodeEventCursor;
import org.artifactory.storage.db.event.service.mapper.NodeEventCursorMapper;
import org.artifactory.storage.db.event.service.mapper.NodeEventCursorMapperImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

/**
 * @author Uriah Levy
 */
@Service
public class NodeEventCursorServiceImpl implements NodeEventCursorService {
    private static final Logger log = LoggerFactory.getLogger(NodeEventCursorServiceImpl.class);

    private NodeEventsCursorDao nodeEventsCursorDao;
    private NodeEventCursorMapper nodeEventCursorMapper;

    @Autowired
    NodeEventCursorServiceImpl(NodeEventsCursorDao nodeEventsCursorDao) {
        this.nodeEventsCursorDao = nodeEventsCursorDao;
        this.nodeEventCursorMapper = new NodeEventCursorMapperImpl();
    }

    @Override
    public void updateOrInsertCursor(NodeEventCursor eventCursor) {
        try {
            log.debug("Updating cursor for operator '{}' with timestamp {}", eventCursor.getOperatorId(),
                    eventCursor.getEventMarker());
            DbNodeEventCursor dbCursor = nodeEventCursorMapper.nodeEventCursorToDbNodeEventCursor(eventCursor);
            int updateResult = nodeEventsCursorDao.update(dbCursor);
            if (updateResult == 0) {
                nodeEventsCursorDao.insert(dbCursor);
            }
        } catch (SQLException e) {
            throw new StorageException("Could not update cursor for operator with ID '" + eventCursor.getOperatorId() +
                    "'", e);
        }
    }

    @Override
    public void insertCursor(String operatorId, NodeEventCursorType nodeEventCursorType) {
        try {
            nodeEventsCursorDao.insert(nodeEventCursorMapper
                    .nodeEventCursorToDbNodeEventCursor(new NodeEventCursor(operatorId, 0, nodeEventCursorType)));
        } catch (SQLException e) {
            throw new StorageException("Could not insert cursor for operator with ID '" + operatorId + "'", e);
        }
    }

    @Override
    public NodeEventCursor cursorForOperator(String operatorId) {
        try {
            Optional<DbNodeEventCursor> dbNodeEventCursorOptional = nodeEventsCursorDao.cursorForOperator(operatorId);
            return dbNodeEventCursorOptional
                    .map(value -> nodeEventCursorMapper.dbNodeEventCursorToNodeEventCursor(value))
                    .orElse(null);
        } catch (SQLException e) {
            throw new StorageException("Unable to retrieve cursor for operator with ID " + operatorId, e);

        }
    }

    @Override
    public void deleteCursor(String operationId) {
        try {
            nodeEventsCursorDao.delete(operationId);
        } catch (SQLException e) {
            throw new StorageException("Unable to delete cursor with ID " + operationId, e);
        }
    }

    @Override
    public long oldestTimestampForEventLogOperator() {
        try {
            return nodeEventsCursorDao.oldestMarkerForTypes(NodeEventCursorType.getEventLogTypes());
        } catch (SQLException e) {
            throw new StorageException("Error when trying to get oldest timestamp from event log", e);
        }
    }

    @Override
    public void updateCursor(String operatorId, long timestamp) {
        try {
            nodeEventsCursorDao.update(operatorId, timestamp);
        }  catch (SQLException e) {
            throw new StorageException("Could not update cursor for operator with ID: " + operatorId, e);
        }
    }
}