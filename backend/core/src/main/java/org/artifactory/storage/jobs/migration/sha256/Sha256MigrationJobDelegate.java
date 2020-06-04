/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.storage.jobs.migration.sha256;

import org.artifactory.repo.RepoPath;
import org.slf4j.Logger;

import java.util.Collection;

/**
 * Passed as delegate to {@link org.artifactory.api.repo.RepositoryService} to avoid polluting it with logic that
 * belongs in this job but must run async via a service.
 *
 * @author Dan Feldman
 */
public interface Sha256MigrationJobDelegate {

    void markFatalErrorOnPaths(Collection<RepoPath> paths, String error);

    void markNonFatalErrorOnPaths(Collection<RepoPath> paths, String error);

    void incrementTotalDone();

    void incrementCurrentBatchCount();

    Logger log();

    /**
     * Tries to retrieve the sha2 value matching this {@param sha1} from the binaries table, if it doesn't exist
     * the binary matching the sha1 value is retrieved from the binarystore and the sha2 is calculated from the
     * incoming stream.
     *
     * @throws Sha256CalculationFatalException on any fatal error that requires user intervention.
     */
    String getOrCalculateSha2(String sha1) throws Sha256CalculationFatalException;

    /**
     * Migration-specific logic for handling exceptions.
     * @param paths - the problematic path(s)
     * @param e - the exception
     */
    void handleExceptionDuringMigration(Collection<RepoPath> paths, Exception e);

    /**
     * Each task should be marked as finished
     * Since we use async workqueue we need a way to wait until all tasks are done
     * @param taskId - task id to mark as finished
     */
    void markTaskAsFinished(String taskId);

}
