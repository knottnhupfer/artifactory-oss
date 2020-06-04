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
import org.artifactory.api.build.PublishedModule;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.PagingModel;
import org.artifactory.ui.rest.model.builds.BuildModule;
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
public class GetPublishedModulesService implements RestService {

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String buildNumber = request.getPathParamByKey("number");
        String buildStarted = request.getPathParamByKey("date");
        String buildName = request.getPathParamByKey("name");
        // fetch modules
        fetchModuleList(response, buildNumber, buildStarted, buildName);
    }

    /**
     * get
     *  @param artifactoryResponse - encapsulate data require for response
     * @param buildNumber         - build number
     * @param buildStarted        - build started
     */
    private void fetchModuleList(RestResponse artifactoryResponse, String buildNumber, String buildStarted,
            String buildName) {
        List<BuildModule> buildModuleList = new ArrayList<>();
        List<PublishedModule> buildModules = buildService.getPublishedModules(buildNumber, buildStarted, buildName);
        if (buildModules != null) {
            buildModules.forEach(module -> buildModuleList.add(new BuildModule(module)));
            PagingModel pagingModel = new PagingModel(0, buildModuleList);
            artifactoryResponse.iModel(pagingModel);
        }
    }
}
