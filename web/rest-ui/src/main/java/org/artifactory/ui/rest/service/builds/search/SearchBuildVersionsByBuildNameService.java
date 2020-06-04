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

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.build.GeneralBuild;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.rest.build.BuildNumberInfo;
import org.artifactory.api.rest.build.BuildNumbersResponse;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.build.BuildInfoUtils;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rotem Kfir
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SearchBuildVersionsByBuildNameService implements RestService {

    @Autowired
    private BuildService buildService;

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private AuthorizationService authService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BuildsSearchFilter buildVersionSearchFilter = createBuildSearchFilter(request);
        List<GeneralBuild> buildVersions = buildService.getBuildVersions(buildVersionSearchFilter);
        BuildNumbersResponse buildNumbersResponse = getBuildNumbersResponse(buildVersions);
        response.iModel(buildNumbersResponse);
    }

    private BuildNumbersResponse getBuildNumbersResponse(List<GeneralBuild> buildVersions) {
        List<BuildNumberInfo> buildVersionInfos = buildVersions.stream()
                .map(this::getBuildGeneralInfo)
                .collect(Collectors.toList());

        BuildNumbersResponse buildNumbersResponse = new BuildNumbersResponse();
        buildVersionInfos.forEach(buildNumbersResponse::add);
        return buildNumbersResponse;
    }

    private BuildsSearchFilter createBuildSearchFilter(ArtifactoryRestRequest request) {
        String buildName = request.getPathParamByKey("buildName");
        String limitStr = request.getQueryParamByKey("num_of_rows");
        String orderBy = request.getQueryParamByKey("order_by");
        String direction = request.getQueryParamByKey("direction");
        int limit = StringUtils.isNotBlank(limitStr) ? Integer.parseInt(limitStr) : ConstantValues.searchUserQueryLimit.getInt();
        limit = Math.min(limit,ConstantValues.searchUserQueryLimit.getInt());

        return BuildsSearchFilter.builder()
                .name(buildName)
                .limit(limit)
                .daoLimit(ConstantValues.searchUserSqlQueryLimit.getLong())
                .orderBy(orderBy)
                .direction(direction)
                .build();
    }


    private BuildNumberInfo getBuildGeneralInfo(GeneralBuild build) {
        return BuildNumberInfo.builder()
                .buildNumber(build.getBuildNumber())
                .ciUrl(build.getCiUrl())
                .buildStat(build.getStatus())
                .buildTime(ISODateTimeFormat.dateTime().print(build.getBuildDate()))
                .time(build.getBuildDate())
                .canDelete(ConstantValues.buildUiSkipDeletePermissionCheck.getBoolean() ||
                        authService.canDeleteBuild(build.getBuildName(), build.getBuildNumber(), BuildInfoUtils.formatBuildTime(build.getBuildDate())))
                .build();
    }
}
