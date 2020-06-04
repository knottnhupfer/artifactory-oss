package org.artifactory.api.replication;

import lombok.Data;
import org.artifactory.api.jobs.JobDetails;

/**
 * @author Yoaz Menda
 */
@Data
public class ReplicationJobInfo {
    private JobDetails jobDetails;
    private ReplicationDetails replicationDetails;

    public ReplicationJobInfo(JobDetails jobDetails, ReplicationDetails replicationDetails) {
        this.jobDetails = jobDetails;
        this.replicationDetails = replicationDetails;
    }

    public ReplicationJobInfo() {
    }
}
