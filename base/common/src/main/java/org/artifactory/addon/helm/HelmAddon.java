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

package org.artifactory.addon.helm;

import org.artifactory.addon.Addon;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.fs.VfsItem;

/**
 * @author Nadav Yogev
 * @author Yuval Reches
 */
public interface HelmAddon extends Addon {

    /**
     * Adds a Helm chart to the registry, index it and add it to the index.yaml
     * Adding the Chart to a queue of events and invoking local index calculation
     *
     * @param fsItem Chart file added
     */
    default void addHelmPackage(VfsItem fsItem) {
    }

    /**
     * Removes a Helm Chart from the repository and the index.yaml
     * Adding the Chart's name and version to a queue of events and invoking local index calculation
     * The removal is based on a Chart's name and version.
     * Those values are either extracted from the file's properties, or from the metadata itself if still present.
     * In case both are missing --> Chart won't be removed from index file.
     */
    default void removeHelmPackage(VfsItem fsItem) {
    }

    /**
     * Request Helm metadata calculation based on a repo key.
     * We use the Helm Service and going threw the Async annotations based on the async param
     */
    default void requestHelmMetadataCalculation(RepoPath path, boolean async) {
    }

    /**
     * Request Helm metadata virtual calculation based on a request url.
     * We generate a index file with URLs based on the requestUrl.
     */
    default RepoPath requestVirtualHelmCustomMetadataCalculation(RepoPath path, String requestUrl) {
        return path;
    }

    /**
     * Request Helm metadata calculation of the entire repo
     * We use the Helm Service and going threw the Async annotations based on the async param
     */
    default void requestReindexRepo(String repoKey, boolean async) {
    }

    /**
     * Used to evict all virtual repositories custom index.yaml files for all virtual repositories containing the
     * given repository, to force recalculation of all custom request url based requests.
     */
    default void invokeVirtualMetadataEviction(RealRepoDescriptor descriptor) {
    }

    /**
     * Used to extract helm metadata for a given helm chart
     */
    default HelmMetadataInfo getMetadataToUiModel(RepoPath repoPath) {
        return null;
    }

    /**
     * Checks if file ends with "tgz" or "tar.gz"
     */
    default boolean isHelmFile(String fileName) {
        return false;
    }
}
