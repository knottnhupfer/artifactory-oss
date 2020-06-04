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

package org.artifactory.ui.rest.resource.admin.advanced.support;

import org.artifactory.addon.support.ArtifactorySupportBundleConfig;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.service.admin.advanced.AdvancedServiceFactory;
import org.artifactory.ui.rest.service.admin.advanced.support.BundleConfigurationWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Michael Pasternak
 */
@Path("userSupport")
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UserSupportResource extends BaseResource {

    @Autowired
    private AdvancedServiceFactory advancedServiceFactory;

    @Autowired
    private AuthorizationService authorizationService;

    @Context
    private HttpServletRequest httpServletRequest;

    @Autowired
    @Qualifier("streamingRestResponse")
    public void setArtifactoryResponse(RestResponse artifactoryResponse) {
        this.artifactoryResponse = artifactoryResponse;
    }

    @Path("generateBundle")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateBundle(ArtifactorySupportBundleConfig bundleInfo) throws Exception {
        if (authorizationService.isAnonymous() || !authorizationService.isAdmin()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return runService(advancedServiceFactory.getSupportServiceGenerateBundle(),
                new BundleConfigurationWrapper(bundleInfo, httpServletRequest)
        );
    }

    @Path("downloadBundle/{archive: .+}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadBundle(@PathParam("archive") String archive, @QueryParam("node") String handlingNode) throws Exception {
        if (authorizationService.isAnonymous() || !authorizationService.isAdmin()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return runService(advancedServiceFactory.getSupportServiceDownloadBundle(), archive);
    }

    @Path("listBundles")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listBundles() throws Exception {
        if (authorizationService.isAnonymous() || !authorizationService.isAdmin()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return runService(advancedServiceFactory.getSupportServiceListBundles());
    }

    @Path("deleteBundle/{archive: .+}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBundle(@PathParam("archive") String archive) throws Exception {
        if (authorizationService.isAnonymous() || !authorizationService.isAdmin()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return runService(advancedServiceFactory.getSupportServiceDeleteBundle(), archive);
    }
}