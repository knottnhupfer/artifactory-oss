package org.artifactory.storage.replication.errors;

import org.artifactory.addon.replication.event.ReplicationEventType;
import org.artifactory.repo.RepoPath;

import java.util.List;

/**
 * Service to persist and retrieve replication errors
 *
 * @author Shay Bagants
 * @author Yoaz Menda
 */
public interface ReplicationErrorsStorageService {

    /**
     * Persist replication error
     *
     * @param errorInfo The replication error information
     */
    void add(ReplicationErrorInfo errorInfo);

    /**
     * Retrieve all existing replication errors while maintaining their order
     *
     * @return all existing replication errors
     */
    List<ReplicationErrorInfo> getErrors();

    /**
     * Delete unique replication error event
     *
     * @param eventType      the type of the replication event, i.e. MKDIR, Deploy..
     * @param eventPath      the event RepoPath from the source repository
     * @param replicationKey the unique replication key combining both source and target information
     * @return true if succeeded, false otherwise
     */
    boolean delete(ReplicationEventType eventType, RepoPath eventPath, String replicationKey);

    /**
     * Delete all replication errors that matches a specific replication-key
     *
     * @param replicationKey - the replication key whose errors should be deleted
     * @return number of deleted replication errors
     */
    int deleteAllByKey(String replicationKey);

    /**
     * Delete all replication errors that matches a specific replication-key and are from the given type
     *
     * @param replicationKey - the replication key whose errors should be deleted
     * @param taskType       - the type of the originating task (deploy, delete ...)
     * @return number of deleted replication errors
     */
    int deleteAllByKeyAndType(String replicationKey, ReplicationEventType taskType);

    /**
     * Updates an existing error (same type, path and replication key) with the given new error (updating it's message and retry count)
     *
     * @param errorInfo - the error to update
     * @return the number of rows affected
     */
    int update(ReplicationErrorInfo errorInfo);

    /**
     * retrieves the error record from the db with the given type, path and replication key
     *
     * @param eventType      error type
     * @param eventPath      error artifact path
     * @param replicationKey the unique replication key combining both source and target information
     * @return the error object or null if one doesn't exist
     */
    ReplicationErrorInfo getError(ReplicationEventType eventType, RepoPath eventPath, String replicationKey);

    /**
     * Retrieve all existing replication errors while maintaining their order filtered by the given replication key
     *
     * @param replicationKey the replication key to filter errors by
     * @return all existing replication errors
     */
    List<ReplicationErrorInfo> getErrorsByReplicationKey(String replicationKey);
}
