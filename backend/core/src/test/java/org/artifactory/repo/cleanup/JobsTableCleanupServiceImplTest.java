package org.artifactory.repo.cleanup;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.schedule.TaskService;
import org.artifactory.storage.db.fs.service.JobsService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.test.ArtifactoryHomeStub;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.artifactory.common.ConstantValues.jobsTableTimeToLiveMillis;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * @author Yoaz Menda
 */
public class JobsTableCleanupServiceImplTest extends ArtifactoryHomeBoundTest {

    private ArtifactoryHomeStub homeStub;
    private JobsTableCleanupService jobsTableCleanupService;
    @Mock
    private JobsService jobsService;
    @Mock
    private TaskService taskService;

    @BeforeMethod
    public void setup() {
        initMocks();
        jobsTableCleanupService = new JobsTableCleanupServiceImpl(jobsService, taskService);
        homeStub.initPropertiesAndReload();
        ArtifactoryHome.bind(homeStub);
        homeStub.setProperty(jobsTableTimeToLiveMillis, "3000");
    }

    @AfterMethod
    public void tearDown() {
        ArtifactoryHome.unbind();
    }

    protected ArtifactoryHomeStub getOrCreateArtifactoryHomeStub() {
        if (homeStub == null) {
            homeStub = new ArtifactoryHomeStub();
            homeStub.loadSystemProperties();
        }
        return homeStub;
    }

    private void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testClean() {
        when(jobsService.deleteJobsStartedBefore(anyLong())).thenReturn(1);
        jobsTableCleanupService.clean();
    }

    @Test
    public void testExceptionDuringClean() {
        when(jobsService.deleteJobsStartedBefore(anyLong())).thenThrow(new RuntimeException("STOP"));
        jobsTableCleanupService.clean();
    }
}