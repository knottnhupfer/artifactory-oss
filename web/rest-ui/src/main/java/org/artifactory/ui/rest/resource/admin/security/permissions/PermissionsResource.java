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

package org.artifactory.ui.rest.resource.admin.security.permissions;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.model.admin.security.permissions.CombinedPermissionTargetUIModel;
import org.artifactory.ui.rest.model.admin.security.permissions.DeletePermissionTargetModel;
import org.artifactory.ui.rest.model.admin.security.permissions.build.BuildPermissionPatternsUIModel;
import org.artifactory.ui.rest.model.distribution.ReleaseBundlePermissionIModel;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Path("permissiontargets")
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PermissionsResource extends BaseResource {

    @Autowired
    private SecurityServiceFactory securityFactory;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPermissionTargets() {
        return runService(securityFactory.getAllPermissionTargets());
    }

    @GET
    @Path("{name : [^/]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPermissionTarget() {
        return runService(securityFactory.getPermissionsTarget());
    }

    @GET
    @Path("{name : [^/]+}/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPermissionTargetUsers() {
        return runService(securityFactory.getPermissionsTargetUsers());
    }

    @GET
    @Path("{name : [^/]+}/groups")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPermissionTargetGroups() {
        return runService(securityFactory.getPermissionsTargetGroups());
    }

    @GET
    @Path("{name : [^/]+}/resources")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPermissionTargetResources() {
        return runService(securityFactory.getPermissionsTargetResources());
    }

    @GET
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    @Path("allUsersGroups")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsersGroups() {
        return runService(securityFactory.getAllUsersAndGroups());
    }

    @GET
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("users/{username}")
    public Response getEffectivePermissionsByUser() {
        return runService(securityFactory.getRepoEffectivePermissionServiceByEntity());
    }

    @GET
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("groups/{groupname}")
    public Response getEffectivePermissionsByGroup() {
        return runService(securityFactory.getRepoEffectivePermissionServiceByEntity());
    }

    @PUT
    @Path("{name : [^/]+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePermissionTarget(CombinedPermissionTargetUIModel permissionTarget) {
        return runService(securityFactory.updatePermissionsTarget(), permissionTarget);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response createPermissionTarget(CombinedPermissionTargetUIModel permissionTarget) {
        return runService(securityFactory.createPermissionsTarget(), permissionTarget);
    }

    @POST
    @Path("delete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePermissionTarget(DeletePermissionTargetModel deletePermissionTargetModel) {
        return runService(securityFactory.deletePermissionsTarget(), deletePermissionTargetModel);
    }

    @POST
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    @Path("buildPatterns")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildPermissionPatterns(BuildPermissionPatternsUIModel patternsUIModel) {
        return runService(securityFactory.getBuildPermissionsPatterns(), patternsUIModel);
    }

    @POST
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    @Path("releaseBundles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReleaseBundlesByReposAndPatterns(ReleaseBundlePermissionIModel releaseBundlePermissionIModel) {
        return runService(securityFactory.getReleaseBundlesByReposAndPatterns(), releaseBundlePermissionIModel);
    }

    @GET
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getBuildGlobalBasicReadAllowed")
    public Response getBuildGlobalBasicReadAllowed() {
        return runService(securityFactory.getBuildGlobalBasicReadAllowed());
    }
}
