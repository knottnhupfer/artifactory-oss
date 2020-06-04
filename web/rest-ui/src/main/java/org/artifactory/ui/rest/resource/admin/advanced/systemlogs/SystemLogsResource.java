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

package org.artifactory.ui.rest.resource.admin.advanced.systemlogs;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.service.admin.advanced.AdvancedServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Lior Hasson
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("systemlogs")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SystemLogsResource extends BaseResource {
    @Autowired
    protected AdvancedServiceFactory advanceFactory;

    @Autowired
    @Qualifier("streamingRestResponse")
    public void setArtifactoryResponse(RestResponse artifactoryResponse) {
        this.artifactoryResponse = artifactoryResponse;
    }

    @GET
    @Path("initialize")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogsInitialize() throws Exception{
        return runService(advanceFactory.getSystemLogsInitialize());
    }

    @GET
    @Path("logData")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogData(){
        return runService(advanceFactory.getSystemLogData());
    }

    @GET
    @Path("downloadFile")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response logDownloadLink() throws Exception{
        return runService(advanceFactory.getSystemLogDownloadLink());
    }
}
