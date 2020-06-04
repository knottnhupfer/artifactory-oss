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

package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.builddiff;

import org.artifactory.api.build.BuildService;
import org.artifactory.api.build.ModuleDependency;
import org.artifactory.api.build.model.diff.BuildParams;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.PagingModel;
import org.artifactory.ui.rest.model.builds.ModuleDependencyModel;
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
public class DiffBuildModuleDependencyService implements RestService {

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String name = request.getPathParamByKey("name");
        String moduleId = request.getPathParamByKey("id");
        String buildNumber = request.getPathParamByKey("number");
        String comparedBuildNum = request.getQueryParamByKey("otherNumber");
        String comparedDate = request.getQueryParamByKey("otherDate");
        String buildStarted = request.getPathParamByKey("date");
        BuildParams buildParams = new BuildParams(moduleId, buildNumber, comparedBuildNum, comparedDate, buildStarted, name);
        List<ModuleDependency> moduleArtifacts = buildService.getModuleDependencyForDiffWithPaging(buildParams);
        List<ModuleDependencyModel> moduleArtifactModels = new ArrayList<>();
        if (moduleArtifacts != null && !moduleArtifacts.isEmpty()) {
            moduleArtifacts.forEach(moduleArtifact -> {
                ModuleDependencyModel depModel = new ModuleDependencyModel(moduleArtifact, ActionUtils.getDownloadLink(request.getServletRequest(),
                        moduleArtifact.getRepoKey(), moduleArtifact.getPath()));
                moduleArtifactModels.add(depModel);
            });
            PagingModel pagingModel = new PagingModel(0, moduleArtifactModels);
            response.iModel(pagingModel);
        }
    }
}
