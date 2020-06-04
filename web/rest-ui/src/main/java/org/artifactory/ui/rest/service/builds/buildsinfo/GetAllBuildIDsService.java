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

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.rest.build.ContinueBuildFilter;
import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.build.BuildId;
import org.artifactory.common.ConstantValues;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAllBuildIDsService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(GetAllBuildIDsService.class);

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private BuildService buildService;

    @Autowired
    private AuthorizationService authService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ContinueBuildDto continueBuildDto = new ContinueBuildDto(request);
        ContinueBuildFilter continueBuildFilter = ContinueBuildTranslator.toBuildFilter(continueBuildDto);
        // fetch build info data
        fetchAllBuildsData(continueBuildFilter, response);
    }
    /**
     * fect all build data by type
     *
     * @param artifactoryResponse - encapsulate data related to response
     */
    private void fetchAllBuildsData(ContinueBuildFilter continueBuildFilter, RestResponse artifactoryResponse) {
        printFilter(continueBuildFilter);

        List<BuildId> buildIds = buildService.getLatestBuildIDsPaging(continueBuildFilter);
        String nextContinueState = null;

        List<GeneralBuildInfo> generalBuildInfoList = buildIds
                .stream()
                .map(this::getBuildGeneralInfo)
                .collect(Collectors.toList());

        if (CollectionUtils.notNullOrEmpty(generalBuildInfoList) && generalBuildInfoList.size() == continueBuildFilter.getLimit()) {
            GeneralBuildInfo last = generalBuildInfoList.get(generalBuildInfoList.size() - 1);
            nextContinueState = ContinueBuildTranslator.buildIdToBase64(last);
        }

        artifactoryResponse.iModel(new ContinueResult<>(nextContinueState, generalBuildInfoList));
    }

    private GeneralBuildInfo getBuildGeneralInfo(BuildId build) {
                return GeneralBuildInfo.builder()
                        .buildName(build.getName())
                        .lastBuildTime(centralConfigService.getDateFormatter().print(build.getStartedDate().getTime()))
                        .buildNumber(build.getNumber())
                        .time(build.getStartedDate() != null ? build.getStartedDate().getTime() : 0)
                        .canDelete(ConstantValues.buildUiSkipDeletePermissionCheck.getBoolean() ||
                                authService.canDeleteBuild(build.getName(), build.getNumber(), build.getStarted()))
                        .build();
    }

    private void printFilter(ContinueBuildFilter continueBuildFilter) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching builds order by {} {}", continueBuildFilter.getOrderByStr(),
                    continueBuildFilter.getDirection().name());

            if (StringUtils.isNotBlank(continueBuildFilter.getSearchStr())) {
                log.debug("Fetching builds only matches {}", continueBuildFilter.getSearchStr());
            }

            if (continueBuildFilter.getLimit() != null) {
                log.debug("Fetching {} builds", continueBuildFilter.getLimit());
            }

            if (continueBuildFilter.getContinueBuildId() != null) {
                log.debug("Fetching builds after {}", continueBuildFilter.getContinueBuildId().getName());
            }
        }
    }
}
