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

package org.artifactory.ui.rest.resource.admin.security.oauth;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.model.admin.security.oauth.OAuthProviderUIModel;
import org.artifactory.ui.rest.model.admin.security.oauth.OAuthUIModel;
import org.artifactory.ui.rest.model.admin.security.oauth.OAuthUserToken;
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
 * @author Gidi Shabat
 */
@Component
@Path("oauth")
@RolesAllowed({AuthorizationService.ROLE_ADMIN,AuthorizationService.ROLE_USER})
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OAuthUIResource extends BaseResource {
    @Autowired
    protected SecurityServiceFactory securityFactory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response updateOAuthSettings(OAuthUIModel oAuthUIModel)
            throws Exception {
        return runService(securityFactory.updateOAuthSettings(), oAuthUIModel);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response getOAuthSettings()
            throws Exception {
        return runService(securityFactory.getOAuthtSettings());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_USER,AuthorizationService.ROLE_ADMIN})
    @Path("user/tokens")
    public Response getOAuthTokensForUser()
            throws Exception {
        return runService(securityFactory.getOAuthTokensForUser());
    }

    @PUT
    @Path("provider")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response addAuthProviderSettings(OAuthProviderUIModel model)
            throws Exception {
        return runService(securityFactory.addOAuthProviderSettings(), model);
    }

    @POST
    @Path("provider")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response updateAuthProviderSettings(OAuthProviderUIModel model)
            throws Exception {
        return runService(securityFactory.updateOAuthProviderSettings(), model);
    }


    @DELETE
    @Path("provider/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response deleteAuthProviderSettings(@PathParam("name") String name)
            throws Exception {
        return runService(securityFactory.deleteOAuthProviderSettings(), name);
    }

    @DELETE
    @Path("user/tokens/{userName}/{providerName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_USER,AuthorizationService.ROLE_ADMIN})
    public Response deleteOAuthUserToken(@PathParam("userName") String userName,@PathParam("providerName") String providerName)
            throws Exception {
        return runService(securityFactory.deleteOAuthUserToken(),new OAuthUserToken(userName,providerName));
    }

}
