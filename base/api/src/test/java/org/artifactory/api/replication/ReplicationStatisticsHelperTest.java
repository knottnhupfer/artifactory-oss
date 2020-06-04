package org.artifactory.api.replication;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ReplicationStatisticsHelperTest {

    private ReplicationStatisticsHelper replicationStatisticsHelper;

    @BeforeMethod
    private void setup() {
        replicationStatisticsHelper = new ReplicationStatisticsHelper();
    }

    @Test
    public void testCalcNoTimeUpdated() {
        replicationStatisticsHelper.incrementBytesTransferred(4000);
        replicationStatisticsHelper.incrementReplicatedArtifacts();
        replicationStatisticsHelper.incrementReplicatedArtifacts();
        replicationStatisticsHelper.incrementReplicatedArtifacts();
        replicationStatisticsHelper.incrementArtifactsFailedToReplicate();
        ReplicationStatistics result = replicationStatisticsHelper.calc();
        assertNotNull(result);
        assertEquals(result.getAverageTransferRateInKbps(), Integer.valueOf(0));
    }

    @Test
    public void testCalculate() {
        //thread 1
        replicationStatisticsHelper.incrementBytesTransferred(4000);
        replicationStatisticsHelper.incrementReplicatedArtifacts();
        replicationStatisticsHelper.incrementReplicationDurationMillis(1000);
        //thread 2
        replicationStatisticsHelper.incrementBytesTransferred(5000);
        replicationStatisticsHelper.incrementReplicatedArtifacts();
        replicationStatisticsHelper.incrementReplicationDurationMillis(2000);
        //thread 3 - failed
        replicationStatisticsHelper.incrementArtifactsFailedToReplicate();
        ReplicationStatistics result = replicationStatisticsHelper.calc();
        assertNotNull(result);
        assertEquals(result.getArtifactsFailedToReplicate(), Integer.valueOf(1));
        assertEquals(result.getArtifactsReplicatedSuccessfully(), Integer.valueOf(2));
        assertEquals(result.getTotalBytesTransferred(), Long.valueOf(9000L));
        assertEquals(result.getTimeSpentReplicatingFilesInMillis(), Long.valueOf(3000));
        assertEquals(result.getAverageTransferRateInKbps(), Integer.valueOf(3));
    }

    @Test
    public void testCalculateSmallValues() {
        //thread 1
        replicationStatisticsHelper.incrementBytesTransferred(4);
        replicationStatisticsHelper.incrementReplicatedArtifacts();
        replicationStatisticsHelper.incrementReplicationDurationMillis(10);
        //thread 2
        replicationStatisticsHelper.incrementBytesTransferred(5);
        replicationStatisticsHelper.incrementReplicatedArtifacts();
        replicationStatisticsHelper.incrementReplicationDurationMillis(0);
        //thread 3 - failed
        replicationStatisticsHelper.incrementArtifactsFailedToReplicate();
        ReplicationStatistics result = replicationStatisticsHelper.calc();
        assertNotNull(result);
        assertEquals(result.getArtifactsFailedToReplicate(), Integer.valueOf(1));
        assertEquals(result.getArtifactsReplicatedSuccessfully(), Integer.valueOf(2));
        assertEquals(result.getTotalBytesTransferred(), Long.valueOf(9L));
        assertEquals(result.getTimeSpentReplicatingFilesInMillis(), Long.valueOf(10));
        assertEquals(result.getAverageTransferRateInKbps(), Integer.valueOf(0));
    }
}