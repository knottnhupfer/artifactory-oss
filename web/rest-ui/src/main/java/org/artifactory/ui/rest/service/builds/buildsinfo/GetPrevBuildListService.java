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

package org.artifactory.ui.rest.service.builds.buildsinfo;

import org.artifactory.api.build.BuildService;
import org.artifactory.api.build.GeneralBuild;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.builds.GeneralBuildInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetPrevBuildListService implements RestService {

    private BuildService buildService;

    @Autowired
    public GetPrevBuildListService(BuildService buildService) {
        this.buildService = buildService;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String buildName = request.getPathParamByKey("name");
        String date = request.getPathParamByKey("date");
        // fetch build info data
        List<GeneralBuildInfo> generalBuildInfoList = fetchAllBuildsData(buildName, date);
        response.iModelList(generalBuildInfoList);
    }

    private List<GeneralBuildInfo>  fetchAllBuildsData(String buildName, String buildDate) {
         return buildService.getPrevBuildsList(buildName, buildDate)
                .stream()
                .map(this::getBuildInfoFromBuildRun)
                .collect(Collectors.toList());
    }

    private GeneralBuildInfo getBuildInfoFromBuildRun(GeneralBuild buildRun) {
        return GeneralBuildInfo.builder()
                .buildNumber(buildRun.getBuildNumber())
                .buildName(buildRun.getBuildName())
                .buildStat(buildRun.getStatus())
                .time(buildRun.getBuildDate()).build();
    }
}
