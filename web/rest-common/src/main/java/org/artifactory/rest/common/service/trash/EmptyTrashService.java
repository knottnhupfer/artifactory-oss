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

package org.artifactory.rest.common.service.trash;

import org.apache.http.HttpStatus;
import org.artifactory.common.StatusHolder;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EmptyTrashService implements RestService {

    @Autowired
    TrashService trashService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        StatusHolder statusHolder = trashService.empty();
        if (request.isUiRestCall()) {
            uiResponse(statusHolder, response);
        } else {
            apiResponse(statusHolder, response);
        }
    }

    private void uiResponse(StatusHolder statusHolder, RestResponse response) {
        if (statusHolder.isError()) {
            response.error(statusHolder.getLastError().getMessage());
        } else {
            response.info("Successfully deleted all trashcan items");
        }
    }

    private void apiResponse(StatusHolder statusHolder, RestResponse response) {
        if (statusHolder.isError()) {
            throw new BadRequestException(statusHolder.getLastError().getMessage());
        } else {
            response.iModel("Successfully deleted all trashcan items");
            response.responseCode(HttpStatus.SC_ACCEPTED);
        }
    }
}
