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

package org.artifactory.ui.rest.resource.admin.security.accesstokens;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Yinon Avraham
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("admin/security/accesstokens")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AccessTokensUIResource extends BaseResource {

    @Autowired
    protected SecurityServiceFactory securityFactory;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("tokens")
    public Response getAccessTokens() {
        return runService(securityFactory.getAccessTokens());
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("revokeTokens")
    public Response revokeAccessTokens(List<String> tokenIds) {
        return runService(securityFactory.revokeAccessToken(), tokenIds);
    }
}
