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

package org.artifactory.rest.resource.security;

import org.apache.commons.codec.CharEncoding;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.rest.constant.SecurityRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.security.permissions.PermissionTargetModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static org.artifactory.api.rest.constant.SecurityRestConstants.*;

/**
 * Provides REST API endpoints for Permission targets V2 (Repositories, Builds and Release Bundles permissions)
 *
 * @author Yuval Reches
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(SecurityRestConstants.PATH_ROOT_V2)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class SecurityResourceV2 {

    private RestAddon restAddon;

    @Context
    private HttpServletRequest request;

    @Autowired
    public SecurityResourceV2(AddonsManager addonsManager) {
        restAddon = addonsManager.addonByType(RestAddon.class);
    }

    @GET
    @Path(PERMISSIONS_ROOT)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    public Response getAllPermissionTargets() {
        return restAddon.getAllPermissionTargetsV2(request);
    }

    @GET
    @Path(PATH_PERMISSIONS)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    public Response getPermissionTarget(@PathParam(ENTITY_KEY) String entityKey) throws UnsupportedEncodingException {
        return restAddon.getPermissionTargetV2(decodeEntityKey(entityKey));
    }

    @DELETE
    @Path(PATH_PERMISSIONS)
    @RolesAllowed(AuthorizationService.ROLE_ADMIN)
    public Response deletePermissionTarget(@PathParam(ENTITY_KEY) String entityKey)
            throws UnsupportedEncodingException {
        return restAddon.deletePermissionTargetV2(decodeEntityKey(entityKey));
    }

    @HEAD
    @Path(PATH_PERMISSIONS)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    public Response isPermissionTargetExists(@PathParam(ENTITY_KEY) String entityKey)
            throws UnsupportedEncodingException {
        return restAddon.isPermissionTargetExistsV2(decodeEntityKey(entityKey));
    }

    @POST
    @Path(PATH_PERMISSIONS)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(AuthorizationService.ROLE_ADMIN)
    public Response createPermissionTarget(@PathParam(ENTITY_KEY) String entityKey,
            PermissionTargetModel permissionTargetModel) throws UnsupportedEncodingException {
        return restAddon.createPermissionTargetV2(decodeEntityKey(entityKey), permissionTargetModel);
    }

    @PUT
    @Path(PATH_PERMISSIONS)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    public Response updatePermissionTarget(@PathParam(ENTITY_KEY) String entityKey,
            PermissionTargetModel permissionTargetModel) throws UnsupportedEncodingException {
        return restAddon.updatePermissionTargetV2(decodeEntityKey(entityKey), permissionTargetModel);
    }

    @GET
    @Path(PATH_USER_PERMISSION)
    @Produces({MediaType.APPLICATION_JSON})
    @RolesAllowed(AuthorizationService.ROLE_ADMIN)
    public Response getUserPermissionsSecurityEntity(@PathParam(ENTITY_KEY) String entityKey)
            throws UnsupportedEncodingException {
        return restAddon.getUserPermissionsSecurityV2(decodeEntityKey(entityKey));
    }

    @GET
    @Path(PATH_GROUP_PERMISSION)
    @Produces({MediaType.APPLICATION_JSON})
    @RolesAllowed(AuthorizationService.ROLE_ADMIN)
    public Response getGroupPermissionsSecurityEntity(@PathParam(ENTITY_KEY) String entityKey)
            throws UnsupportedEncodingException {
        return restAddon.getGroupPermissionsSecurityV2(decodeEntityKey(entityKey));
    }

    private String decodeEntityKey(String entityKey) throws UnsupportedEncodingException {
        return URLDecoder.decode(entityKey, CharEncoding.UTF_8);
    }

}
