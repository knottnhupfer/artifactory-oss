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

package org.artifactory.storage.service;

import org.artifactory.repo.RepoPath;

/**
 * Delegates statistics events to registered members
 *
 * @author Michael Pasternak
 */
public interface StatsDelegatingService {
    /**
     * Update the download (performed at remote artifactory instance) stats and increment the count by one.
     * The storage update is not immediate.
     *
     * @param origin         The origin hosts
     * @param path           The round trip of download request
     * @param repoPath       The file repo path to set/update stats
     * @param downloadedBy   User who downloaded the file
     * @param downloadedTime Time the file was downloaded
     * @param count          Amount of performed downloads
     */
    void fileDownloaded(String origin, String path, RepoPath repoPath, String downloadedBy, long downloadedTime, long count);

    /**
     * Flushes the collected statistics event from the memory to the backing storage.
     */
    void flushStats();
}
