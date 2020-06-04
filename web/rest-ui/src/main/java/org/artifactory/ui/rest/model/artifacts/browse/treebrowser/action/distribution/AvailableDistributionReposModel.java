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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.distribution;

import com.google.common.collect.Lists;
import org.artifactory.repo.RepoPath;

import java.util.List;

/**
 * @author nadavy
 */
public class AvailableDistributionReposModel {
    private boolean offlineMode;
    private boolean distributionRepoConfigured;
    private List<RepoPath> availableDistributionRepos;

    public AvailableDistributionReposModel(List<RepoPath> availableDistributionRepos) {
        this.availableDistributionRepos = availableDistributionRepos;
        this.distributionRepoConfigured = true;
    }

    public AvailableDistributionReposModel(boolean offlineMode, boolean distributionRepoConfigured) {
        this.offlineMode = offlineMode;
        this.distributionRepoConfigured = distributionRepoConfigured;
        availableDistributionRepos = Lists.newArrayList();
    }

    public List<RepoPath> getAvailableDistributionRepos() {
        return availableDistributionRepos;
    }

    public boolean isOfflineMode() {
        return offlineMode;
    }

    public boolean isDistributionRepoConfigured() {
        return distributionRepoConfigured;
    }
}
