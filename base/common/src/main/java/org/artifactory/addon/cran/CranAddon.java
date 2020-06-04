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

package org.artifactory.addon.cran;

import org.artifactory.addon.Addon;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.fs.VfsItem;

/**
 * @author Inbar Tal
 */
public interface CranAddon extends Addon {
    /**
     * Adds a cran package to the repository and indexing it. /todo complete it
     * @param fsItem The added cran package file
     */
    default void addCranPackage(VfsItem fsItem) {
    }

    default void removeCranPackage(VfsItem fsItem) {
    }

    /**
     * Checks if file ends with "tar.gz" /todo what about other extensions?
     */
    default boolean isCranSourceFile(String fileName) {
        return false;
    }

    /**
     * Checks if file ends with "tgz" or "zip"
     */
    default boolean isCranBinaryFile(String fileName) {
        return false;
    }

    /**
     * Checks if file is temporary - only for the upload
     */
    default boolean isCranTempFile(String fileName) {
        return false;
    }

    /**
     * Request Cran metadata calculation of the entire repo
     * We use the Cran Service and going threw the Async annotations based on the async param /todo check this comment
     */
    default void reindexRepo(String repoKey, boolean async) {
    }

    /**
     * Extract package metadata for the CRAN info tab
     */
    default CranMetadataInfo getCranMetadataToUiModel(RepoPath path) {
        return null;
    }

    /**
     * Clean given virtual repository cached PACKAGES file
     */
    default void invokeCranVirtualMetadataEviction(RealRepoDescriptor descriptor, RepoPath indexFile) {
    }

    /**
     * Triggers a calculation on {@param requestedPath} using information from {@param requestContext}
     * across all of the virtual's aggregated repos that contain this path.
     * This method blocks until the path is deemed calculated which is when either another thread has finished
     * calculating this path (it was already running when this request came) or this thread has calculated it.
     * Once this method returns the index is either available in the virtual cache or it doesn't exist (because nothing
     * was calculated)
     */
    default void calculateVirtualCranMetadata(RepoPath requestedPath) {
    }

    /**
     * Build the PACKAGES cache path according to the user read permissions on each of the
     * repositories included in the virtual repository.
     * @param repoKey - The virtual repository
     * @param repoPath - The PACKAGES file path
     */
    default RepoPath getPackagesCachePath(String repoKey, RepoPath repoPath) {
        return null;
    }
}
