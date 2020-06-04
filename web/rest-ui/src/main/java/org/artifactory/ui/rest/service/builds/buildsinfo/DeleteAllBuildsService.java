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
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.builds.BuildCoordinate;
import org.artifactory.ui.rest.model.builds.DeleteBuildsModel;
import org.artifactory.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import java.util.List;

/**
 * @author Chen Keinans
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
public class DeleteAllBuildsService<T extends DeleteBuildsModel> implements RestService<T> {

    private BuildService buildService;

    @Autowired
    public DeleteAllBuildsService(BuildService buildService) {
        this.buildService = buildService;
    }

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        T model = request.getImodel();
        List<String> buildsToDelete = Lists.newArrayList();
        for (BuildCoordinate coordinate : model.getBuildsCoordinates()) {
            buildsToDelete.add(coordinate.getBuildName());
        }
        if (CollectionUtils.isNullOrEmpty(buildsToDelete)) {
            response.error("Invalid request, non build names were specified");
            return;
        }
        deleteAllBuildsAndUpdateResponse(buildsToDelete);
        if (model.getBuildsCoordinates().size() > 1) {
            response.info("Successfully removed " + model.getBuildsCoordinates().size() + " build projects");
        } else if (model.getBuildsCoordinates().size() == 1) {
            response.info(
                    "Successfully removed " + model.getBuildsCoordinates().get(0).getBuildName() + " build project");
        }
    }

    /**
     * delete all builds and update response
     */
    private void deleteAllBuildsAndUpdateResponse(List<String> buildsNames) {
        BasicStatusHolder status = new BasicStatusHolder();
        // delete all builds by name, in case of error, a mapped exception is thrown
        buildService.deleteAllBuildsByName(buildsNames, false, status);
    }

}
