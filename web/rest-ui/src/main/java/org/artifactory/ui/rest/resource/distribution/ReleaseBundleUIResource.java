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

package org.artifactory.ui.rest.resource.distribution;

import org.artifactory.api.rest.distribution.bundle.models.ReleaseBundleSearchModel;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.service.distribution.ReleaseBundleServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Tomer Mayost
 */
@Path("bundles")
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReleaseBundleUIResource extends BaseResource {

    @Autowired
    ReleaseBundleServiceFactory releaseBundleService;

    @GET
    @Path("{type}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_USER,AuthorizationService.ROLE_ADMIN})
    public Response getBundles( @QueryParam("name") String name,
                                @QueryParam("before") long before,
                                @QueryParam("after") long after,
                                @QueryParam("num_of_rows") String limit,
                                @QueryParam("order_by") String orderBy,
                                @QueryParam("direction") String direction) {
        return runService(releaseBundleService.getAllBundles());
    }

    @GET
    @Path("{type}/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_USER,AuthorizationService.ROLE_ADMIN})
    public Response getBundleVersions(  @QueryParam("before") long before,
                                        @QueryParam("after") long after,
                                        @QueryParam("num_of_rows") String limit,
                                        @QueryParam("order_by") String orderBy,
                                        @QueryParam("direction") String direction) {
        return runService(releaseBundleService.getAllBundleVersions());
    }

    @GET
    @Path("{type}/{name}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_USER,AuthorizationService.ROLE_ADMIN})
    public Response getBundle() {
        return runService(releaseBundleService.getReleaseBundle());
    }

    @GET
    @Path("repositories")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    public Response getReleaseBundleRepos() {
        return runService(releaseBundleService.getReleaseBundleRepos());
    }

    @DELETE
    @Path("{type}/{name}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response deleteBundle(@QueryParam("include_content") Boolean includeContent) {
        return runService(releaseBundleService.deleteReleaseBundle());
    }

    @POST
    @Path("{type}/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_USER,AuthorizationService.ROLE_ADMIN})
    public Response getBundleVersionsByIds(ReleaseBundleSearchModel releaseBundleSearchModel) {
        return runService(releaseBundleService.getReleaseBundleVersionsByVersionsList(),releaseBundleSearchModel);
    }

    // for release-bundles page
    @GET
    @Path("effectivePermission/{repoKey}/{name}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    public Response getBuildEffectivePermission() {
        return runService(releaseBundleService.getReleaseBundleEffectivePermissions());
    }
}
