package org.artifactory.storage.binstore.service;

import org.artifactory.storage.GCCandidate;

/**
 * A function that performs some action on a {@link GCCandidate} and returns true only when the candidate was actually
 * GC'ed.
 *
 * @author Uriah Levy
 */
@FunctionalInterface
public interface GCFunction {

    /**
     * @return true if the binary candidate were deleted, false otherwise
     */
    boolean accept(GCCandidate candidate, GarbageCollectorInfo result);
}