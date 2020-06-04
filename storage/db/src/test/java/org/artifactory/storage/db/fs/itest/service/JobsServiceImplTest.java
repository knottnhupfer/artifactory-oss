package org.artifactory.storage.db.fs.itest.service;

import org.artifactory.addon.jobs.JobStatus;
import org.artifactory.addon.jobs.JobType;
import org.artifactory.addon.jobs.JobsQuery;
import org.artifactory.storage.db.fs.entity.JobEntity;
import org.artifactory.storage.db.fs.service.JobsService;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Yoaz Menda
 */
@Test
public class JobsServiceImplTest extends DbBaseTest {

    @Autowired
    private JobsService jobsService;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes-for-service.sql");
    }

    @Test
    public void getByTypeWithoutToParameter() {
        List<JobEntity> jobs = jobsService.getJobs(new JobsQuery.JobQueryBuilder()
                .type(JobType.REPLICATION)
                .startedAfter("2014-12-12T10:35:40Z")
                .build());
        assertNotNull(jobs);
        assertEquals(jobs.size(), 3);
        for (JobEntity job : jobs) {
            assertEquals(job.getJobType(), JobType.REPLICATION);
        }
    }

    @Test
    public void getInsideInterval() {
        List<JobEntity> jobs = jobsService.getJobs(new JobsQuery.JobQueryBuilder()
                .type(JobType.REPLICATION)
                .startedAfter("2014-12-12T10:35:00Z")
                .finishedBefore("2014-12-12T10:38:50Z")
                .build());
        assertNotNull(jobs);
        assertEquals(jobs.size(), 1);
        for (JobEntity job : jobs) {
            assertEquals(job.getJobType(), JobType.REPLICATION);
            assertEquals(job.getJobStatus(), JobStatus.FINISHED);
        }
    }

    @Test
    public void testSortedResultsAndAdditionalInfo() {
        List<JobEntity> jobs = jobsService.getJobs(new JobsQuery.JobQueryBuilder()
                .type(JobType.REPLICATION)
                .startedAfter("2011-12-12T10:38:00Z")
                .finishedBefore("2013-12-12T10:38:00Z")
                .build());
        assertNotNull(jobs);
        assertEquals(jobs.size(), 2);
        JobEntity newer = jobs.get(0);
        JobEntity older = jobs.get(1);
        assertTrue(older.getStarted().isBefore(newer.getStarted()));
        assertEquals(older.getAdditionalDetails(), "old");
        assertEquals(newer.getAdditionalDetails(), "new");
    }

    @Test
    public void getByTypeAndStatus() {
        List<JobEntity> jobs = jobsService.getJobs(new JobsQuery.JobQueryBuilder()
                .type(JobType.REPLICATION)
                .status(JobStatus.RUNNING)
                .startedAfter("2014-12-12T10:35:40Z")
                .build());
        assertNotNull(jobs);
        assertEquals(jobs.size(), 1);
        for (JobEntity job : jobs) {
            assertEquals(job.getJobType(), JobType.REPLICATION);
            assertEquals(job.getJobStatus(), JobStatus.RUNNING);
        }
    }

    @Test
    public void getEvenWhenFinishTimeIsNull() {
        List<JobEntity> jobs = jobsService.getJobs(new JobsQuery.JobQueryBuilder()
                .type(JobType.REPLICATION)
                .status(JobStatus.RUNNING)
                .startedAfter("2014-12-12T10:35:40Z")
                .build());
        assertNotNull(jobs);
        assertEquals(jobs.size(), 1);
    }
}
