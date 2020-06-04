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
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.VirtualRepoItem;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.request.RequestThreadLocal;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.BaseInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.virtualrepositories.VirtualRepository;
import org.jfrog.client.util.PathUtils;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class GeneralArtifactInfo extends BaseArtifactInfo implements RestGeneralTab {

    private BaseInfo info;
    private List<VirtualRepository> virtualRepositories = Lists.newArrayList();

    public GeneralArtifactInfo() {
    }

    @Override
    public void populateGeneralData() {

    }

    @Override
    public void populateGeneralData(VirtualRepoItem item) {

    }

    @Override
    public RepoPath retrieveRepoPath() {
        return RepoPathFactory.create(getRepoKey(), getPath());
    }

    protected RepositoryService retrieveRepoService() {
        return ContextHelper.get().beanForType(RepositoryService.class);
    }

    protected CentralConfigService retrieveCentralConfigService() {
        return ContextHelper.get().beanForType(CentralConfigService.class);
    }

    protected void populateVirtualRepositories(RepoDescriptor descriptor) {
        if(descriptor != null) {
            String baseURL = PathUtils.addTrailingSlash(RequestThreadLocal.getBaseUrl());
            List<VirtualRepoDescriptor> virtuals = retrieveRepoService().getVirtualReposContainingRepo(descriptor);
            for (VirtualRepoDescriptor virtualRepoDescriptor : virtuals) {
                virtualRepositories.add(new VirtualRepository(virtualRepoDescriptor.getKey(), baseURL + virtualRepoDescriptor.getKey()));
            }
        }
    }

    public BaseInfo getInfo() {
        return info;
    }

    public void setInfo(BaseInfo info) {
        this.info = info;
    }

    public List<VirtualRepository> getVirtualRepositories() {
        return virtualRepositories;
    }

    public void setVirtualRepositories(List<VirtualRepository> virtualRepositories) {
        this.virtualRepositories = virtualRepositories;
    }
}
