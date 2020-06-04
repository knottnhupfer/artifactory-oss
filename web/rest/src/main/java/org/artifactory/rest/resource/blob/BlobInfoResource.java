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
package org.artifactory.rest.resource.blob;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.blob.BlobInfoAddon;
import org.artifactory.api.rest.blob.ClosestBlobInfoRequest;
import org.artifactory.api.rest.constant.HaRestConstants;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Rotem Kfir
 */
@Path(SystemRestConstants.PATH_BLOB)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, HaRestConstants.ROLE_HA})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BlobInfoResource {

    @Autowired
    private AddonsManager addonsManager;

    @Path("closest")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @POST
    public Response getClosestBlobInfo(ClosestBlobInfoRequest request, @HeaderParam(HttpHeaders.AUTHORIZATION) String auth) {
        BlobInfoAddon addon = addonsManager.addonByType(BlobInfoAddon.class);
        String closestBlobInfoJson = addon.getClosestBlobInfo(request, auth);
        if (closestBlobInfoJson == null) {
            // This target Artifactory already has the requested artifact, no need to get the blob info
            return Response.noContent().build();
        }
        return Response.ok().entity(closestBlobInfoJson).build();
    }
}
