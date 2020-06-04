package org.artifactory.storage.db.replication.errors.service;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.replication.event.ReplicationEventType;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.storage.db.replication.errors.dao.ReplicationErrorsDao;
import org.artifactory.storage.db.replication.errors.entity.ReplicationErrorRecord;
import org.artifactory.storage.replication.errors.InternalReplicationEventType;
import org.artifactory.storage.replication.errors.ReplicationErrorInfo;
import org.artifactory.storage.replication.errors.ReplicationErrorsStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Storage service layer for persisting and retrieving replication errors (avoid using directly)
 *
 * @author Shay Bagants
 * @author Yoaz Menda
 */
@Service
public class ReplicationErrorsStorageServiceImpl implements ReplicationErrorsStorageService {
    private static final Logger log = LoggerFactory.getLogger(ReplicationErrorsStorageServiceImpl.class);

    @Autowired
    private ReplicationErrorsDao replicationErrorsDao;

    @Autowired
    private InternalDbService dbService;

    @Override
    public void add(ReplicationErrorInfo errorInfo) {
        ReplicationErrorRecord record = toErrorRecord(errorInfo);
        try {
            log.debug("adding replication error: {}", errorInfo);
            replicationErrorsDao.create(record);
        } catch (SQLException e) {
            throw new StorageException("Couldn't persist replication error from " + errorInfo.getTaskPath() + " to " +
                    errorInfo.getReplicationKey(), e);
        }
    }

    @Override
    public List<ReplicationErrorInfo> getErrors() {
        try {
            List<ReplicationErrorRecord> allErrors = replicationErrorsDao.getAllErrors();
            return allErrors.stream().map(this::toErrorInfo).collect(Collectors.toList());
        } catch (SQLException e) {
            log.error("Couldn't retrieve replication errors");
            throw new StorageException("Couldn't retrieve replication errors", e);
        }
    }

    @Override
    public List<ReplicationErrorInfo> getErrorsByReplicationKey(String replicationKey) {
        try {
            List<ReplicationErrorRecord> errors = replicationErrorsDao.getErrorsByReplicationKey(replicationKey);
            return errors.stream().map(this::toErrorInfo).collect(Collectors.toList());
        } catch (SQLException e) {
            log.error("Couldn't retrieve replication errors");
            throw new StorageException("Couldn't retrieve replication errors", e);
        }
    }

    @Override
    public boolean delete(ReplicationEventType eventType, RepoPath eventPath, String replicationKey) {
        if (!isValidEventRequest(eventType, eventPath, replicationKey)) {
            return false;
        }
        String message = "Type: " +
                eventType.name() + " Event path: " + eventPath.toPath() + "target repository: " + replicationKey;
        try {
            log.debug("Attempting to clean replication error record. {}", message);
            InternalReplicationEventType internalEventType = toInternalReplicationEventType(eventType);
            return replicationErrorsDao.delete(internalEventType, eventPath.toPath(), replicationKey);
        } catch (SQLException e) {
            throw new StorageException("Failed to delete replication error. " + message, e);
        }
    }

    @Override
    public int deleteAllByKey(String replicationKey) {
        if (StringUtils.isBlank(replicationKey)) {
            throw new StorageException("Replication key must be provided");
        }
        try {
            log.debug("Attempting to clean all replication error records under {}", replicationKey);
            return replicationErrorsDao.deleteAllByKey(replicationKey);
        } catch (SQLException e) {
            throw new StorageException("Failed to delete replication errors by replication key" + replicationKey, e);
        }
    }

    @Override
    public int deleteAllByKeyAndType(String replicationKey, ReplicationEventType taskType) {
        if (StringUtils.isBlank(replicationKey) || taskType == null) {
            throw new StorageException("Replication key and task type must be provided");
        }
        try {
            log.debug("Attempting to clean all replication error records of type {} under {}", taskType,
                    replicationKey);
            return replicationErrorsDao.deleteAllByKeyAndType(replicationKey, toInternalReplicationEventType(taskType));
        } catch (SQLException e) {
            throw new StorageException("Failed to delete replication errors of type " + taskType + " and replication key" + replicationKey, e);
        }
    }

    @Override
    public int update(ReplicationErrorInfo errorInfo) {
        ReplicationErrorRecord record = toErrorRecord(errorInfo);
        try {
            log.debug("updating replication error: {}", errorInfo);
            return replicationErrorsDao.update(record);
        } catch (SQLException e) {
            throw new StorageException("Couldn't update replication error", e);
        }
    }

    @Override
    public ReplicationErrorInfo getError(ReplicationEventType eventType, RepoPath eventPath, String replicationKey) {
        try {
            ReplicationErrorRecord errorRecord = replicationErrorsDao
                    .get(toInternalReplicationEventType(eventType), eventPath.toPath(), replicationKey);
            if (errorRecord == null) {
                return null;
            }
            return toErrorInfo(errorRecord);
        } catch (SQLException e) {
            log.error("Couldn't retrieve replication errors");
            throw new StorageException("Couldn't retrieve replication error", e);
        }
    }

    private boolean isValidEventRequest(ReplicationEventType eventType, RepoPath eventPath, String targetKey) {
        if (eventType == null) {
            log.warn("Invalid replication error request, missing replication event type.");
            return false;
        }
        if (eventPath == null) {
            log.warn("Invalid replication error request, missing replication event path.");
            return false;
        }
        if (targetKey == null) {
            log.warn("Invalid replication error request, missing replication target key.");
            return false;
        }
        return true;
    }

    private ReplicationErrorInfo toErrorInfo(ReplicationErrorRecord record) {
        ReplicationEventType type = toReplicationEventType(record.getTaskType());
        return ReplicationErrorInfo.builder()
                .firstErrorTime(record.getFirstErrorTime())
                .lastErrorTime(record.getLastErrorTime())
                .errorCount(record.getErrorCount())
                .errorMessage(record.getErrorMessage())
                .replicationKey(record.getReplicationKey())
                .taskTime(record.getTaskTime())
                .taskPath(RepoPathFactory.create(record.getTaskPath()))
                .taskType(type)
                .build();
    }

    private ReplicationErrorRecord toErrorRecord(ReplicationErrorInfo errorInfo) {
        RepoPath eventPath = errorInfo.getTaskPath();
        InternalReplicationEventType internalReplicationEventType = toInternalReplicationEventType(
                errorInfo.getTaskType());
        return ReplicationErrorRecord.builder()
                .errorId(dbService.nextId())
                .firstErrorTime(errorInfo.getFirstErrorTime())
                .lastErrorTime(errorInfo.getLastErrorTime())
                .errorCount(errorInfo.getErrorCount())
                .errorMessage(errorInfo.getErrorMessage())
                .replicationKey(errorInfo.getReplicationKey())
                .taskTime(errorInfo.getTaskTime())
                .taskType(internalReplicationEventType)
                .taskPath(eventPath.toPath())
                .build();
    }

    private InternalReplicationEventType toInternalReplicationEventType(ReplicationEventType replicationEventType) {
        InternalReplicationEventType internalType;
        switch (replicationEventType) {
            case MKDIR:
                internalType = InternalReplicationEventType.MKDIR;
                break;
            case DEPLOY:
                internalType = InternalReplicationEventType.DEPLOY;
                break;
            case DELETE:
                internalType = InternalReplicationEventType.DELETE;
                break;
            case PROPERTY_CHANGE:
                internalType = InternalReplicationEventType.PROPERTY_CHANGE;
                break;
            default:
                throw new IllegalArgumentException("Invalid replication error type: " + replicationEventType.name());
        }
        return internalType;
    }

    private ReplicationEventType toReplicationEventType(InternalReplicationEventType eventType) {
        ReplicationEventType type;
        switch (eventType) {
            case MKDIR:
                type = ReplicationEventType.MKDIR;
                break;
            case DEPLOY:
                type = ReplicationEventType.DEPLOY;
                break;
            case DELETE:
                type = ReplicationEventType.DELETE;
                break;
            case PROPERTY_CHANGE:
                type = ReplicationEventType.PROPERTY_CHANGE;
                break;
            default:
                throw new IllegalArgumentException("Invalid replication error type: " + eventType.name());
        }
        return type;
    }
}
