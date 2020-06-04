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

package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.docker.repo.DockerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetDockerRepoService implements RestService {

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private RepositoryService repoService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoKey = request.getQueryParamByKey("repoKey");
        DockerRepo dockerRepo = new DockerRepo();
        dockerRepo.setHostname(ContextHelper.get().beanForType(AddonsManager.class).addonByType(CoreAddons.class).getArtifactoryServerName());
        if (repoService.localRepoDescriptorByKey(repoKey) != null) {
            boolean deployToLocal = configService.getDescriptor().getVirtualRepositoriesMap().values().stream()
                    .filter(descriptor -> RepoType.Docker.equals(descriptor.getType()))
                    .anyMatch(descriptor -> {
                        LocalRepoDescriptor defaultDeploymentRepo = descriptor.getDefaultDeploymentRepo();
                        return defaultDeploymentRepo != null && repoKey.equals(defaultDeploymentRepo.getKey());
                    });
            dockerRepo.setDeployToLocal(deployToLocal);
        }

        response.responseCode(HttpStatus.SC_OK);
        response.iModel(dockerRepo);
    }
}