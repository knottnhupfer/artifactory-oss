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

package org.artifactory.ui.rest.service.builds.search;

import org.artifactory.api.build.BuildService;
import org.artifactory.api.rest.build.BuildIdInfo;
import org.artifactory.api.rest.build.BuildIdResponse;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.build.BuildId;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rotem Kfir
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SearchBuildIDsByNameService implements RestService {

    @Autowired
    private BuildService buildService;

    @Autowired
    private AuthorizationService authService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String buildName = request.getQueryParamByKey("name");
        String from = request.getQueryParamByKey("after");
        String to = request.getQueryParamByKey("before");
        String numOfRows = request.getQueryParamByKey("num_of_rows");
        long limit = Long.MAX_VALUE;
        try {
            limit = Long.parseLong(numOfRows);
        } catch (NumberFormatException e) {
            // num_of_rows parameter is optional
        }
        String orderBy = request.getQueryParamByKey("order_by");
        String direction = request.getQueryParamByKey("direction");

        List<BuildId> builds = buildService.getBuildIDsByName(buildName, from, to, limit, orderBy, direction);
        BuildIdResponse buildIdResponse = getBuildIdResponse(builds);
        response.iModel(buildIdResponse);
    }

    private BuildIdResponse getBuildIdResponse(List<BuildId> builds) {
        List<BuildIdInfo> generalBuildInfoList = builds.stream()
                .map(this::getBuildIdInfo)
                .collect(Collectors.toList());
        BuildIdResponse buildIdResponse = new BuildIdResponse();
        generalBuildInfoList.forEach(buildIdResponse::add);
        return buildIdResponse;
    }

    private BuildIdInfo getBuildIdInfo(BuildId build) {
        Date startDate = build.getStartedDate();
        long startTime = startDate != null ? startDate.getTime() : 0;
        return BuildIdInfo.builder()
                .buildName(build.getName())
                .lastBuildTime(ISODateTimeFormat.dateTime().print(startTime))
                .time(startTime)
                .lastBuildNumber(build.getNumber())
                .canDelete(ConstantValues.buildUiSkipDeletePermissionCheck.getBoolean() ||
                        authService.canDeleteBuild(build.getName(), build.getNumber(), build.getStarted()))
                .build();
    }
}
