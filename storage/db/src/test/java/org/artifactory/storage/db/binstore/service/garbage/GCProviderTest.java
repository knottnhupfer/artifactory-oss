package org.artifactory.storage.db.binstore.service.garbage;

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.storage.binstore.service.InternalBinaryService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * @author Uriah Levy
 */
@Test
public class GCProviderTest extends ArtifactoryHomeBoundTest {

    public void testShouldPrintReport() {
        TrashAndBinariesGCProvider trashAndBinariesGCProvider = new TrashAndBinariesGCProvider(mock(
                RepositoryService.class), mock(TrashService.class), mock(InternalBinaryService.class));
        assertTrue(trashAndBinariesGCProvider.shouldReportAfterBatch());
    }
}