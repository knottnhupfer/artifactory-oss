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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions.distribution;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.distribution.AvailableDistributionReposModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAvailableDistributionReposService implements RestService {

    @Autowired
    private RepositoryService repoService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        boolean offlineMode = centralConfigService.getDescriptor().isOfflineMode();
        List<DistributionRepoDescriptor> distributionRepoDescriptors = repoService.getDistributionRepoDescriptors();
        boolean noDistributionRepos = distributionRepoDescriptors.isEmpty();
        if (offlineMode || noDistributionRepos) {
            response.iModel(new AvailableDistributionReposModel(offlineMode, !noDistributionRepos));
        } else {
            response.iModel(new AvailableDistributionReposModel(distributionRepoDescriptors.stream()
                    .map(RepoDescriptor::getKey)
                    .map(RepoPathFactory::create)
                    .filter(authorizationService::canDeploy)
                    .collect(Collectors.toList())));
        }
    }
}
