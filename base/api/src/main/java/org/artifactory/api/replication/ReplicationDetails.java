package org.artifactory.api.replication;

import lombok.Data;

/**
 * @author Yoaz Menda
 */
@Data
public class ReplicationDetails {
    private ReplicationMetaData replicationMetaData;
    private ReplicationStatistics replicationStatistics;

    public ReplicationDetails(ReplicationMetaData replicationMetaData,
            ReplicationStatistics replicationStatistics) {
        this.replicationMetaData = replicationMetaData;
        this.replicationStatistics = replicationStatistics;
    }

    public ReplicationDetails() {
    }
}
