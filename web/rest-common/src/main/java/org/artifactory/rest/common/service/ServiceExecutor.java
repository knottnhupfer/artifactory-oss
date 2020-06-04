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

package org.artifactory.rest.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
/**
 * @author Chen Keinan
 */
@Component("serviceUiExecutor")
public class ServiceExecutor {
    private static final Logger log = LoggerFactory.getLogger(ServiceExecutor.class);

    public Response process(ArtifactoryRestRequest restReq, RestResponse restRes, RestService serviceAction) {
        log.trace("calling rest service :" + serviceAction.getClass().getSimpleName());
        // execute service method
        serviceAction.execute(restReq,restRes);
        // build response
        Response response = restRes.buildResponse();
        return response;
    }
}
