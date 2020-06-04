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

package org.artifactory.ui.rest.service.home.widget;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiBuild;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlBuild;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.schedule.CachedThreadPoolTaskExecutor;
import org.artifactory.ui.rest.model.builds.GeneralBuildInfo;
import org.artifactory.ui.rest.model.home.HomeWidgetModel;
import org.jfrog.common.CachedValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.artifactory.build.BuildInfoUtils.formatBuildTime;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LatestBuildsWidgetService implements RestService {

    private AqlService aqlService;
    private AuthorizationService authorizationService;
    private CachedThreadPoolTaskExecutor cachedThreadPoolTaskExecutor;
    private CachedValue<List<GeneralBuildInfo>> latestBuildsCache;

    @Autowired
    public LatestBuildsWidgetService(AqlService aqlService, AuthorizationService authorizationService,
            CachedThreadPoolTaskExecutor cachedThreadPoolTaskExecutor) {
        this.aqlService = aqlService;
        this.authorizationService = authorizationService;
        this.cachedThreadPoolTaskExecutor = cachedThreadPoolTaskExecutor;
        initCache();
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        HomeWidgetModel widgetModel = new HomeWidgetModel("Latest Builds");
        widgetModel.addData("mostRecentBuilds", latestBuildsCache.get());
        response.iModel(widgetModel);
    }

    /**
     * Return the most recent builds that the current user has permission to.
     * In case no builds apply return an empty list.
     */
    private List<GeneralBuildInfo> getMostRecentBuildsPerPermissions() {
        AqlEagerResult<AqlBuild> results = aqlService.executeQueryEager(
                AqlApiBuild.create()
                        .addSortElement(AqlApiBuild.created())
                        .desc()
                        .limit(5));
        return results.getResults().stream()
                .filter(build -> authorizationService.isBuildBasicRead(build.getBuildName(), build.getBuildNumber(),
                        formatBuildTime(build.getBuildCreated().getTime())))
                .map(toBuildModel)
                .collect(Collectors.toList());
    }

    private final Function<AqlBuild, GeneralBuildInfo> toBuildModel = build ->
            GeneralBuildInfo.builder()
                    .buildName(build.getBuildName())
                    .buildNumber(build.getBuildNumber())
                    .build();

    private void initCache() {
        latestBuildsCache = CachedValue
                .loadUsing(this::getMostRecentBuildsPerPermissions)
                .async(cachedThreadPoolTaskExecutor::submit)
                .defaultValue(Collections.emptyList())
                .initialLoadTimeout(15, TimeUnit.SECONDS)
                .expireAfterRefresh(ConstantValues.mostDownloadedCacheIdleTimeSecs.getLong(), TimeUnit.SECONDS)
                .name("MostRecentBuilds")
                .build();
    }
}
