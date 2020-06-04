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

import org.apache.http.HttpStatus;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.build.model.BuildGeneralInfo;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.builds.GeneralBuildInfo;
import org.artifactory.ui.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetBuildGeneralInfoService extends AbstractBuildService {

    private BuildService buildService;

    @Autowired
    public GetBuildGeneralInfoService(BuildService buildService) {
        this.buildService = buildService;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String buildName = request.getPathParamByKey("name");
        String buildNumber = request.getPathParamByKey("number");
        String date = request.getPathParamByKey("date");
        String buildStarted = null;
        if (isNotBlank(date)) {
            buildStarted = DateUtils.formatBuildDate(Long.parseLong(date));
        }
        getBuildAndUpdateResponse(response, buildName, buildNumber, buildStarted);
    }

    /**
     * populate build info from build model and update response
     *
     * @param response - encapsulate data require for response
     * @param buildName           - build name
     * @param buildNumber         - build number
     * @param buildStarted        - build start date
     */
    private void getBuildAndUpdateResponse(RestResponse response, String buildName, String buildNumber, String buildStarted) {
        BuildGeneralInfo generalInfo = buildService.getBuildGeneralInfo(buildName, buildNumber, buildStarted);
        if (generalInfo == null) {
            response.error(format("Failed to locate build %s:%s-%s", buildName, buildNumber, isNotBlank(buildStarted) ? buildStarted : ""))
                    .responseCode(HttpStatus.SC_NOT_FOUND);
        } else {
            response.iModel(new GeneralBuildInfo(generalInfo));
        }
    }
}
