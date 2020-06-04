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

package org.artifactory.api.maven;

import org.artifactory.api.repo.Async;

public interface MavenMetadataService {

    /**
     * Calculate maven metadata on the giver repo path asynchronously.
     *
     * @param workItem contains s path to a folder to start calculating metadata from. Must be a local non-cache repository
     *                       path and a boolean flag which if should calculate recursively
     */
    @Async(delayUntilAfterCommit = true, workQueue = true)
    void calculateMavenMetadataAsync(MavenMetadataWorkItem workItem);

    /**
     * Calculates the maven metadata recursively on all the folders under the input folder.
     * This will also trigger asynchronous maven metadata calculation for maven plugins.
     */
    void calculateMavenMetadata(MavenMetadataWorkItem wi);

    /**
     * Calculate the maven plugins metadata asynchronously after the current transaction is committed. The reason is the
     * metadata calculator uses xpath queries for its job and since the move is not committed yet, the xpath query
     * result might not be accurate (for example when moving plugins from one repo to another the query on the source
     * repository will return the moved plugins while the target repo will not return them). <p/> Note: you should call
     * the markBaseForMavenMetadataRecalculation() before calling this method to recover in case this task is
     * interrupted in the middle.
     */
    @Async(delayUntilAfterCommit = true, workQueue = true)
    void calculateMavenPluginsMetadataAsync(MavenMetadataPluginWorkItem wi);

}
