package org.artifactory.storage;

/**
 * Represents a processed (GCed deleted binary) {@link GCCandidate}
 *
 * @author Uriah Levy
 */
public class DeletedGCCandidate {

    private GCCandidate processedCandidate;

    public DeletedGCCandidate(GCCandidate processedCandidate) {
        this.processedCandidate = processedCandidate;
    }

    public GCCandidate getDeletedCandidate() {
        return processedCandidate;
    }
}