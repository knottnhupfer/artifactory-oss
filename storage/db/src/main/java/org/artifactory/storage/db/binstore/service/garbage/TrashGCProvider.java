package org.artifactory.storage.db.binstore.service.garbage;

import org.artifactory.repo.trash.TrashService;
import org.artifactory.storage.GCCandidate;
import org.artifactory.storage.binstore.service.GCFunction;
import org.artifactory.storage.binstore.service.GCProvider;
import org.artifactory.storage.binstore.service.GarbageCollectorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dudim
 */
public class TrashGCProvider implements GCProvider {
    private static final Logger log = LoggerFactory.getLogger(TrashAndBinariesGCProvider.class);

    private TrashService trashService;

    public TrashGCProvider(TrashService trashService) {
        this.trashService = trashService;
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
            return false;
        };
    }

    @Override
    public boolean shouldReportAfterBatch() {
        return false;
    }

    @Override
    public String getName() {
        return GarbageCollectorStrategy.TRASH.name();
    }
}
