package org.artifactory.api.replication;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.StrictMath.toIntExact;

/**
 * @author Yoaz Menda
 */
public class ReplicationStatisticsHelper {

    private AtomicInteger artifactsReplicated;
    private AtomicInteger artifactsFailed;
    private AtomicLong bytesTransferred;
    private AtomicLong replicationDurationMillis;

    public ReplicationStatisticsHelper() {
        this.bytesTransferred = new AtomicLong();
        this.artifactsReplicated = new AtomicInteger();
        this.artifactsFailed = new AtomicInteger();
        this.replicationDurationMillis = new AtomicLong();
    }

    public ReplicationStatistics calc() {
        return ReplicationStatistics.builder()
                .averageTransferRateInKbps(averageTransferRate())
                .totalBytesTransferred(bytesTransferred.get())
                .artifactsReplicatedSuccessfully(artifactsReplicated.get())
                .artifactsFailedToReplicate(artifactsFailed.get())
                .timeSpentReplicatingFilesInMillis(replicationDurationMillis.get())
                .build();
    }

    private Integer averageTransferRate() {
        if (bytesTransferred.get() == 0 || artifactsReplicated.get() == 0 || replicationDurationMillis.get() == 0) {
            return 0;
        }
        long bytesPerMillis = bytesTransferred.get() / replicationDurationMillis.get();
        return toIntExact(bytesPerMillis);
    }

    public void incrementReplicatedArtifacts() {
        this.artifactsReplicated.incrementAndGet();
    }

    public void incrementBytesTransferred(long bytesTransferred) {
        this.bytesTransferred.addAndGet(bytesTransferred);
    }

    public void incrementReplicationDurationMillis(long replicationDurationMillis) {
        this.replicationDurationMillis.addAndGet(replicationDurationMillis);
    }

    public void incrementArtifactsFailedToReplicate(){
        this.artifactsFailed.incrementAndGet();
    }
}
