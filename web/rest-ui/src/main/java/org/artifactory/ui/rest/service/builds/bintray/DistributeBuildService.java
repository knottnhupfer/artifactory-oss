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

package org.artifactory.ui.rest.service.builds.bintray;

import org.artifactory.api.bintray.distribution.Distribution;
import org.artifactory.api.bintray.distribution.Distributor;
import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.util.build.distribution.BuildDistributionHelper;
import org.artifactory.ui.rest.model.builds.BuildCoordinate;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.AbstractBuildService;
import org.artifactory.ui.utils.DateUtils;
import org.jfrog.build.api.Build;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.artifactory.rest.common.model.distribution.DistributionResponseBuilder.doResponse;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DistributeBuildService extends AbstractBuildService {

    @Autowired
    private Distributor distributor;

    @Autowired
    private BuildDistributionHelper buildDistributionHelper;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BuildCoordinate buildCoordinate = (BuildCoordinate) request.getImodel();
        Build requestedBuild = getBuild(response, buildCoordinate);
        if (requestedBuild == null || response.isFailed()) {
            return;
        }
        distributeBuild(response, request, requestedBuild);
    }

    private Build getBuild(RestResponse response, BuildCoordinate buildCoordinate) {
        String buildName = buildCoordinate.getBuildName();
        String buildNumber = buildCoordinate.getBuildNumber();
        String buildStarted = DateUtils.formatBuildDate(buildCoordinate.getDate());
        return getBuild(buildName, buildNumber, buildStarted, response);
    }

    private void distributeBuild(RestResponse response, ArtifactoryRestRequest request, Build requestedBuild) {
        boolean isDryRun = Boolean.parseBoolean(request.getQueryParamByKey("dryRun"));
        boolean async = Boolean.parseBoolean(request.getQueryParamByKey("async"));
        String targetRepo = request.getQueryParamByKey("targetRepo");
        boolean overrideExistingFiles = Boolean.parseBoolean(request.getQueryParamByKey("overrideExistingFiles"));

        Distribution distribution = new Distribution();
        distribution.setTargetRepo(targetRepo);
        distribution.setAsync(async);
        distribution.setDryRun(isDryRun);
        distribution.setOverrideExistingFiles(overrideExistingFiles);
        DistributionReporter status = new DistributionReporter(!isDryRun);
        buildDistributionHelper.populateBuildPaths(requestedBuild, distribution, status);
        if (!status.isError()) {
            status = distributor.distribute(distribution);
        }
        String performedOn = "Build '" + requestedBuild.getName() + ":" + requestedBuild.getNumber();
        doResponse(response, performedOn, distribution, status);
    }
}
