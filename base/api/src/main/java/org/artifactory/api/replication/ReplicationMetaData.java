package org.artifactory.api.replication;

import lombok.Data;

/**
 * @author Yoaz Menda
 */
@Data
public class ReplicationMetaData {
    private String replicationType;
    private String sourceRepo;
    private String targetUrl;
    private String repoPath;
    private String replicationStrategy;

    public ReplicationMetaData() {
    }

    public ReplicationMetaData(String replicationType, String sourceRepo, String targetUrl, String repoPath,
            String replicationStrategy) {
        this.replicationType = replicationType;
        this.sourceRepo = sourceRepo;
        this.targetUrl = targetUrl;
        this.repoPath = repoPath;
        this.replicationStrategy = replicationStrategy;
    }
}
