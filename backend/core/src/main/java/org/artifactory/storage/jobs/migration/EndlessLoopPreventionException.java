package org.artifactory.storage.jobs.migration;

/**
 * @author Dan Feldman
 */
public class EndlessLoopPreventionException extends RuntimeException {

    public EndlessLoopPreventionException(String message) {
        super(message);
    }
}
