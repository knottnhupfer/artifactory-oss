package org.artifactory.sha2;

import org.artifactory.api.repo.Async;
import org.artifactory.api.rest.sha2.Sha256MigrationModel;
import org.artifactory.schedule.TaskBase;

/**
 * @author Inbar Tal
 */
public interface Sha256MigrationTaskHelper {

    @Async
    void stopMigrationTask(long sleepIntervalMillis);

    TaskBase createManualTask(Sha256MigrationModel model);

    boolean shouldPropagateStartRequest(String serverId);

    boolean shouldPropagateStopRequest();

    void assertNotAol();

    boolean jobIsAlreadyRunning();
}