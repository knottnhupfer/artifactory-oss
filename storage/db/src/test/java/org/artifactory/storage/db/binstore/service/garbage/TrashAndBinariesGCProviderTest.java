package org.artifactory.storage.db.binstore.service.garbage;

import com.google.common.collect.ImmutableList;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.storage.GCCandidate;
import org.artifactory.storage.binstore.service.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

@Test
public class TrashAndBinariesGCProviderTest {

    private GCProvider provider;
    @Mock
    private RepositoryService repositoryService;
    @Mock
    private TrashService trashService;
    @Mock
    private InternalBinaryService binaryService;

    @BeforeClass
    private void setup() {
        MockitoAnnotations.initMocks(this);
        provider = new TrashAndBinariesGCProvider(repositoryService, trashService, binaryService);
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

        Mockito.when(repositoryService.existsBySha1("aaa")).thenReturn(true);
        Mockito.when(repositoryService.existsBySha1("bbb")).thenReturn(true);
        Mockito.when(repositoryService.existsBySha1("ccc")).thenReturn(false);
        Mockito.when(repositoryService.existsBySha1("ddd")).thenReturn(false);

        Mockito.when(binaryService.executeBinaryCleaner(candidatesForDeletion.get(2), gcInfo)).thenReturn(true);
        Mockito.when(binaryService.executeBinaryCleaner(candidatesForDeletion.get(3), gcInfo)).thenReturn(false);

        for (int i = 0; i < candidatesForDeletion.size(); i++) {
            GCCandidate candidate = candidatesForDeletion.get(i);
            boolean binaryDeleted = cleanupAction.accept(candidate, gcInfo);
            Mockito.verify(trashService).undeployFromTrash(candidate);
            Mockito.verify(repositoryService).existsBySha1(candidate.getSha1());
            if (i < 2) {
                Mockito.verify(binaryService, Mockito.times(0)).executeBinaryCleaner(candidate, gcInfo);
                Assert.assertFalse(binaryDeleted);
            } else {
                Mockito.verify(binaryService, Mockito.times(1)).executeBinaryCleaner(candidate, gcInfo);
                if (i == 2) {
                    Assert.assertTrue(binaryDeleted);
                } else {
                    Assert.assertFalse(binaryDeleted);
                }
            }
        }
    }

    public void testGetBatch() {
        Mockito.when(trashService.getGCCandidatesFromTrash()).thenReturn(getGcCandidates());
        List<GCCandidate> candidatesForDeletion = provider.getBatch();
        Mockito.verify(trashService, Mockito.times(1)).getGCCandidatesFromTrash();
        Assert.assertEquals(candidatesForDeletion.size(), 4);
    }

    public void testGetName() {
        Assert.assertEquals(GarbageCollectorStrategy.TRASH_AND_BINARIES.name(), provider.getName());
    }

    public void testShouldReportAfterBatch() {
        Assert.assertTrue(provider.shouldReportAfterBatch());
    }
}