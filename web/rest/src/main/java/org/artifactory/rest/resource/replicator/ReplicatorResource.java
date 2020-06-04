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
package org.artifactory.rest.resource.replicator;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.replicator.ReplicatorAddon;
import org.artifactory.addon.replicator.ReplicatorDetails;
import org.artifactory.api.replicator.ReplicatorDetailsResponse;
import org.artifactory.api.replicator.ReplicatorRegistrationRequest;
import org.artifactory.api.replicator.ReplicatorRegistrationResponse;
import org.artifactory.api.security.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.artifactory.api.rest.constant.ReplicatorRestConstants.*;
import static org.jfrog.common.ArgUtils.requireHttpHttpsUrl;

/**
 * @author Yoaz Menda
 */
@Component
@Path(PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
public class ReplicatorResource {

    @Autowired
    private AddonsManager addonsManager;

    @GET
    @Path(PATH_LOCAL_DETAILS)
    @Produces({MediaType.APPLICATION_JSON})
    public Response getLocalReplicatorDetails() {
        ReplicatorAddon replicatorAddon = addonsManager.addonByType(ReplicatorAddon.class);
        ReplicatorDetails replicatorDetails = replicatorAddon.getReplicatorDetails();
        if (replicatorDetails != null && replicatorDetails.getExternalBaseUrl() != null) {
            ReplicatorDetailsResponse res = ReplicatorDetailsResponse.builder()
                    .externalUrl(replicatorDetails.getExternalBaseUrl())
                    .version(replicatorDetails.getVersion())
                    .build();
            return Response.ok(res).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("No replicator is registered")
                .build();
    }

    @GET
    @Path(PATH_AUTHORIZED)
    @Produces({MediaType.APPLICATION_JSON})
    public Response isAuthorized() {
        return Response.ok().build();
    }

    @POST
    @Path(PATH_REGISTER)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    public Response register(ReplicatorRegistrationRequest registrationRequest) {
        ReplicatorAddon replicatorAddon = addonsManager.addonByType(ReplicatorAddon.class);
        requireHttpHttpsUrl(registrationRequest.getExternalBaseUrl(), "External replicator url must be a valid URL");
        requireHttpHttpsUrl(registrationRequest.getInternalBaseUrl(), "Internal replicator url must be a valid URL");
        try {
            ReplicatorRegistrationResponse replicatorRegistrationResponse = replicatorAddon
                    .register(registrationRequest);
            return Response.ok(replicatorRegistrationResponse).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

    }
}
