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

package org.artifactory.ui.rest.service.builds.buildsinfo.tabs;

import org.artifactory.api.build.BuildService;
import org.artifactory.build.BuildRun;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.ViewArtifact;
import org.artifactory.ui.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetBuildJsonService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetBuildJsonService.class);

    private BuildService buildService;

    @Autowired
    public GetBuildJsonService(BuildService buildService) {
        this.buildService = buildService;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String buildName = request.getPathParamByKey("name");
        String buildNumber = request.getPathParamByKey("number");
        String buildStarted = DateUtils.formatBuildDate(Long.parseLong(request.getPathParamByKey("date")));
        BuildRun buildRun = buildService.getBuildRun(buildName, buildNumber, buildStarted);
        if (buildRun == null) {
            log.error("Requested build {} that does not exist.", buildName + ":" + buildNumber);
            return;
        }
        String buildJson = buildService.getBuildAsJson(buildRun);
        ViewArtifact buildJsonModel = new ViewArtifact();
        buildJsonModel.setFileContent(buildJson);
        response.iModel(buildJsonModel);
    }
}
