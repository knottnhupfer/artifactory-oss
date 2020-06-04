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

package org.artifactory.addon.chef;

import org.artifactory.addon.Addon;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;

/**
 * @author Alexis Tual
 */
public interface ChefAddon extends Addon {

    default void addCookbook(FileInfo info) {
    }

    /**
     * @param repoPath path to an Artifact
     * @return the Chef Cookbook informations corresponding to the given Artifact.
     */
    default ChefCookbookInfo getChefCookbookInfo(RepoPath repoPath) {
        return null;
    }

    /**
     * @param fileName file name
     * @return true if the filename corresponds to a Chef Cookbook
     */
    default boolean isChefCookbookFile(String fileName) {
        return false;
    }

    /**
     * Should recalculate the Cookbook for all index in repo
     *
     * @param repoKey    a repo key
     * @param indexAsync true if the indexing will be done asynchronously, false to wait for the outcome of the index.
     */
    void recalculateAll(String repoKey, boolean indexAsync);

    /**
     * Calculate the root index (api/v1/cookbooks) for a virtual repository from all the aggregated repositories.
     */
    default void calculateVirtualRepoMetadata(String repoKey, String baseUrl) {
    }
}
