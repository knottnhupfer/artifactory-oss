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

package org.artifactory.rest.resource.token;

import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.security.access.AccessService;
import org.jfrog.access.client.token.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.artifactory.api.security.AuthorizationService.ROLE_ADMIN;

/**
 * @author Tomer Mayost
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(AccessAdminTokenResource.PATH_ROOT)
@RolesAllowed({ROLE_ADMIN})
public class AccessAdminTokenResource {
    private static final Logger log = LoggerFactory.getLogger(AccessAdminTokenResource.class);

    public static final String PATH_ROOT = "security/access/admin/token";

    private final AccessService accessService;

    @Autowired
    public AccessAdminTokenResource(AccessService accessService) {
        this.accessService = accessService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccessAdminToken(AccessAdminTokenModel model) {
        try {
            TokenResponse tokenWithAdminCredentials = accessService
                    .createTokenWithAccessAdminCredentials(model.getServiceId());
            return Response.ok(tokenWithAdminCredentials).build();
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("Failed to create token With Admin Credentials ", e);
            throw new BadRequestException("Failed to create token with Admin Credentials");
        }
    }
}
