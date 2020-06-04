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

package org.artifactory.ui.rest.model.admin.configuration.repository.info;

import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.ui.utils.RegExUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Aviad Shikloshi
 */
public class VirtualRepositoryInfo extends RepositoryInfo {

    protected List<String> selectedRepos;
    private Integer numberOfIncludesRepositories;

    public VirtualRepositoryInfo() {
    }

    public VirtualRepositoryInfo(VirtualRepoDescriptor repoDescriptor) {
        repoKey = repoDescriptor.getKey();
        repoType = repoDescriptor.getType().toString();
        numberOfIncludesRepositories = repoDescriptor.getRepositories().size();
        selectedRepos = repoDescriptor.getRepositories().stream()
                .map(RepoDescriptor::getKey)
                .collect(Collectors.toList());
        hasReindexAction = RegExUtils.VIRTUAL_REPO_REINDEX_PATTERN.matcher(repoType).matches();
    }

    public Integer getNumberOfIncludesRepositories() {
        return numberOfIncludesRepositories;
    }

    public void setNumberOfIncludesRepositories(Integer numberOfIncludesRepositories) {
        this.numberOfIncludesRepositories = numberOfIncludesRepositories;
    }

    public List<String> getSelectedRepos() {
        return selectedRepos;
    }

    public void setSelectedRepos(List<String> selectedRepos) {
        this.selectedRepos = selectedRepos;
    }
}
