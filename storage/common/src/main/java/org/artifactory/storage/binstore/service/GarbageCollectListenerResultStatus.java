package org.artifactory.storage.binstore.service;

/**
 * Status enum for Garbage collection listener call
 */
public enum GarbageCollectListenerResultStatus {
    HAS_MORE_WORK, // listener might have more work to do, i.e. when implementation uses batches
    COMPLETED
}
