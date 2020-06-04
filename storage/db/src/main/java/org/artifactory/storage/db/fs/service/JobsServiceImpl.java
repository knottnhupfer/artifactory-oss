package org.artifactory.storage.db.fs.service;

import org.artifactory.addon.jobs.JobStatus;
import org.artifactory.addon.jobs.JobType;
import org.artifactory.addon.jobs.JobsQuery;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.storage.db.fs.dao.JobRecord;
import org.artifactory.storage.db.fs.dao.JobsDao;
import org.artifactory.storage.db.fs.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.addon.jobs.JobStatus.RUNNING;


/**
 * @author Yoaz Menda
 */
@Service
public class JobsServiceImpl implements JobsService {
    private static final Logger log = LoggerFactory.getLogger(JobsServiceImpl.class);

    private JobsDao jobsDao;
    private InternalDbService dbService;

    @Autowired
    public JobsServiceImpl(JobsDao jobsDao, InternalDbService dbService) {
        this.jobsDao = jobsDao;
        this.dbService = dbService;
    }

    @Override
    public List<JobEntity> getJobs(JobsQuery jobsQuery) {
        try {
            List<JobRecord> jobRecords = jobsDao.getJobs(jobsQuery);
            return jobRecords.stream().map(this::recordToEntity).collect(Collectors.toList());
        } catch (SQLException e) {
            log.error("Failed to get jobs from db", e);
            throw new StorageException("Failed to get jobs from db: " + e.getMessage(), e);
        }
    }

    @Override
    public int deleteJobsStartedBefore(long startedBefore) {
        try {
            return jobsDao.deleteJobsStartedBefore(startedBefore);
        } catch (SQLException e) {
            log.error("Failed to delete jobs from db", e);
            throw new StorageException("Failed to delete jobs from db: " + e.getMessage(), e);
        }
    }

    @Override
    public long startNewJob(JobType jobType, String additionalDetails) {
        long jobId = dbService.nextId();
        try {
            jobsDao.create(jobId, jobType.toString(), RUNNING.toString(), Instant.now().toEpochMilli(), null,
                    additionalDetails);
        } catch (SQLException e) {
            throw new StorageException("Failed to persist job" + e.getMessage(), e);
        }
        return jobId;
    }

    @Override
    public JobEntity getJob(long jobId) {
        try {
            JobRecord jobRecord = jobsDao.getJob(jobId);
            if (jobRecord == null) {
                return null;
            }
            return recordToEntity(jobRecord);
        } catch (SQLException e) {
            log.error("Failed to get job from db", e);
            throw new StorageException("Failed to get job from db: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateReplicationJobIsFinished(long jobId, JobStatus newStatus, String additionalDetails) {
        try {
            jobsDao.updateJob(jobId, newStatus.toString(), additionalDetails);
        } catch (SQLException e) {
            String err = String.format("Failed to update job with id : %s", jobId);
            throw new StorageException(err + e.getMessage(), e);
        }
    }

    private JobEntity recordToEntity(JobRecord jobRecord) {
        return new JobEntity(
                jobRecord.getJobId(),
                jobRecord.getJobType() == null ? null : JobType.valueOf(jobRecord.getJobType()),
                jobRecord.getJobStatus() == null ? null : JobStatus.valueOf(jobRecord.getJobStatus()),
                jobRecord.getStarted() == 0 ? null : Instant.ofEpochMilli(jobRecord.getStarted()),
                jobRecord.getFinished() == 0 ? null : Instant.ofEpochMilli(jobRecord.getFinished()),
                jobRecord.getAdditionalInfo());
    }
}
