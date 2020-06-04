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

package org.artifactory.addon.smartrepo;

import org.artifactory.addon.Addon;
import org.artifactory.fs.StatsInfo;
import org.artifactory.repo.RepoPath;

/**
 * @author Chen Keinan
 */
public interface SmartRepoAddon extends Addon {

    default boolean supportRemoteStats() {
        return false;
    }

    /**
     * Triggered on remote download event
     *
     * Event queued for local stats update and potential delegation
     *
     * @param statsInfo The {@link StatsInfo} container
     * @param origin    The remote host the download was triggered by
     * @param repoPath  The file repo path to set/update stats
     */
    default void fileDownloadedRemotely(StatsInfo statsInfo, String origin, RepoPath repoPath) {
    }

}
