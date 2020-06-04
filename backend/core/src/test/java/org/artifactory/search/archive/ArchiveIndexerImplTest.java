package org.artifactory.search.archive;

import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.schedule.TaskService;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.storage.fs.lock.provider.JvmConflictGuard;
import org.jfrog.storage.common.ConflictGuard;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Shay Bagants
 */
@Test
public class ArchiveIndexerImplTest {

    private ArchiveIndexerImpl archiveIndexer = new ArchiveIndexerImpl();

    @Mock
    private TaskService taskService;

    @Mock
    private InternalArtifactoryContext context;

    @BeforeMethod
    private void setup() {
        MockitoAnnotations.initMocks(this);
        ArtifactoryContextThreadBinder.bind(context);
    }

    @AfterMethod
    private void cleanup() {
        ArtifactoryContextThreadBinder.unbind();
    }


    @Test(dataProvider = "shouldStopProvider")
    public void shouldStop(ConflictGuard conflictGuard, boolean isReady, boolean pauseOrBreak, boolean expectedToStop) {
        archiveIndexer.conflictGuard = conflictGuard;
        Mockito.when(context.isReady()).thenReturn(isReady);
        archiveIndexer.taskService = taskService;
        Mockito.when(taskService.pauseOrBreak()).thenReturn(pauseOrBreak);
        boolean shouldStop = archiveIndexer.shouldStop();
        Assert.assertEquals(shouldStop, expectedToStop);
    }

    @DataProvider
    private static Object[][] shouldStopProvider() {
        return new Object[][]{
                // conflictGuard, isReady, pauseOrBreak, expectedToStop
                {null, true, false, true},
                {null, true, true, true},
                {null, false, true, true},
                {null, false, false, true},
                {new JvmConflictGuard(null), true, false, false},
                {new JvmConflictGuard(null), true, true, true},
                {new JvmConflictGuard(null), false, true, true},
                {new JvmConflictGuard(null), false, false, true}
        };
    }
}