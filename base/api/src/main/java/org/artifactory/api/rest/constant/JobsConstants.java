package org.artifactory.api.rest.constant;


/**
 * @author Yoaz Menda
 */
public interface JobsConstants {

    String JOBS = "jobs";
    String REPLICATION_JOBS = "replications";

    //general jobs params
    String STARTED_AFTER = "startedAfter";
    String FINISHED_BEFORE = "finishedBefore";
    String JOB_STATUS = "jobStatus";

    //replication job params
    String REPLICATION_TYPE = "replicationType";
    String REPLICATION_STRATEGY = "replicationStrategy";
    String SOURCE_REPO = "sourceRepo";
    String TARGET_URL = "targetUrl";

}
