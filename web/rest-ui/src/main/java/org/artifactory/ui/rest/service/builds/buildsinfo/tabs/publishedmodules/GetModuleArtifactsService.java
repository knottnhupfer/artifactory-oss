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

package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.publishedmodules;

import org.artifactory.api.build.BuildService;
import org.artifactory.api.build.ModuleArtifact;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.PagingModel;
import org.artifactory.ui.rest.model.builds.ModuleArtifactModel;
import org.artifactory.ui.utils.ActionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetModuleArtifactsService implements RestService {

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String buildName = request.getPathParamByKey("name");
        String buildNumber = request.getPathParamByKey("number");
        String buildStarted = request.getPathParamByKey("date");
        String moduleId = request.getPathParamByKey("id");
        // fetch artifact module data
        fetchModuleArtifact(request, response, buildNumber, buildStarted, moduleId, buildName);
    }

    /**
     * fetch Module artifact data
     *
     * @param artifactoryRequest  - encapsulate data related to request
     * @param artifactoryResponse - encapsulate data related to response
     * @param buildNumber         - build number
     * @param buildStarted        - build started time
     * @param moduleId            - module id
     */
    private void fetchModuleArtifact(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse,
            String buildNumber, String buildStarted, String moduleId, String buildName) {
        List<ModuleArtifact> moduleArtifacts = buildService.getModuleArtifact(buildName, buildNumber, moduleId,
                buildStarted);
        List<ModuleArtifactModel> moduleArtifactModels = new ArrayList<>();
        moduleArtifacts.forEach(moduleArtifact ->
                moduleArtifactModels.add(new ModuleArtifactModel(moduleArtifact,
                        ActionUtils.getDownloadLink(artifactoryRequest.getServletRequest(),
                                moduleArtifact.getRepoKey(), moduleArtifact.getPath())))
        );
        PagingModel pagingModel = new PagingModel(0, moduleArtifactModels);
        artifactoryResponse.iModel(pagingModel);
    }
}
