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

package org.artifactory.addon.bower;

import org.artifactory.addon.Addon;
import org.artifactory.api.repo.Async;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;

import javax.annotation.Nullable;

/**
 * Core Bower functionality interface
 *
 * @author Shay Yaakov
 */
public interface BowerAddon extends Addon {

    /**
     * Adds a bower package to the repository and indexing it by tagging name and version properties.
     *
     * @param info      The added bower package file
     * @param version   The version contained in this artifact's bower.version property, if exists
     */
    default void addBowerPackage(FileInfo info, @Nullable String version) {
    }

    /**
     *
     * Adds a bower package to the repository asynchronously, delegates to the indexing for properties extraction.
     *
     * @param info      The added bower package file
     * @param version   The version contained in this artifact's bower.version property, if exists
     */
    @Async(delayUntilAfterCommit = true)
    void handleAddAfterCommit(FileInfo info, @Nullable String version);

    /**
     * Removes a bower package from the repository.
     *
     * @param info The bower package file to be removed
     */
    default void removeBowerPackage(FileInfo info) {
    }

    /**
     * Checks if a given path is a valid bower file according to it's extension (tar.gz, tgz, zip).
     *
     * @param filePath The file path to check
     */
    default boolean isBowerFile(String filePath) {
        return false;
    }

    /**
     * Adds the given repository key into the map of queued reindex requests and trigger asynchronously
     * {@link BowerService#asyncReindex()} for actual re-indexing.
     *
     * @param repoKey The repository key to reindex
     */
    default void requestAsyncReindexBowerPackages(String repoKey) {
    }

    /**
     * get bower meta data info
     *
     * @param repoPath - bower file info
     * @return bower meta data info
     */
    default BowerMetadataInfo getBowerMetadata(RepoPath repoPath) {
        return null;
    }
}