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

package org.artifactory.storage.fs.service;

import org.artifactory.fs.FileInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.Request;

import javax.annotation.Nullable;

/**
 * A business service to interact with file statistics.
 *
 * @author Yossi Shaul
 */
public interface StatsService {

    /**
     * Retrieves stats of the specified file info. This method is more efficient than simply retrieving by
     * repo path. Prefer it over {@link StatsService#getStats(org.artifactory.repo.RepoPath)}.
     *
     * @param fileInfo The file to get stats on
     * @return The {@link org.artifactory.fs.StatsInfo} of this file. Null if non exist
     */
    @Nullable
    StatsInfo getStats(FileInfo fileInfo);

    /**
     * @param repoPath The file repo path to get stats on
     * @return The {@link org.artifactory.fs.StatsInfo} of this node. Null if non exist
     */
    @Nullable
    StatsInfo getStats(RepoPath repoPath);

    /**
     * Update the download stats and increment the count by one. The storage update is not immediate.
     *
     * @param repoPath       The file repo path to set/update stats
     * @param downloadedBy   User who downloaded the file
     * @param downloadedTime Time the file was downloaded
     * @param fromAnotherArtifactory specifying whether request comes fromAnotherArtifactory
     */
    void fileDownloaded(RepoPath repoPath, String downloadedBy, long downloadedTime, boolean fromAnotherArtifactory);

    /**
     * Calls fileDownloaded in case:
     * 1. Its a real download request (not HEAD request, not internal - system request)
     * 2. Download stats is enabled globally
     * 3. Not Xray request (When Xray downloads artifacts we treat it as an internal request)
     * 4. The request is not marked as "ignore download stats"
     */
    void updateDownloadStatsIfNeeded(RepoPath repoPath, Request request, boolean isRealRepo);

    /**
     * Update the download (performed at remote artifactory instance) stats and increment the count by one.
     * The storage update is not immediate.
     *
     * @param origin           The remote host the download was triggered by
     * @param path             The round trip of download request
     * @param repoPath         The file repo path to set/update stats
     * @param downloadedBy     User who downloaded the file
     * @param downloadedTime   Time the file was downloaded
     * @param count            Amount of performed downloads
     */
    void fileDownloadedRemotely(String origin, String path, RepoPath repoPath, String downloadedBy, long downloadedTime, long count);

    /**
     * Sets the stats details on the given repo path. Existing statistics on this node are overridden.
     * @param repoPath  The repo path to set the stats on
     * @param statsInfo The stats info
     * @return {@code true} if the stats were set successfully, {@code false} otherwise (e.g. repo path not found)
     */
    boolean setStats(RepoPath repoPath, StatsInfo statsInfo);

    /**
     * Sets the stats details on the given node. Existing statistics on this node are overridden.
     *
     * @param nodeId    The node id to set stats on.
     * @param statsInfo The stats info
     * @return Updates rows count. Any value other than 1 is an error
     */
    int setStats(long nodeId, StatsInfo statsInfo);

    /**
     * Deletes statistics info for the specified noe.
     *
     * @param nodeId The node id
     * @return True if statistics were deleted from the database/ False otherwise (node doesn't exist or has no stats).
     */
    boolean deleteStats(long nodeId);

    /**
     * @param repoPath The repo path to check
     * @return True if the item represented by this item has stats info. Folders never have stats info
     */
    boolean hasStats(RepoPath repoPath);

    /**
     * Flushes the collected statistics event from the memory to the backing storage.
     */
    void flushStats();
}
