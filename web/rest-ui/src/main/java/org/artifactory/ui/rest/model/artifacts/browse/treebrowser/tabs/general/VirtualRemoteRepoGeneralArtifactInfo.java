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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general;

import com.google.common.collect.Lists;
import org.artifactory.api.repo.VirtualRepoItem;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.request.RequestThreadLocal;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.includedrepositories.Repository;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.jfrog.client.util.PathUtils;

import java.util.List;

/**
 * @author Chen Keinan
 */
@JsonTypeName("virtualRemoteRepository")
public class VirtualRemoteRepoGeneralArtifactInfo extends RepositoryGeneralArtifactInfo {

    private String offlineMessage;
    private List<Repository> includedRepositories;

    public VirtualRemoteRepoGeneralArtifactInfo() {
    }

    @Override
    public void populateGeneralData(VirtualRepoItem item) {
        super.populateGeneralData();
        RepoDescriptor repoDescriptor = retrieveRepoService().repoDescriptorByKey(getRepoKey());
        updateVirtualIncludedRepositories(repoDescriptor);
        updateRepositoryOffline(repoDescriptor);
    }

    private void updateVirtualIncludedRepositories(RepoDescriptor repoDescriptor) {
        if (repoDescriptor instanceof VirtualRepoDescriptor) {
            includedRepositories = Lists.newArrayList();
            VirtualRepoDescriptor virtualRepoDescriptor = (VirtualRepoDescriptor) repoDescriptor;
            String baseURL = PathUtils.addTrailingSlash(RequestThreadLocal.getBaseUrl());
            List<RepoDescriptor> repositories = virtualRepoDescriptor.getRepositories();
            for (RepoDescriptor descriptor : repositories) {
                includedRepositories.add(new Repository(descriptor, baseURL + descriptor.getKey()));
            }
        }
    }

    private void updateRepositoryOffline(RepoDescriptor repoDescriptor) {
        if (repoDescriptor != null && repoDescriptor instanceof RemoteRepoDescriptor) {
            if (((RemoteRepoDescriptor) repoDescriptor).isOffline()) {
                setOfflineMessage("This repository is offline, content is served from the cache only.");
            }
        }
    }

    public String getOfflineMessage() {
        return offlineMessage;
    }

    public void setOfflineMessage(String offlineMessage) {
        this.offlineMessage = offlineMessage;
    }

    public List<Repository> getIncludedRepositories() {
        return includedRepositories;
    }

    public void setIncludedRepositories(List<Repository> includedRepositories) {
        this.includedRepositories = includedRepositories;
    }

    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
