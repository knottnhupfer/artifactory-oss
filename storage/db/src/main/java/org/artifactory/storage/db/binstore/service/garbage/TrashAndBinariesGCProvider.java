package org.artifactory.storage.db.binstore.service.garbage;

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.storage.GCCandidate;
import org.artifactory.storage.binstore.service.GCFunction;
import org.artifactory.storage.binstore.service.GCProvider;
import org.artifactory.storage.binstore.service.GarbageCollectorStrategy;
import org.artifactory.storage.binstore.service.InternalBinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dudim
 */
public class TrashAndBinariesGCProvider implements GCProvider {

    private static final Logger log = LoggerFactory.getLogger(TrashAndBinariesGCProvider.class);

    private InternalBinaryService binaryService;
    private RepositoryService repoService;
    private TrashService trashService;

    public TrashAndBinariesGCProvider(RepositoryService repoService, TrashService trashService,
            InternalBinaryService binaryService) {
        this.trashService = trashService;
        this.repoService = repoService;
        this.binaryService = binaryService;
    }

    @Override
    public List<GCCandidate> getBatch() {
        List<GCCandidate> trashCandidates = trashService.getGCCandidatesFromTrash();
        log.debug("Found {} GC candidates from Trash", trashCandidates.size());
        return new ArrayList<>(trashCandidates);
    }

    @Override
    public GCFunction getAction() {
        return (gcCandidate, result) -> {
            trashService.undeployFromTrash(gcCandidate);
            if (!repoService.existsBySha1(gcCandidate.getSha1())) {
                return binaryService.executeBinaryCleaner(gcCandidate, result);
            }
            // item was not garbage collected as another reference exists to this node
            return false;
        };
    }

    @Override
    public boolean shouldReportAfterBatch() {
        return true;
    }

    @Override
    public String getName() {
        return GarbageCollectorStrategy.TRASH_AND_BINARIES.name();
    }
}
