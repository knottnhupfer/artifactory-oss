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

package org.artifactory.rest.resource;

import org.artifactory.rest.http.request.ArtifactoryRestRequest;
import org.artifactory.rest.http.response.IResponse;
import org.artifactory.rest.services.IService;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@Component("serviceExecutor")
public class ServiceExecutor {

    public Response process(ArtifactoryRestRequest restReq, IResponse restRes, IService serviceAction) {
        // execute service method
        serviceAction.execute(restReq, restRes);
        // build response
        Response response = restRes.buildResponse();
        return response;
    }
}
