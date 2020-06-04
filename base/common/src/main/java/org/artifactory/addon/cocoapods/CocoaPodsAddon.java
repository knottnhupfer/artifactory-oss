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

package org.artifactory.addon.cocoapods;

import org.artifactory.addon.Addon;
import org.artifactory.api.repo.Async;
import org.artifactory.fs.FileInfo;
import org.artifactory.md.Properties;

/**
 * @author Dan Feldman
 */
public interface CocoaPodsAddon extends Addon {

    /**
     * Adds a pod to the repository by triggering an index and tagging with properties
     *
     * @param info The added pod file
     */
    default void addPod(FileInfo info) {
    }

    /**
     * Adds a pod to the repository asynchronously.
     *
     * @param info The added pod file
     */
    @Async(delayUntilAfterCommit = true)
    void addPodAfterCommit(FileInfo info);

    /**
     * Used by the remote interceptor to just cache properties on a downloaded pod as there's no need to write an
     * index entry for it.
     * @param info Pod to write properties for
     */
    @Async(delayUntilAfterCommit = true, authenticateAsSystem = true)
    void cachePodProperties(FileInfo info);

    /**
     * Removes a pod's index entry from the repository.
     *
     * @param info          The pod file being removed
     * @param properties    Properties of the pod being removed
     */
    @Async(delayUntilAfterCommit = true)
    void removePod(FileInfo info, Properties properties);

    /**
     * Adds the given repository key into the map of queued reindex requests and trigger the
     * async reindex operation in the service.
     *
     * @param repoKey The repository key to reindex
     */
    void reindexAsync(String repoKey);
}
