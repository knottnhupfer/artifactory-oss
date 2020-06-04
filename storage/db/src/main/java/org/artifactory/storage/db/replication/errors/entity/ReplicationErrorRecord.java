package org.artifactory.storage.db.replication.errors.entity;


import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.artifactory.storage.replication.errors.InternalReplicationEventType;

/**
 * DB model for replication errors
 *
 * @author Shay Bagants
 */
@Value
@Builder
@EqualsAndHashCode
public class ReplicationErrorRecord {
    private long errorId;
    private long firstErrorTime;
    private long lastErrorTime;
    private int errorCount;
    private String errorMessage;
    private String replicationKey;
    private long taskTime;
    private InternalReplicationEventType taskType;
    private String taskPath;
}
