package org.artifactory.storage.db.fs.service;

import org.artifactory.addon.jobs.JobStatus;
import org.artifactory.addon.jobs.JobType;
import org.artifactory.addon.jobs.JobsQuery;
import org.artifactory.storage.db.fs.entity.JobEntity;

import java.util.List;


/**
 * @author Yoaz Menda
 */
public interface JobsService {

    /**
     * retrieve a subset of existing artifactory job entries the given query
     *
     * @param jobsQuery - query containing filters (i.e. job type, jobs status, etc...)
     */
    List<JobEntity> getJobs(JobsQuery jobsQuery);

    /**
     * delete jobs that have started before a given point in time
     *
     * @param startedAfter - millis since epoch time that represents an absolute point in time
     * @return number of deleted items
     */
    int deleteJobsStartedBefore(long startedAfter);

    /**
     * add a new job record with the RUNNING status
     *
     * @param jobType           - type of the job (i.e. REPLICATION)
     * @param additionalDetails - (job related additional info)
     * @return the job id
     */
    long startNewJob(JobType jobType, String additionalDetails);

    /**
     * retrieve a job entry
     *
     * @param jobId - job id to retrieve
     * @return the job entity or null if not found
     */
    JobEntity getJob(long jobId);

    /**
     * update an existing job entry with the FINISHED status and overrides the job's current additional details
     *
     * @param jobId             - id of job the update
     * @param additionalDetails - replaces the current additional details
     */
    void updateReplicationJobIsFinished(long jobId, JobStatus newJobStatus, String additionalDetails);
}
