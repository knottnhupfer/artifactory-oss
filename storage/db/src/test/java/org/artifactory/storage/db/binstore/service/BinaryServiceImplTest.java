package org.artifactory.storage.db.binstore.service;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.storage.binstore.service.BinariesGarbageCollectorService;
import org.artifactory.storage.db.binstore.dao.BinariesDao;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;

/**
 * @author Shay Bagants
 */
@Test
public class BinaryServiceImplTest extends ArtifactoryHomeBoundTest {

    private BinaryServiceImpl binaryService = new BinaryServiceImpl();

    @Mock
    private ArtifactoryContext artifactoryContext;
    @Mock
    private BinariesGarbageCollectorService gcService;
    @Mock
    private TrashService trashService;
    @Mock
    private BinariesDao binariesDao;
    private int originalBatchSize;

    @BeforeMethod
    private void setup() {
        MockitoAnnotations.initMocks(this);
        when(artifactoryContext.beanForType(BinariesGarbageCollectorService.class)).thenReturn(gcService);
        when(artifactoryContext.beanForType(TrashService.class)).thenReturn(trashService);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        binaryService.binariesDao = binariesDao;
        originalBatchSize = ConstantValues.trashcanMaxSearchResults.getInt();
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(
                ConstantValues.trashcanMaxSearchResults.getPropertyName(), "3");
    }

    @AfterMethod
    private void cleanup() {
        ArtifactoryContextThreadBinder.unbind();
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(
                ConstantValues.trashcanMaxSearchResults.getPropertyName(), String.valueOf(originalBatchSize));
    }

    public void testStartGarbageCollect() throws SQLException {
        doThrow(new SQLException("message")).when(binariesDao).getCountAndTotalSize();
        when(trashService.isTrashcanEnabled()).thenReturn(true);

        AtomicInteger fullGcErrorCount = new AtomicInteger(0);
        for (int i = 0; i < 100; i++) {
            try {
                binaryService.startGarbageCollect();
            } catch (Exception e) {
                if (e.getMessage().equals("Could not find potential Binaries to delete!")) {
                    fullGcErrorCount.incrementAndGet();
                }
            }
        }
        verify(gcService, times(100)).startGCByStrategy(any());
        // ensure full gc was triggered once
        Assert.assertEquals(5, fullGcErrorCount.get());

        Assert.assertEquals(binaryService.gcIteration.get(), 0);
        binaryService.startGarbageCollect();
        Assert.assertEquals(binaryService.gcIteration.get(), 1);
    }
}