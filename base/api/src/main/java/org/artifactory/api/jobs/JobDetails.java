package org.artifactory.api.jobs;

import lombok.Builder;
import lombok.Data;

/**
 * @author Yoaz Menda
 */
@Data
@Builder
public class JobDetails {
    private String jobType;
    private String jobStatus;
    private String startedAfter;
    private String finishedBefore;

    public JobDetails() {
    }

    public JobDetails(String jobType, String jobStatus, String startedAfter, String finishedBefore) {
        this.jobType = jobType;
        this.jobStatus = jobStatus;
        this.startedAfter = startedAfter;
        this.finishedBefore = finishedBefore;
    }
}
