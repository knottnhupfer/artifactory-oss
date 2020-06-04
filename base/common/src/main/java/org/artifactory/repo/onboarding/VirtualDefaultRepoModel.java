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

package org.artifactory.repo.onboarding;

import java.util.List;

/**
 * @author nadavy
 */
public class VirtualDefaultRepoModel {
    private String repoKey;
    private String defaultDeployment;
    private List<String> includedLocalRepos;
    private List<String> includedRemoteRepos;

    public VirtualDefaultRepoModel(String repoKey, String defaultDeployment,
            List<String> includedLocalRepos, List<String> includedRemoteRepos) {
        this.repoKey = repoKey;
        this.defaultDeployment = defaultDeployment;
        this.includedLocalRepos = includedLocalRepos;
        this.includedRemoteRepos = includedRemoteRepos;
    }

    public VirtualDefaultRepoModel() {
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getDefaultDeployment() {
        return defaultDeployment;
    }

    public void setDefaultDeployment(String defaultDeployment) {
        this.defaultDeployment = defaultDeployment;
    }

    public List<String> getIncludedLocalRepos() {
        return includedLocalRepos;
    }

    public void setIncludedLocalRepos(List<String> includedLocalRepos) {
        this.includedLocalRepos = includedLocalRepos;
    }

    public List<String> getIncludedRemoteRepos() {
        return includedRemoteRepos;
    }

    public void setIncludedRemoteRepos(List<String> includedRemoteRepos) {
        this.includedRemoteRepos = includedRemoteRepos;
    }
}
