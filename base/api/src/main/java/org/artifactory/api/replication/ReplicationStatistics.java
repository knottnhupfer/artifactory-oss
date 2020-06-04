package org.artifactory.api.replication;

import lombok.Builder;
import lombok.Data;

/**
 * @author Yoaz Menda
 */
@Data
@Builder
public class ReplicationStatistics {
    private Integer averageTransferRateInKbps;
    private Long totalBytesTransferred;
    private Integer artifactsReplicatedSuccessfully;
    private Integer artifactsFailedToReplicate;
    private Long timeSpentReplicatingFilesInMillis;

    public ReplicationStatistics() {
    }

    public ReplicationStatistics(Integer averageTransferRateInKbps, Long totalBytesTransferred,
            Integer artifactsReplicatedSuccessfully, Integer artifactsFailedToReplicate,
            Long timeSpentReplicatingFilesInMillis) {
        this.averageTransferRateInKbps = averageTransferRateInKbps;
        this.totalBytesTransferred = totalBytesTransferred;
        this.artifactsReplicatedSuccessfully = artifactsReplicatedSuccessfully;
        this.artifactsFailedToReplicate = artifactsFailedToReplicate;
        this.timeSpentReplicatingFilesInMillis = timeSpentReplicatingFilesInMillis;
    }
}
