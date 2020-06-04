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

package org.artifactory.addon.composer;

import org.artifactory.addon.Addon;
import org.artifactory.repo.RepoPath;

/**
 * @author Shay Bagants
 */
public interface ComposerAddon extends Addon {

    /**
     * Handle new package deployment by marking the artifact with the composer properties and trigger async indexing
     * of the package
     *
     * @param repoPath The file repoPath
     */
    default void handlePackageDeployment(RepoPath repoPath) {
    }

    /**
     * Handle package deletion
     *
     * @param repoPath The repoPath of the deleted package
     */
    default void handlePackageDeletion(RepoPath repoPath) {
    }

    /**
     * Re index the entire repository content
     *
     * @param repoKey The repo key
     * @param async   Calculate async or not
     */
    void recalculateAll(String repoKey, boolean async);

    /**
     * Check whether the file name extension matches the supported extensions
     */
    default boolean isComposerSupportedExtension(String fileName) {
        return false;
    }

    /**
     * Return a model of the metadata info. Should be used to retrieve a package information into the UI tab
     *
     * @param repoPath The path of the artifact to retrieve the metadata
     * @return An ComposerMetadataInfo object with the package metadata information
     */
    default ComposerMetadataInfo getComposerMetadataInfo(RepoPath repoPath) {
        return null;
    }
}
