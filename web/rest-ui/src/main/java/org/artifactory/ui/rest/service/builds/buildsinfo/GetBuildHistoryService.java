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
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.rest.build.ContinueBuildFilter;
import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.ui.rest.model.continuous.dtos.ContinueBuildDto;
import org.artifactory.ui.rest.model.continuous.translators.ContinueBuildTranslator;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.builds.GeneralBuildInfo;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetBuildHistoryService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetBuildHistoryService.class);

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String buildName = request.getPathParamByKey("name");
        ContinueBuildDto continueBuildDto = new ContinueBuildDto(request);
        ContinueBuildFilter buildFilter = ContinueBuildTranslator.toBuildFilter(continueBuildDto);

        // fetch build info data
        fetchAllBuildsData(response, buildName, buildFilter);
    }

    /**
     * fetch all build data by type
     *
     * @param response - encapsulate data related to response
     */
    private void fetchAllBuildsData(RestResponse response, String buildName, ContinueBuildFilter buildFilter) {
        List<GeneralBuild> buildsForName = Optional.ofNullable(getAllBuildsByName(response, buildName, buildFilter))
                .orElse(new ArrayList<>());
        String nextContinueState = null;
        List<GeneralBuildInfo> generalBuildInfoList = buildsForName.stream()
                .map(this::createGeneralInfo)
                .collect(Collectors.toList());

        if (CollectionUtils.notNullOrEmpty(generalBuildInfoList) && generalBuildInfoList.size() == buildFilter.getLimit()) {
            GeneralBuildInfo last = generalBuildInfoList.get(generalBuildInfoList.size() - 1);
            nextContinueState = ContinueBuildTranslator.buildIdToBase64(last);
        }

        response.iModel(new ContinueResult<>(nextContinueState, generalBuildInfoList));
    }

    private GeneralBuildInfo createGeneralInfo(GeneralBuild build) {
        return GeneralBuildInfo.builder()
                .buildNumber(build.getBuildNumber())
                .lastBuildTime(centralConfigService.getDateFormatter().print(build.getBuildDate()))
                .releaseStatus(build.getStatus())
                .ciUrl(build.getCiUrl())
                .time(build.getBuildDate())
                .time(build.getBuildDate())
                .buildStat("Modules-" + build.getNumOfModules() + ", Artifacts-"
                        + build.getNumOfArtifacts() + ", Dependencies-" + build.getNumOfDependencies())
                .build();
    }

    private List<GeneralBuild> getAllBuildsByName(RestResponse response, String buildName, ContinueBuildFilter buildFilter) {
        List<GeneralBuild> buildsForName = null;
        try {
            buildsForName = buildService.getBuildForName(buildName, buildFilter);
        } catch (SQLException e) {
            String err = "Failed to retrieve build history for build " + buildName;
            response.error(err + ". check the logs for error information");
            log.error(err, e);
        }
        return buildsForName;
    }
}
