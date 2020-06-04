package org.artifactory.storage.db.binstore.service.garbage;

import org.artifactory.storage.GCCandidate;
import org.jfrog.storage.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author uriahl & dudim
 */
class GCStreamingQueryHelper {
    private static final Logger log = LoggerFactory.getLogger(GCStreamingQueryHelper.class);
    private ResultSet resultSet;
    private boolean isFinished = false;
    private int batchSize;
    private Supplier<ResultSet> batchProvider;
    private Function<ResultSet, GCCandidate> resultSetReader;

    GCStreamingQueryHelper(int batchSize, Supplier<ResultSet> batchProvider,
            Function<ResultSet, GCCandidate> resultSetReader) {
        this.batchSize = batchSize;
        this.batchProvider = batchProvider;
        this.resultSetReader = resultSetReader;
    }

    List<GCCandidate> nextBatch() {
        List<GCCandidate> gcCandidates = new ArrayList<>();
        if (resultSet == null) {
            resultSet = batchProvider.get();
            if (resultSet == null) {
                log.debug("Streaming GC Provider result set is null");
                return new ArrayList<>();
            }
        }
        if (isFinished) {
            return new ArrayList<>();
        }
        try {
            // take another bulk from result set
            int numberOfRecords = 0;
            while (numberOfRecords < batchSize && resultSet.next()) {
                GCCandidate gcCandidate = resultSetReader.apply(resultSet);
                if (gcCandidate.getRepoPath() == null) {
                    collectBinaryCandidate(gcCandidates, gcCandidate);
                } else {
                    collectTrashCandidate(gcCandidates, gcCandidate);
                }
                numberOfRecords++;
            }
            if (gcCandidates.size() < batchSize) {
                log.info("Reached the end of GCCandidates stream");
                isFinished = true;
            }
        } catch (SQLException e) {
            log.error("Error while iterating GCCandidates stream due to: {}", e.getMessage());
            log.debug("", e);
            reset();
        }
        return gcCandidates;
    }

    private void collectBinaryCandidate(List<GCCandidate> gcCandidates, GCCandidate gcCandidate) {
        // not trash item, add binary candidate
        gcCandidates.add(gcCandidate);
    }

    private void collectTrashCandidate(List<GCCandidate> gcCandidates, GCCandidate gcCandidate) {
        if (TrashUtil.isTrashItem(gcCandidate)) {
            gcCandidates.add(gcCandidate);
        }
    }

    public void reset() {
        DbUtils.close(resultSet);
        resultSet = null;
        isFinished = false;
    }

    public boolean isFinished() {
        return isFinished;
    }
}
