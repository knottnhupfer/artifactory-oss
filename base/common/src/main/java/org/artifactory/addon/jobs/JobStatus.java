package org.artifactory.addon.jobs;


/**
 * @author Yoaz Menda
 */
public enum JobStatus {
    RUNNING, // currently running
    STOPPED, // Replication was stopped due to StopReplicationException exception by user or plugin
    FAILED, // Replication crashed with exception
    FINISHED // replication has finished with or without errors
}

