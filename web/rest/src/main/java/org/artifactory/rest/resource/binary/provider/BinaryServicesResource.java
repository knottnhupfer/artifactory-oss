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
package org.artifactory.rest.resource.binary.provider;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.binary.provider.BinaryProviderApiAddon;
import org.artifactory.api.rest.binary.services.GetPartsByChecksumRequest;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Gal Ben Ami
 */
@Path(SystemRestConstants.PATH_BINARY_SERVICES)
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component
public class BinaryServicesResource {

    @Autowired
    private AddonsManager addonsManager;


    @Path("file/{sha256}")
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    @GET
    public Response getBinaryBySha256(@PathParam("sha256") String sha256) {
        BinaryProviderApiAddon addon = addonsManager.addonByType(BinaryProviderApiAddon.class);
        InputStream stream = addon.getBinaryBySha256(sha256);
        return Response.ok(stream).build();
    }

    @Path("part/{sha256}/{start}/{end}")
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    @GET
    public Response getBinaryPartBySha256(@PathParam("sha256") String sha256, @PathParam("start") long start,
            @PathParam("end") long end) {
        BinaryProviderApiAddon addon = addonsManager.addonByType(BinaryProviderApiAddon.class);
        InputStream stream = addon.getBinaryPartBySha256(sha256, start, end);
        return Response.ok(stream).build();
    }

    @Path("parts/{sha256}")
    @POST
    @Produces("application/octet-stream")
    @Consumes(APPLICATION_JSON)
    public Response getBinaryPartsBySha256Post(@PathParam("sha256") String sha256,
            GetPartsByChecksumRequest partsRequest) {
        BinaryProviderApiAddon addon = addonsManager.addonByType(BinaryProviderApiAddon.class);
        InputStream is = addon.getBinaryBySha256(sha256);
        return Response
                .ok()
                .entity((StreamingOutput) outputStream -> addon
                        .getBinaryPartsBySha256(sha256, partsRequest.getParts(), is, outputStream))
                .build();
    }


}
