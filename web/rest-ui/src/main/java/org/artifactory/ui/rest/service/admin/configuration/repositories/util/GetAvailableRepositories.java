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

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.*;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.info.AvailableRepositories;
import org.artifactory.ui.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Retrieves the available repositories in the Virtual repository creation process
 *
 * @author Aviad Shikloshi
 */
@Component("allAvailableRepositories")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAvailableRepositories implements RestService {

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String type = request.getQueryParamByKey("type");
        if (StringUtils.isBlank(type)) {
            response.responseCode(HttpStatus.SC_BAD_REQUEST).error("Request must specify package type.");
            return;
        }
        Predicate<RepoDescriptor> filter = getFilter(request, type);
        List<String> localRepos = availableRepos(repositoryService.getLocalRepoDescriptors(), filter);
        List<String> remoteRepos = availableRepos(repositoryService.getRemoteRepoDescriptors(), filter);
        String currentRepoKey = RequestUtils.getRepoKeyFromRequest(request);
        List<String> virtualRepos = availableVirtualRepos(currentRepoKey, filter);
        AvailableRepositories allRepos = new AvailableRepositories(localRepos, remoteRepos, virtualRepos);
        response.iModel(allRepos);
    }

    protected Predicate<RepoDescriptor> getFilter(ArtifactoryRestRequest request, String type) {
        return repo -> filterByType(RepoType.valueOf(type), repo);
    }

    private boolean filterByType(RepoType type, RepoDescriptor repo) {
        boolean isGeneric = type.equals(RepoType.Generic);
        if (isGeneric) {
            return true;
        }

        if (type.isMavenGroup()) {
            return repo.getType().isMavenGroup();
        }

        boolean isDocker = type.equals(RepoType.Docker) && repo.getType().equals(RepoType.Docker);
        boolean isDebian = type.equals(RepoType.Debian) && repo.getType().equals(RepoType.Debian);
        if (isDocker) {
            boolean isLocal = repo instanceof LocalRepoDescriptor;
            if (isLocal) {
                return DockerApiVersion.V2.equals(repo.getDockerApiVersion());
            }
        }
        else if (isDebian) {
            boolean isLocal = repo instanceof LocalRepoDescriptor;
            if (isLocal) {
                return  !((LocalRepoDescriptor)repo).isDebianTrivialLayout();
            }
        }

        return repo.getType().equals(type);
    }

    /**
     * List all available repositories that has the same package type as the new repository
     *
     * @param descriptors list of existing repository descriptors
     * @param filter      The filter predicate
     * @return repositories keys
     */
    private List<String> availableRepos(List<? extends RepoDescriptor> descriptors, Predicate<RepoDescriptor> filter) {
        return descriptors.stream()
                .filter(filter)
                .map(RepoDescriptor::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Collect all available virtual repositories keys, excluding the current repo key (in edit mode)
     *
     * @param repoKey the requesting current repo key
     * @param filter  The filter predicate
     * @return list of available keys
     */
    private List<String> availableVirtualRepos(String repoKey, Predicate<RepoDescriptor> filter) {
        return repositoryService.getVirtualRepoDescriptors().stream()
                .filter(filter)
                .map(VirtualRepoDescriptor::getKey)
                .filter(currentKey -> !StringUtils.equals(repoKey, currentKey)) //exclude current to avoid cycles
                .collect(Collectors.toList());
    }
}