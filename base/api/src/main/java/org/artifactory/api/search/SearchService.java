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

package org.artifactory.api.search;

import org.artifactory.api.rest.search.common.RestDateFieldName;
import org.artifactory.api.search.archive.ArchiveSearchControls;
import org.artifactory.api.search.archive.ArchiveSearchResult;
import org.artifactory.api.search.artifact.ArtifactSearchControls;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.api.search.artifact.ChecksumSearchControls;
import org.artifactory.api.search.exception.InvalidChecksumException;
import org.artifactory.api.search.gavc.GavcSearchControls;
import org.artifactory.api.search.gavc.GavcSearchResult;
import org.artifactory.api.search.property.PropertySearchControls;
import org.artifactory.api.search.property.PropertySearchResult;
import org.artifactory.api.search.stats.StatsSearchResult;
import org.artifactory.aql.result.rows.AqlStatistics;
import org.artifactory.build.BuildRun;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.common.RepositoryRuntimeException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

/**
 * @author Noam Tenne
 */
public interface SearchService {

    /**
     * @param controls Search data (mainly the search term)
     * @return Artifacts found by the search, empty list if nothing was found
     */
    ItemSearchResults<ArtifactSearchResult> searchArtifacts(ArtifactSearchControls controls);

    /**
     * Searches for artifacts by their checksum values (Used by REST)
     *
     * @param searchControls Search controls
     * @return Set of repo paths that comply with the given checksums
     */
    Set<RepoPath> searchArtifactsByChecksum(ChecksumSearchControls searchControls) throws InvalidChecksumException;

    /**
     * Searches for artifacts by their checksum values (Used by UI and Trashcan)
     *
     * @param searchControls Search controls
     * @return List of ItemSearchResults
     */
    ItemSearchResults<ArtifactSearchResult> getArtifactsByChecksumResults(ChecksumSearchControls searchControls) throws InvalidChecksumException;

    /**
     * Search for files with dates fields passed that are included in the date range provided.
     * One of the from or to date is mandatory.
     *
     * @param from          The time to start the search exclusive (eg, >). If empty will start from 1st Jan 1970
     * @param to            The time to end search inclusive (eg, <=), If empty, will not use current time as the limit
     * @param reposToSearch Lists of repositories to search within
     * @param dates         Lists of date field names to look for
     * @return List of artifacts that have a date between the input time range and the date the file
     * was modified. Empty if none is found.
     */
    ItemSearchResults<ArtifactSearchResult> searchArtifactsInRange(
            Calendar from,
            Calendar to,
            List<String> reposToSearch,
            RestDateFieldName... dates);

    Stream<AqlStatistics> getRemoteDownloadedSinceEventsStatistics(@Nonnull RepoPath rootRepoPath, long lastReplicationTime, long limit);

    Stream<AqlStatistics> getDownloadedSinceEventsStatistics(@Nonnull RepoPath rootRepoPath, long lastReplicationTime, long limit);

    Stream<StatsSearchResult> streamStatsForArtifactsNotDownloadedSince(@Nullable Calendar since,
                                                                        @Nullable Calendar createdBefore,
                                                                        String... repositories);

    /**
     * Find artifacts not downloaded since the specified date, searches for both locally and remotely downloaded stats,
     * using smart remote repository stats where applicable.
     *
     * @param since         The time to start the search exclusive (eg, >). If null will start from 1st Jan 1970
     * @param createdBefore Only include artifacts created before the specified time. If null will default to the value
     *                      of since.
     * @param repositories  A list of repositories to search in. Can be null to search all repositories
     * @return Stream<RepoPath> of file repo paths that were not downloaded since the specified date
     */
    Stream<RepoPath> streamArtifactsRepoPathNotDownloadedSince(@Nullable Calendar since, @Nullable Calendar createdBefore,
                                                               String... repositories);

    /**
     * Find artifacts not downloaded since the specified date, searches for both locally and remotely downloaded stats,
     * using smart remote repository stats where applicable.
     *
     * @param since         The time to start the search exclusive (eg, >). If null will start from 1st Jan 1970
     * @param createdBefore Only include artifacts created before the specified time. If null will default to the value
     *                      of since.
     * @param repositories  A list of repositories to search in. Can be null to search all repositories
     * @return Stream<ItemInfo></ItemInfo> of file repo paths that were not downloaded since the specified date
     */
    Stream<ItemInfo> streamArtifactsItemInfoNotDownloadedSince(@Nullable Calendar since, @Nullable Calendar createdBefore,
                                                               String... repositories);

    ItemSearchResults<ArchiveSearchResult> searchArchiveContent(ArchiveSearchControls controls);

    ItemSearchResults<ArchiveSearchResult> searchArchiveContentAql(ArchiveSearchControls controls);

    ItemSearchResults<GavcSearchResult> searchGavc(GavcSearchControls controls);

    ItemSearchResults<PropertySearchResult> searchProperty(PropertySearchControls controls);

    ItemSearchResults<PropertySearchResult> searchPropertyAql(PropertySearchControls controls);

    Set<BuildRun> getLatestBuilds() throws RepositoryRuntimeException;

    /**
     * Returns a filtered list according to user's permissions
     */
    Set<BuildRun> findBuildsByArtifactChecksum(@Nullable String sha1, @Nullable String sha2, @Nullable String md5) throws RepositoryRuntimeException;

    /**
     * Returns a filtered list according to user's permissions
     */
    Set<BuildRun> findBuildsByDependencyChecksum(@Nullable String sha1, @Nullable String sha2, @Nullable String md5) throws RepositoryRuntimeException;

    /**
     * Search for artifacts within a repository matching a given pattern.<br> The pattern should be like
     * repo-key:this/is/a/pattern
     *
     * @param pattern Pattern to search for
     * @return Set of matching artifact paths relative to the repo
     */
    Set<String> searchArtifactsByPattern(String pattern) throws ExecutionException, InterruptedException,
            TimeoutException;

    Stream<AqlStatistics> getTimestampStatsEvents(RepoPath repoPath, long lastStatsTime, boolean remote);
}
