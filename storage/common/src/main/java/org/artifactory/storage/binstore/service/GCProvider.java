package org.artifactory.storage.binstore.service;

import org.artifactory.storage.GCCandidate;

import java.util.List;

/**
 * @author dudim
 */
public interface GCProvider {

    /**
     * Fetch batch of GC candidates that requires GC cleanup action
     *
     * @return batch of candidates for cleanup
     */
    List<GCCandidate> getBatch();

    /**
     * @return consumer that perform cleanup action on candidates
     */
    GCFunction getAction();

    /**
     * Whether or not to print the binaries GC report.
     */
    boolean shouldReportAfterBatch();

    String getName();
}
