package org.artifactory.repo.service.trash.prune;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.storage.GCCandidate;
import org.artifactory.storage.binstore.service.BinariesGarbageCollectorService;
import org.artifactory.storage.binstore.service.GarbageCollectorInfo;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Test
public class TrashcanPrunerTest extends ArtifactoryHomeBoundTest {

    private TrashcanPruner trashcanPruner;

    @Mock
    private ArtifactoryContext artifactoryContext;
    @Mock
    private BinariesGarbageCollectorService gcService;
    @Mock
    private TrashService trashService;

    private int originalBatchSize;

    @BeforeMethod
    private void setup() {
        MockitoAnnotations.initMocks(this);
        when(artifactoryContext.beanForType(BinariesGarbageCollectorService.class)).thenReturn(gcService);
        when(artifactoryContext.beanForType(TrashService.class)).thenReturn(trashService);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        originalBatchSize = ConstantValues.trashcanMaxSearchResults.getInt();
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(
                ConstantValues.trashcanMaxSearchResults.getPropertyName(), "3");
        trashcanPruner = new TrashcanPruner();
    }

    @AfterMethod
    private void cleanup() {
        ArtifactoryContextThreadBinder.unbind();
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(
                ConstantValues.trashcanMaxSearchResults.getPropertyName(), String.valueOf(originalBatchSize));
    }

    public void testUndeployExpiredTrashItems() {
        List<GCCandidate> firstBatch = Arrays.asList(
                new GCCandidate(null, null, "x", null, 0),
                new GCCandidate(null, null, "y", null, 0),
                new GCCandidate(null, null, "z", null, 0)
        );
        List<GCCandidate> secondBatch = Arrays.asList(
                new GCCandidate(null, null, "1", null, 0),
                new GCCandidate(null, null, "2", null, 0)
        );
        List<GCCandidate> thirdBatch = Collections.singletonList(
                new GCCandidate(null, null, "t", null, 0));
        GarbageCollectorInfo info = new GarbageCollectorInfo();
        mockBatches(firstBatch, secondBatch, thirdBatch).when(trashService).getGCCandidatesFromTrash();

        trashcanPruner.undeployExpiredTrashItems(info);
        verify(trashService, times(2)).getGCCandidatesFromTrash();
        verify(trashService, times(5)).undeployFromTrash(any());
    }

    private Stubber mockBatches(List<GCCandidate> firstBatch, List<GCCandidate> secondBatch,
            List<GCCandidate> thirdBatch) {
        return doAnswer(new Answer() {
            private int count = 0;

            public Object answer(InvocationOnMock invocation) {
                count++;
                if (count == 1) {
                    return firstBatch;
                }
                if (count == 2) {
                    return secondBatch;
                }
                if (count == 3) {
                    return thirdBatch;
                }
                return Collections.emptyList();
            }
        });
    }

}