package org.artifactory.storage.db.binstore.service.garbage;

import com.google.common.collect.ImmutableList;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.storage.GCCandidate;
import org.artifactory.storage.binstore.service.GCFunction;
import org.artifactory.storage.binstore.service.GCProvider;
import org.artifactory.storage.binstore.service.GarbageCollectorInfo;
import org.artifactory.storage.binstore.service.GarbageCollectorStrategy;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Uriah Levy
 */
@Test
public class TrashGCProviderTest extends ArtifactoryHomeBoundTest {

    private GCProvider provider;

    @Mock
    private TrashService trashService;

    @BeforeClass
    private void setup() {
        MockitoAnnotations.initMocks(this);
        provider = new TrashGCProvider(trashService);
    }

    private List<GCCandidate> getGcCandidates() {
        return ImmutableList.of(
                new GCCandidate(null, "aaa", "aaa", "aaa", 55),
                new GCCandidate(null, "bbb", "bbb", "bbb", 55),
                new GCCandidate(null, "ccc", "ccc", "ccc", 55),
                new GCCandidate(null, "ddd", "ddd", "ddd", 55)
        );
    }

    public void testGetAction() {
        GCFunction cleanupAction = provider.getAction();

        GarbageCollectorInfo gcInfo = new GarbageCollectorInfo();
        List<GCCandidate> candidatesForDeletion = getGcCandidates();

        for (GCCandidate candidate : candidatesForDeletion) {
            boolean binaryDeleted = cleanupAction.accept(candidate, gcInfo);
            Mockito.verify(trashService).undeployFromTrash(candidate);
            Assert.assertFalse(binaryDeleted);
        }
    }

    public void testGetBatch() {
        Mockito.when(trashService.getGCCandidatesFromTrash()).thenReturn(getGcCandidates());
        List<GCCandidate> candidatesForDeletion = provider.getBatch();
        Mockito.verify(trashService, Mockito.times(1)).getGCCandidatesFromTrash();
        Assert.assertEquals(candidatesForDeletion.size(), 4);
    }

    public void testGetName() {
        Assert.assertEquals(GarbageCollectorStrategy.TRASH.name(), provider.getName());
    }

    public void testShouldReportAfterBatch() {
        Assert.assertFalse(provider.shouldReportAfterBatch());
    }
}