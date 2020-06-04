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

import com.google.common.collect.Lists;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.build.BuildRun;
import org.artifactory.common.StatusEntry;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.exception.ForbiddenException;
import org.artifactory.ui.rest.model.builds.BuildCoordinate;
import org.artifactory.ui.rest.model.builds.DeleteBuildsModel;
import org.artifactory.ui.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * @author Chen Keinans
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
public class DeleteBuildService<T extends DeleteBuildsModel> implements RestService<T> {
    private static final Logger log = LoggerFactory.getLogger(DeleteBuildService.class);

    private BuildService buildService;

    @Autowired
    public DeleteBuildService(BuildService buildService) {
        this.buildService = buildService;
    }

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        T model = request.getImodel();
        List<BuildCoordinate> buildsCoordinates = model.getBuildsCoordinates();
        // Delete all coordinates
        BasicStatusHolder status = new BasicStatusHolder();
        for (BuildCoordinate coordinate : buildsCoordinates) {
            // Delete specific build and update response feedback
            deleteSpecificBuildsAndUpdateResponse(coordinate, status);
        }
        if (status.hasErrors()) {
            List<String> errors = status.getErrors().stream()
                    .map(StatusEntry::toString)
                    .collect(Collectors.toList());
            response.errors(errors);
        } else {
            constructResponse(response, model);
        }
    }

    /**
     * delete Build by "build coordinate"
     */
    private void deleteSpecificBuildsAndUpdateResponse(BuildCoordinate coordinate, BasicStatusHolder status) {
        String buildName = coordinate.getBuildName();
        String buildNumber = coordinate.getBuildNumber();
        long buildDate = coordinate.getDate();
        try {
            String buildStarted = DateUtils.formatBuildDate(buildDate);
            if (isBlank(buildNumber) && isBlank(buildStarted)) {
                buildService.deleteAllBuildsByName(Lists.newArrayList(buildName), false, status);
            } else {
                buildService.assertDeletePermissions(buildName, buildNumber, buildStarted);
                log.trace("trying to delete build {}/{}-{}", buildName, buildNumber, buildStarted);
                BuildRun buildRun = buildService.getBuildRun(buildName, buildNumber, buildStarted);
                buildService.deleteBuild(buildRun, false, status);
            }
        } catch (ForbiddenException e){
            throw e;
        }
        catch (Exception ex) {
            String error = String.format("Exception occurred while deleting build '%s' #%s", buildName, buildNumber);
            status.error(error, ex, log);
        }
    }

    private void constructResponse(RestResponse response, T model) {
        if (model.getBuildsCoordinates().size() > 1) {
            response.info("Successfully removed " + model.getBuildsCoordinates().size() + " builds");
        } else if (model.getBuildsCoordinates().size() == 1) {
            BuildCoordinate coordinate = model.getBuildsCoordinates().get(0);
            response.info("Successfully removed " + coordinate.getBuildName() + " #" + coordinate.getBuildNumber() + " build");
        }
    }
}
