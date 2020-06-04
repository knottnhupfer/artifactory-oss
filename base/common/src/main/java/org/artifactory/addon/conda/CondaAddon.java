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

package org.artifactory.addon.conda;

import org.artifactory.addon.Addon;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.fs.VfsItem;

/**
 * @author Uriah Levy
 * @author Dudi Morad
 */
public interface CondaAddon extends Addon {

    /**
     * Adds a Conda package to the repository by indexing it.
     * @param fsItem - the item to be added
     */
    default void addCondaPackage(VfsItem fsItem) {
    }

    /**
     * Remove a Conda package from the repository index.
     * @param fsItem - the item to be removed
     */
    default void removeCondaPackage(VfsItem fsItem) {
    }

    /**
     * Trigger re-index of an entire Conda repository.
     * @param repoKey - the repository to be re-indexed
     * @param async - whether to invoke the recalculation asynchronously
     */
    default void reindexRepository(String repoKey, boolean async) {

    }

    /**
     * Extract package metadata for the CONDA info tab
     */
    default CondaMetadataInfo getCondaMetadataToUiModel(RepoPath repoPath) {
        return null;
    }

    default boolean isCondaPackage(String path) {
        return false;
    }
}
