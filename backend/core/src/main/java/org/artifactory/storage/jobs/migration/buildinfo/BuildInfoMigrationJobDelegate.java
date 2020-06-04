package org.artifactory.storage.jobs.migration.buildinfo;

import org.slf4j.Logger;

/**
 * Passed as delegate to {@link org.artifactory.api.repo.RepositoryService} to avoid polluting it with logic that
 * belongs in this job but must run async via a service.
 *
 * @author Yuval Reches
 */
public interface BuildInfoMigrationJobDelegate {

    void incrementTotalDone();

    void incrementCurrentBatchCount();

    Logger log();

    /**
     * Tries to retrieve the Build Info JSON matching this {@param buildId} from the Build Info repository.
     * if it doesn't exist, the Build info json value is retrieved from db and written into /_temp.
     * Then it is moved to the proper coordinates in the repo (move is done in order to avoid interceptor's afterCreate)
     *
     * @throws BuildInfoCalculationFatalException on any fatal error that requires user intervention.
     */
    void migrateBuildJsonToRepo(long buildId) throws BuildInfoCalculationFatalException;

    /**
     * Migration-specific logic for handling exceptions.
     * @param buildId - the problematic build
     * @param e - the exception
     */
    void handleExceptionDuringMigration(long buildId, Exception e);


    /**
     * Each task should be marked as finished
     * Since we use async workQueue we need a way to wait until all tasks are done
     * @param taskId - task id to mark as finished
     */
    void markTaskAsFinished(String taskId);
}
