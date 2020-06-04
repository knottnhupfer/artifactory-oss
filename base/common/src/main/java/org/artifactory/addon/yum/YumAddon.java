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

package org.artifactory.addon.yum;

import org.artifactory.addon.Addon;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.RequestContext;

import javax.annotation.Nullable;

/**
 * @author Noam Y. Tenne
 */
public interface YumAddon extends Addon {

    String REPO_DATA_DIR = "repodata/";


    /**
     * Request a Rpm metadata calculation based on specific folders OR entire repo.
     * In cae of entire repo - @param repo should be valid, and we search for the relevant folders according to depth.
     * In case its specific folders - @param repoPaths should exist
     * Order of execution:
     * In case @param repoPaths exist we calculate by it, otherwise by @param repo
     * Each repodata folder calculation will be processed in a separate workQueueItem.
     */
    default void requestRpmMetadataCalculation(LocalRepoDescriptor repo, String passphrase, boolean async, RepoPath... repoPaths) {
    }

    /**
     *  get Rpm file Meta data
     */
    default ArtifactRpmMetadata getRpmMetadata(FileInfo fileInfo) {
        return null;
    }

    /**
     * Triggers an async calculation on {@param requestedPath} using information from {@param requestContext}
     * across all of the virtual's aggregated repos that contain this path.
     */
    default void calculateVirtualYumMetadataAsync(RepoPath requestedPath, @Nullable RequestContext requestContext) {
    }

    /**
     * Triggers a calculation on {@param requestedPath} using information from {@param requestContext}
     * across all of the virtual's aggregated repos that contain this path.
     * This method blocks until the path is deemed calculated which is when either another thread has finished
     * calculating this path (it was already running when this request came) or this thread has calculated it.
     * Once this method returns the index is either available in the virtual cache or it doesn't exist (because nothing
     * was calculated)
     */
    default void calculateVirtualYumMetadata(RepoPath requestedPath, @Nullable RequestContext requestContext) {
    }

    /**
     * Checks if the repo that contains {@param yumRepoRootPath} is aggregated in any virtual repos, and triggers
     * an async calculation on those virtuals if yes.
     */
    default void invokeVirtualCalculationIfNeeded(RepoPath yumRepoRootPath) {
    }
}
