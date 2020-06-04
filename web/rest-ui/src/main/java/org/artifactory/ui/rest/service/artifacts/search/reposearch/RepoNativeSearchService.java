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

package org.artifactory.ui.rest.service.artifacts.search.reposearch;

import com.google.common.collect.Lists;
import org.apache.http.HttpStatus;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.ReposNativeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeRestConstants.TYPE;

/**
 * @author ortalh
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RepoNativeSearchService implements RestService {

    private RepositoryService repoService;
    private AuthorizationService authorizationService;

    @Autowired
    public RepoNativeSearchService(RepositoryService repoService, AuthorizationService authorizationService) {
        this.repoService = repoService;
        this.authorizationService = authorizationService;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RepoType repoType;
        String type = request.getPathParamByKey(TYPE);
        if (type != null) {
            repoType = RepoType.fromType(type);
        } else {
            response.responseCode(HttpStatus.SC_BAD_REQUEST);
            response.error("Repository type cannot be empty");
            return;
        }
        List<String> searchRepos = getSearchRepos(repoType);
        ReposNativeModel model = new ReposNativeModel(searchRepos, searchRepos.size());
        response.iModel(model);
    }

    private List<String> getSearchRepos(RepoType repoType) {
        List<LocalRepoDescriptor> repoList = Lists.newArrayList();
        List<LocalRepoDescriptor> localAndCachedRepoDescriptors = repoService.getLocalAndCachedRepoDescriptors();
        localAndCachedRepoDescriptors.addAll(repoService.getDistributionRepoDescriptors());
        localAndCachedRepoDescriptors.stream()
                .filter(descriptor -> descriptor.getType().equals(repoType))
                .filter(descriptor -> authorizationService.canRead(InternalRepoPathFactory.repoRootPath(descriptor.getKey())))
                .forEach(repoList::add);
        List<String> repoKeys = Lists.newArrayList();
        for (LocalRepoDescriptor descriptor : repoList) {
            repoKeys.add(descriptor.getKey());
        }
        return repoKeys;
    }
}
