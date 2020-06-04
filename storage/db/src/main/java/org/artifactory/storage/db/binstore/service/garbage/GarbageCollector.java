package org.artifactory.storage.db.binstore.service.garbage;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ConstantValues;
import org.artifactory.storage.GCCandidate;
import org.artifactory.storage.DeletedGCCandidate;
import org.artifactory.storage.binstore.service.GCProvider;
import org.artifactory.storage.binstore.service.GarbageCollectorInfo;
import org.artifactory.storage.binstore.service.InternalBinaryService;
import org.artifactory.util.CollectionUtils;
import org.jfrog.storage.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author Uriah Levy
 */
public class GarbageCollector {
    private static final Logger log = LoggerFactory.getLogger(GarbageCollector.class);
    private GCProvider gcProvider;
    private InternalBinaryService binaryService;
    private ExecutorService executorService;
    private SecurityService securityService;
    private int expectedBatchSize;
    int numberOfThreads;

    public GarbageCollector(GCProvider gcProvider, SecurityService securityService, InternalBinaryService binaryService,
            DbType databaseType) {
        this.securityService = securityService;
        this.gcProvider = gcProvider;
        this.binaryService = binaryService;
        numberOfThreads = ConstantValues.gcNumberOfWorkersThreads.getInt();
        if (databaseType == DbType.MSSQL) {
            // Deadlock might occurs specifically on mssql with multi threaded handling the same checksums.
            numberOfThreads = 1;
        }
        executorService = getExecutorService();
        expectedBatchSize = ConstantValues.trashcanMaxSearchResults.getInt();
    }

    public void run() {
        try {
            log.info("Starting GC strategy '{}'", gcProvider.getName());
            garbageCollect();
            log.info("Finished GC Strategy '{}'", gcProvider.getName());
        } catch (Exception e) {
            log.error("Error occurred during Garbage Collection. {}", e.getMessage());
            log.debug("Error occurred during Garbage Collection.", e);
        } finally {
            destroy();
        }
    }

    private void destroy() {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(120, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Failed shutting down Garbage Collector executor service: {}", e.getMessage());
            log.debug("Failed shutting down Garbage Collector executor service", e);
            Thread.currentThread().interrupt();
        }
    }

    private void garbageCollect() {
        GarbageCollectorInfo result = new GarbageCollectorInfo();
        List<GCCandidate> batch;
        boolean cleanArchiveIndexes = false;
        boolean shouldStop = false;
        while (!shouldStop && !((batch = gcProvider.getBatch()).isEmpty())) {
            shouldStop = batch.size() < expectedBatchSize;
            List<DeletedGCCandidate> deletedGCCandidates = doWork(gcProvider, batch);
            cleanArchiveIndexes = cleanArchiveIndexes || !CollectionUtils.isNullOrEmpty(deletedGCCandidates);
            deleteBlobInfos(deletedGCCandidates);
        }
        if (cleanArchiveIndexes) {
            cleanArchivedIndexes(result);
        }
    }

    private void cleanArchivedIndexes(GarbageCollectorInfo result) {
        log.debug("Cleaning archive indexes data");
        result.archivePathsCleaned.set(binaryService.deleteUnusedArchivePaths());
        result.archiveNamesCleaned.set(binaryService.deleteUnusedArchiveNames());
        log.debug("{} archive paths were cleaned", result.archivePathsCleaned);
        log.debug("{} archive names were cleaned", result.archiveNamesCleaned);
    }

    private List<DeletedGCCandidate> doWork(GCProvider gcProvider, List<GCCandidate> batch) {
        log.debug("Starting to work on GC batch for provider '{}'", gcProvider.getName());
        List<Future<DeletedGCCandidate>> futures = new ArrayList<>();
        GarbageCollectorInfo result = submitWorkers(gcProvider, batch, futures);
        List<DeletedGCCandidate> deletedGCCandidates = waitForWorkers(gcProvider, futures, result);
        log.debug("Finished working on GC batch for provider '{}'.", gcProvider.getName());
        return deletedGCCandidates;
    }

    void deleteBlobInfos(List<DeletedGCCandidate> deletedCandidates) {
        List<String> deletedBinariesSha2 = deletedCandidates.stream()
                .map(DeletedGCCandidate::getDeletedCandidate)
                .filter(Objects::nonNull)
                .map(GCCandidate::getSha2)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        binaryService.deleteBlobInfos(deletedBinariesSha2);
    }

    private GarbageCollectorInfo submitWorkers(GCProvider gcProvider, List<GCCandidate> batch,
            List<Future<DeletedGCCandidate>> futures) {
        GarbageCollectorInfo result = new GarbageCollectorInfo();
        result.candidatesForDeletion = batch.size();
        result.initialCount = -1;
        batch.forEach(
                candidate -> futures.add(executorService.submit(() -> executeTask(gcProvider, candidate, result))));
        return result;
    }

    List<DeletedGCCandidate> waitForWorkers(GCProvider gcProvider, List<Future<DeletedGCCandidate>> futures,
            GarbageCollectorInfo result) {
        log.debug("Waiting for GC workers to finish batch execution");
        ArrayList<DeletedGCCandidate> deletedGCCandidates = new ArrayList<>();
        futures.forEach(future -> {
            try {
                DeletedGCCandidate deletedGCCandidate = future.get();
                if (deletedGCCandidate != null) {
                    deletedGCCandidates.add(deletedGCCandidate);
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Waiting interrupted", e);
                Thread.currentThread().interrupt();
            }
        });
        log.debug("Workers finished one batch.");
        result.gcEndTime = System.currentTimeMillis();
        reportAfterBatch(gcProvider, result);
        return deletedGCCandidates;
    }

    private void reportAfterBatch(GCProvider gcProvider, GarbageCollectorInfo result) {
        if (gcProvider.shouldReportAfterBatch()) {
            result.printCollectionInfo(-1);
        }
    }

    DeletedGCCandidate executeTask(GCProvider gcProvider, GCCandidate gcCandidate,
            GarbageCollectorInfo result) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication originalAuthentication = securityContext.getAuthentication();
        try {
            securityService.authenticateAsSystem();
            try {
                log.debug("Starting GC action on '{}'", gcCandidate);
                boolean binaryDeleted = gcProvider.getAction().accept(gcCandidate, result);
                if (binaryDeleted) {
                    return new DeletedGCCandidate(gcCandidate);
                }
            } catch (Exception e) {
                log.debug("Unable to execute GC action on GC candidate '{}'", gcCandidate, e);
            }
        } finally {
            securityContext.setAuthentication(originalAuthentication);
        }
        return null;
    }

    private ThreadPoolExecutor getExecutorService() {
        return new ThreadPoolExecutor(numberOfThreads, numberOfThreads, 5L, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(ConstantValues.trashcanMaxSearchResults.getInt() * 2),
                new ThreadFactoryBuilder().setDaemon(true)
                        .setNameFormat("art-GC-worker-%s")
                        .build());
    }
}
