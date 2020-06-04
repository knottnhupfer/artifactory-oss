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

package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.vcs.VcsGitProvider;
import org.artifactory.rest.common.util.JsonUtil;

import java.util.List;

import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_GO_METADATA_URLS;
import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_LIST_REMOTE_ITEMS_UNSUPPORTED_TYPE;

/**
 * @author Yuval Reches
 */
public class GoTypeSpecificConfigModel extends VcsTypeSpecificConfigModel {

    public GoTypeSpecificConfigModel() {
        setGitProvider(VcsGitProvider.ARTIFACTORY); // Default
    }

    //remote
    protected Boolean listRemoteFolderItems = DEFAULT_LIST_REMOTE_ITEMS_UNSUPPORTED_TYPE;

    //virtual
    private Boolean enableExternalDependencies = true;
    private List<String> externalPatterns = DEFAULT_GO_METADATA_URLS;

    public Boolean isListRemoteFolderItems() {
        return listRemoteFolderItems;
    }

    public void setListRemoteFolderItems(Boolean listRemoteFolderItems) {
        this.listRemoteFolderItems = listRemoteFolderItems;
    }

    public Boolean getEnableExternalDependencies() {
        return enableExternalDependencies;
    }

    public void setEnableExternalDependencies(Boolean enableExternalDependencies) {
        this.enableExternalDependencies = enableExternalDependencies;
    }

    public List<String> getExternalPatterns() {
        return externalPatterns;
    }

    public void setExternalPatterns(List<String> externalPatterns) {
        this.externalPatterns = externalPatterns;
    }

    public String getExternalRemoteRepo() {
        return null;
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.Go;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }

    private String toAntPattern(String url) {
        return "**/" + url;
    }
}
