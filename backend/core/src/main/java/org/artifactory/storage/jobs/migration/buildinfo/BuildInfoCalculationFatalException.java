package org.artifactory.storage.jobs.migration.buildinfo;

/**
 * This exception denotes a fatal, unrecoverable error that deems all related nodes as failures that must be resolved
 * by user intervention
 *
 * @author Yuval Reches
 */
public class BuildInfoCalculationFatalException extends Exception {

    public BuildInfoCalculationFatalException(String message, Throwable cause) {
        super(message, cause);
    }
}
