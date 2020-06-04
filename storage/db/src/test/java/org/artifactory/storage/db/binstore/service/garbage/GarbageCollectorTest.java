package org.artifactory.storage.db.binstore.service.garbage;

import com.google.common.collect.ImmutableList;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.storage.DeletedGCCandidate;
import org.artifactory.storage.GCCandidate;
import org.artifactory.storage.binstore.service.GCProvider;
import org.artifactory.storage.binstore.service.GarbageCollectorInfo;
import org.artifactory.storage.binstore.service.InternalBinaryService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.storage.DbType;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

/**
 * @author dudim
 */
@Test
public class GarbageCollectorTest extends ArtifactoryHomeBoundTest {

    private GarbageCollector garbageCollector;

    private int originalBatchSize;
    @Mock
    private SecurityService securityService;
    @Mock
    private InternalBinaryService binaryService;
    @Mock
    private GCProvider gcProvider;

    @BeforeMethod
    private void setup() {
        MockitoAnnotations.initMocks(this);
        when(gcProvider.getName()).thenReturn("myname");
        originalBatchSize = ConstantValues.trashcanMaxSearchResults.getInt();
        ArtifactoryHome.get().getArtifactoryProperties()
                .setProperty(ConstantValues.trashcanMaxSearchResults.getPropertyName(), "3");
        garbageCollector = new GarbageCollector(gcProvider, securityService, binaryService, DbType.MYSQL);
    }

    @AfterMethod
    private void cleanup() {
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(
                ConstantValues.trashcanMaxSearchResults.getPropertyName(), String.valueOf(originalBatchSize));
    }

    public void testRun() {
        testGarbageCollection();
    }

    public void testRunWithError() {
        doThrow(new RuntimeException("Error occurred")).when(gcProvider).getBatch();
        garbageCollector.run();

        verify(gcProvider, times(1)).getBatch();
        verify(binaryService, times(0)).deleteBlobInfos(any());
        verify(binaryService, times(0)).deleteUnusedArchiveNames();
        verify(binaryService, times(0)).deleteUnusedArchivePaths();
    }

    public void testGarbageCollection() {
        List<GCCandidate> firstBatch = Arrays.asList(
                new GCCandidate(null, null, "x", null, 0),
                new GCCandidate(null, null, "y", null, 0),
                new GCCandidate(null, null, "`", null, 0)
        );
        List<GCCandidate> secondBatch = Arrays.asList(
                new GCCandidate(null, null, "1", null, 0),
                new GCCandidate(null, null, "2", null, 0),
                new GCCandidate(null, null, "3", null, 0)
        );

        mockTwoBatches(firstBatch, secondBatch).when(gcProvider).getBatch();

        when(gcProvider.getAction()).thenReturn((candidate, result) -> true);
        garbageCollector.run();

        verify(gcProvider, times(3)).getBatch();
        List<GCCandidate> allBatches = Stream.concat(firstBatch.stream(), secondBatch.stream())
                .collect(Collectors.toList());
        Set<String> sha2s = allBatches.stream().map(GCCandidate::getSha2).collect(Collectors.toSet());
        ArgumentCaptor<List<String>> argument = ArgumentCaptor.forClass(List.class);
        verify(binaryService, times(2)).deleteBlobInfos(argument.capture());
        List<List<String>> capturedBlobInfoDeletionRequests = argument.getAllValues();
        Set<String> capturedShas = capturedBlobInfoDeletionRequests.stream().flatMap(List::stream)
                .collect(Collectors.toSet());
        Assert.assertEquals(sha2s, capturedShas);

        // should be called at the end of the GC
        verify(binaryService, times(1)).deleteUnusedArchiveNames();
        verify(binaryService, times(1)).deleteUnusedArchivePaths();
    }

    public void testGarbageCollectionSmallBatch() {
        List<GCCandidate> firstBatch = Arrays.asList(
                new GCCandidate(null, null, "file-1", null, 0),
                new GCCandidate(null, null, "file-2", null, 0));
        List<GCCandidate> secondBatch = Collections.singletonList(
                new GCCandidate(null, null, "x", null, 0));
        mockTwoBatches(firstBatch, secondBatch).when(gcProvider).getBatch();
        when(gcProvider.getAction()).thenReturn((candidate, result) -> true);

        garbageCollector.run();
        // ensure second batch was never called as the first one was not full
        verify(gcProvider, times(1)).getBatch();
    }

    public void testGarbageCollectionWithSingleError() {
        List<GCCandidate> firstBatch = Arrays.asList(
                new GCCandidate(null, null, "x", null, 0),
                new GCCandidate(null, null, "y", null, 0),
                new GCCandidate(null, null, "z", null, 0)
        );
        List<GCCandidate> secondBatch = Arrays.asList(
                new GCCandidate(null, null, "1", null, 0),
                new GCCandidate(null, null, "2", null, 0),
                new GCCandidate(null, null, "3", null, 0)

        );
        mockTwoBatches(firstBatch, secondBatch).when(gcProvider).getBatch();

        AtomicInteger counter = new AtomicInteger(0);
        when(gcProvider.getAction()).thenReturn((candidate, result) -> {
            counter.getAndIncrement();
            return !candidate.getSha2().equals("x");
        });
        garbageCollector.run();

        verify(gcProvider, times(3)).getBatch();
        List<GCCandidate> allBatches = Stream.concat(firstBatch.stream(), secondBatch.stream())
                .collect(Collectors.toList());
        Set<String> sha2s = allBatches.stream().map(GCCandidate::getSha2)
                // filter out the first event which failed by the action
                .filter(sha -> !sha.equals(firstBatch.get(0).getSha2()))
                .collect(Collectors.toSet());
        ArgumentCaptor<List<String>> argument = ArgumentCaptor.forClass(List.class);
        verify(binaryService, times(2)).deleteBlobInfos(argument.capture());
        List<List<String>> capturedBlobInfoDeletionRequests = argument.getAllValues();
        Set<String> capturedShas = capturedBlobInfoDeletionRequests.stream().flatMap(List::stream)
                .collect(Collectors.toSet());
        Assert.assertEquals(sha2s, capturedShas);

        // should be called at the end of the GC
        verify(binaryService, times(1)).deleteUnusedArchiveNames();
        verify(binaryService, times(1)).deleteUnusedArchivePaths();
    }

    public void testExecuteTask() {
        GCCandidate candidate = new GCCandidate(null, null, "x", null, 0);
        GarbageCollectorInfo gcInfo = new GarbageCollectorInfo();
        when(gcProvider.getAction()).thenReturn((gcCandidate, rs) -> true);

        DeletedGCCandidate deletedGCCandidate = garbageCollector.executeTask(gcProvider, candidate, gcInfo);
        Assert.assertEquals(deletedGCCandidate.getDeletedCandidate().getSha2(), candidate.getSha2());

        verify(securityService, times(1)).authenticateAsSystem();
        verify(gcProvider, times(1)).getAction();
    }

    public void testExecuteTaskWithError() {
        GCCandidate candidate = new GCCandidate(null, null, "x", null, 0);
        GarbageCollectorInfo gcInfo = new GarbageCollectorInfo();

        doThrow(new RuntimeException("message")).when(gcProvider).getAction();
        DeletedGCCandidate deletedGCCandidate = garbageCollector.executeTask(gcProvider, candidate, gcInfo);
        Assert.assertNull(deletedGCCandidate);
        verify(securityService, times(1)).authenticateAsSystem();
        verify(gcProvider, times(1)).getAction();
    }


    private Stubber mockTwoBatches(List<GCCandidate> firstBatchCandidates, List<GCCandidate> secondBatchCandidates) {
        return doAnswer(new Answer() {
            private int count = 0;

            public Object answer(InvocationOnMock invocation) {
                count++;
                if (count == 1) {
                    return firstBatchCandidates;
                }
                if (count == 2) {
                    return secondBatchCandidates;
                }
                return Collections.emptyList();
            }
        });
    }

    public void testDeleteBlobInfos() {
        List<DeletedGCCandidate> deletedBinaries = Arrays.asList(
                new DeletedGCCandidate(new GCCandidate(null, "1", "x", null, 0)),
                new DeletedGCCandidate(new GCCandidate(null, "2", "", null, 0)),
                new DeletedGCCandidate(new GCCandidate(null, "3", null, null, 0))
        );
        garbageCollector.deleteBlobInfos(deletedBinaries);
        ArgumentCaptor<List<String>> argument = ArgumentCaptor.forClass(List.class);
        verify(binaryService, times(1)).deleteBlobInfos(argument.capture());
        List<String> capturedSha2s = argument.getValue();
        Assert.assertEquals(capturedSha2s.size(),1);
        Assert.assertEquals(capturedSha2s.get(0), deletedBinaries.get(0).getDeletedCandidate().getSha2());
    }

    public void testWaitForWorkers() throws ExecutionException, InterruptedException {
        Future firstFuture = mock(Future.class);
        when(firstFuture.get()).thenReturn(null);
        Future secondFuture = mock(Future.class);
        when(secondFuture.get()).thenReturn(
                new DeletedGCCandidate(new GCCandidate(null, "x", "y", "z", 1)));
        List<Future<DeletedGCCandidate>> futures = ImmutableList.of(firstFuture, secondFuture);
        List<DeletedGCCandidate> resultsDeletedGcCandidates = garbageCollector
                .waitForWorkers(gcProvider, futures, new GarbageCollectorInfo());
        Assert.assertEquals(resultsDeletedGcCandidates.size(), 1);
        Assert.assertEquals(resultsDeletedGcCandidates.get(0).getDeletedCandidate().getSha1(), "x");
    }

    public void testMssqlThreads() {
        garbageCollector = new GarbageCollector(gcProvider, securityService, binaryService, DbType.MSSQL);
        Assert.assertEquals(garbageCollector.numberOfThreads, 1);

        Arrays.stream(DbType.values())
                .filter(dbType -> dbType != DbType.MSSQL)
                .forEach(dbType -> {
                    garbageCollector = new GarbageCollector(gcProvider, securityService, binaryService, DbType.MYSQL);
                    Assert.assertEquals(garbageCollector.numberOfThreads,
                            ConstantValues.gcNumberOfWorkersThreads.getInt());
                });
    }
}