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
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.home.HomeWidgetArtifactModel;
import org.artifactory.ui.rest.model.home.HomeWidgetModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MostDownloadedWidgetService implements RestService {

    @Autowired
    private MostDownloadedWidgetHelper widgetHelper;

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        boolean forceRefresh = Optional.ofNullable(request.getQueryParamByKey("force")).map(Boolean::valueOf)
                .orElse(false);
        //As a heuristic for the case where a user might not have read permissions on the top 5, fetch 10 and then take 5...
        List<PathDownloadsStats> mostDownloaded = widgetHelper.getMostDownloaded(forceRefresh);
        List<HomeWidgetArtifactModel> mostDownloadedModels = mostDownloaded.stream()
                .filter(pathDownloadsStats -> authorizationService.canRead(pathDownloadsStats.getRepoPath()))
                .map(pathDownloadsStats -> createHomeWidgetArtifactModel(request, pathDownloadsStats))
                .limit(5)
                .collect(Collectors.toList());

        HomeWidgetModel model = new HomeWidgetModel("Most Downloaded Artifacts");
        model.addData("mostDownloaded", mostDownloadedModels);
        response.iModel(model);
    }

    private HomeWidgetArtifactModel createHomeWidgetArtifactModel(ArtifactoryRestRequest request,
            PathDownloadsStats pathDownloadsStats) {
        RepoPath repoPath = pathDownloadsStats.getRepoPath();
        long downloads = pathDownloadsStats.getDownloads();
        return new HomeWidgetArtifactModel(repoPath.toPath(), request.getDownloadLink(repoPath), downloads);
    }
}
