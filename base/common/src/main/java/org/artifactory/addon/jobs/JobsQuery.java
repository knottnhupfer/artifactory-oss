package org.artifactory.addon.jobs;


import org.apache.commons.lang.StringUtils;

import java.time.Instant;

/*
 * @author Yoaz Menda
 */
public class JobsQuery {

    private Instant startedAfter;
    private Instant finishedBefore;
    private JobType jobType;
    private JobStatus jobStatus;

    private JobsQuery(Instant startedAfter, Instant finishedBefore, JobType jobType, JobStatus jobStatus) {
        this.startedAfter = startedAfter;
        this.finishedBefore = finishedBefore;
        this.jobStatus = jobStatus;
        this.jobType = jobType;
    }

    public Instant getStartedAfter() {
        return startedAfter;
    }

    public Instant getFinishedBefore() {
        return finishedBefore;
    }

    public JobType getJobType() {
        return jobType;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public static class JobQueryBuilder {
        private String startedAfter;
        private String finishedBefore;
        private JobType jobType;
        private JobStatus jobStatus;

        public JobQueryBuilder startedAfter(String startedAfter) {
            this.startedAfter = startedAfter;
            return this;
        }

        public JobQueryBuilder finishedBefore(String finishedBefore) {
            this.finishedBefore = finishedBefore;
            return this;
        }

        public JobQueryBuilder type(JobType jobType) {
            this.jobType = jobType;
            return this;
        }

        public JobQueryBuilder status(JobStatus jobStatus) {
            this.jobStatus = jobStatus;
            return this;
        }

        public JobsQuery build() {
            Instant startedAfterInstant = null;
            Instant finishedbeforeInstant = null;
            if (StringUtils.isNotBlank(startedAfter)) {
                startedAfterInstant = Instant.parse(startedAfter);
            }
            if (StringUtils.isNotBlank(finishedBefore)) {
                finishedbeforeInstant = Instant.parse(finishedBefore);
            }
            return new JobsQuery(startedAfterInstant, finishedbeforeInstant, jobType, jobStatus);
        }
    }
}
