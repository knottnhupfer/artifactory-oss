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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general;

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.dependecydeclaration.DependencyDeclaration;
import org.artifactory.ui.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
public class GetDependencyDeclarationService implements RestService {

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        DependencyDeclaration dependencyDeclaration = new DependencyDeclaration();
        //update DependencyDeclaration model data
        updateDependencyDeclarationModel(request, dependencyDeclaration);
        // update response
        response.iModel(dependencyDeclaration);
    }

    /**
     * update DependencyDeclaration model data
     * @param artifactoryRequest - encapsulate data related to request
     * @param dependencyDeclaration - dependencyDeclaration model
     */
    private void updateDependencyDeclarationModel(ArtifactoryRestRequest artifactoryRequest,
            DependencyDeclaration dependencyDeclaration) {
        RepoPath repoPath = RequestUtils.getPathFromRequest(artifactoryRequest);
        if(repositoryService.isVirtualRepoExist(repoPath.getRepoKey())){
            repoPath = repositoryService.getVirtualFileInfo(repoPath).getRepoPath();
        }
        LocalRepoDescriptor localRepoDescriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoPath.getRepoKey());
        String buildtool = artifactoryRequest.getQueryParamByKey("buildtool");
        dependencyDeclaration.updateDependencyDeclaration(buildtool, repoPath, localRepoDescriptor);
        dependencyDeclaration.setTypes(null);
    }

}
