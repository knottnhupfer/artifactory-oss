package org.artifactory.storage.replication.errors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.artifactory.addon.replication.event.ReplicationEventType;
import org.artifactory.repo.RepoPath;

/**
 * @author Shay Bagants
 */
@Builder
@Value
@AllArgsConstructor
public class ReplicationErrorInfo {
    private long taskTime;
    private int errorCount;
    private String errorMessage;
    private String replicationKey;
    private long firstErrorTime;
    private long lastErrorTime;
    private ReplicationEventType taskType;
    private RepoPath taskPath;

    @Override
    public String toString() {
        return "ReplicationErrorInfo{" +
                "taskTime=" + taskTime +
                ", errorCount=" + errorCount +
                ", errorMessage='" + errorMessage + '\'' +
                ", replicationKey='" + replicationKey + '\'' +
                ", firstErrorTime=" + firstErrorTime +
                ", lastErrorTime=" + lastErrorTime +
                ", taskType=" + taskType +
                ", taskPath=" + taskPath +
                '}';
    }
}
