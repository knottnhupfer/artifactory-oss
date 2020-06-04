package org.artifactory.storage.db.fs.itest.dao;

import org.artifactory.addon.jobs.JobStatus;
import org.artifactory.addon.jobs.JobType;
import org.artifactory.addon.jobs.JobsQuery;
import org.artifactory.storage.db.fs.dao.JobRecord;
import org.artifactory.storage.db.fs.dao.JobsDao;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.artifactory.common.ConstantValues.jobsTableFetchLimit;
import static org.testng.Assert.*;

/**
 * @author Yoaz Menda
 */
@Test
public class JobsDaoTest extends DbBaseTest {

    @Autowired
    private JobsDao jobsDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes.sql");
    }

    @Test
    public void getJobsZeroFilters() throws SQLException {
        List<JobRecord> jobs = jobsDao.getJobs(new JobsQuery.JobQueryBuilder().build());
        assertNotNull(jobs);
        assertEquals(jobs.size(), 9);
    }


    @Test
    public void getByTypeWithoutToParameter() throws SQLException {
        List<JobRecord> jobs = jobsDao.getJobs(new JobsQuery.JobQueryBuilder()
                .type(JobType.REPLICATION)
                .startedAfter("2014-12-12T10:35:40Z")
                .build());
        assertNotNull(jobs);
        assertEquals(jobs.size(), 3);
        for (JobRecord job : jobs) {
            assertEquals(job.getJobType(), JobType.REPLICATION.name());
        }
    }

    @Test
    public void getInsideInterval() throws SQLException {
        List<JobRecord> jobs = jobsDao.getJobs(new JobsQuery.JobQueryBuilder()
                .type(JobType.REPLICATION)
                .startedAfter("2014-12-12T10:35:00Z")
                .finishedBefore("2014-12-12T10:38:50Z")
                .build());
        assertNotNull(jobs);
        assertEquals(jobs.size(), 1);
        for (JobRecord job : jobs) {
            assertEquals(job.getJobType(), JobType.REPLICATION.name());
            assertEquals(job.getJobStatus(), JobStatus.FINISHED.name());
        }
    }

    @Test
    public void testSortedResultsAndAdditionalInfo() throws SQLException {
        List<JobRecord> jobs = jobsDao.getJobs(new JobsQuery.JobQueryBuilder()
                .type(JobType.REPLICATION)
                .startedAfter("2011-12-12T10:38:00Z")
                .finishedBefore("2013-12-12T10:38:00Z")
                .build());
        assertNotNull(jobs);
        assertEquals(jobs.size(), 2);
        JobRecord newer = jobs.get(0);
        JobRecord older = jobs.get(1);
        assertTrue(older.getStarted() < newer.getStarted());
        assertEquals(older.getAdditionalInfo(), "old");
        assertEquals(newer.getAdditionalInfo(), "new");
    }

    @Test
    public void getByTypeAndStatus() throws SQLException {
        List<JobRecord> jobs = jobsDao.getJobs(new JobsQuery.JobQueryBuilder()
                .type(JobType.REPLICATION)
                .status(JobStatus.RUNNING)
                .startedAfter("2014-12-12T10:35:40Z")
                .build());
        assertNotNull(jobs);
        assertEquals(jobs.size(), 1);
        for (JobRecord job : jobs) {
            assertEquals(job.getJobType(), JobType.REPLICATION.name());
            assertEquals(job.getJobStatus(), JobStatus.RUNNING.name());
        }
    }

    @Test
    public void testDeleteStartedBefore() throws SQLException {
        int jobsBefore = jobsDao.getJobs(new JobsQuery.JobQueryBuilder().build()).size();
        List<JobRecord> before = jobsDao.getJobs(new JobsQuery.JobQueryBuilder().build()).stream()
                .filter(jobRecord -> jobRecord.getJobType().equals("TYPE_TO_DELETE")).collect(
                        Collectors.toList());
        assertEquals(before.size(), 3);
        int deleted = jobsDao.deleteJobsStartedBefore(40L);
        assertEquals(deleted, 2);
        List<JobRecord> after = jobsDao.getJobs(new JobsQuery.JobQueryBuilder().build()).stream()
                .filter(jobRecord -> jobRecord.getJobType().equals("TYPE_TO_DELETE")).collect(
                        Collectors.toList());
        assertEquals(after.size(), 1);
        int jobsAfter = jobsDao.getJobs(new JobsQuery.JobQueryBuilder().build()).size();
        assertEquals(jobsAfter + 2, jobsBefore);
    }

    private long findBiggestJobId() throws SQLException {
        List<JobRecord> allJobs = jobsDao.getJobs(new JobsQuery.JobQueryBuilder().build());
        JobRecord jobRecord = allJobs.stream().max(Comparator.comparing(JobRecord::getJobId))
                .orElseThrow(NoSuchElementException::new);
        return jobRecord.getJobId();
    }

    @Test
    public void testLimit() throws SQLException {
        int jobCountBefore = jobsDao.getJobs(new JobsQuery.JobQueryBuilder().build()).size();
        long biggestJobId = findBiggestJobId() + 1000;
        try {
            for (int i = 0; i < 1 + jobsTableFetchLimit.getLong(); i++) {
                jobsDao.create(biggestJobId + i, "REPLICATION", "RUNNING", 8L, null, "{}");
            }
            int jobCountAfter = jobsDao.getJobs(new JobsQuery.JobQueryBuilder().build()).size();
            assertEquals(jobCountAfter, jobsTableFetchLimit.getLong());
        } finally {
            jobsDao.deleteJobsStartedBefore(10L);
            int jobCountFinally = jobsDao.getJobs(new JobsQuery.JobQueryBuilder().build()).size();
            assertEquals(jobCountFinally, jobCountBefore);
        }
    }
}
