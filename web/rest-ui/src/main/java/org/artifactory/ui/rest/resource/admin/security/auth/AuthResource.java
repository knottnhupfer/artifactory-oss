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

package org.artifactory.ui.rest.resource.admin.security.auth;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.dataholder.PasswordContainer;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.model.admin.security.login.UserLogin;
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
 * @author chen keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Path("auth")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AuthResource extends BaseResource {

    @Autowired
    protected SecurityServiceFactory securityFactory;

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(UserLogin userLoginModel) throws Exception {
        return runService(securityFactory.loginService(), userLoginModel);
    }

    @POST
    @Path("logout")
    public Response logout() throws Exception {
        return runService(securityFactory.logoutService());
    }

    @GET
    @Path("issaml")
    public Response isSamlAuthentication() throws Exception {
        return runService(securityFactory.isSamlAuthentication());
    }

    /**
     * Tests are in OpenIdGatewayResourceTest
     */
    @GET
    @Path("redirect")
    @Produces(MediaType.APPLICATION_JSON)
    public Response redirect(@QueryParam("redirectTo") String redirectTo) throws Exception {
        return runService(securityFactory.redirectService(), redirectTo);
    }
    @POST
    @Path("forgotpassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forgotPassword(UserLogin userLoginModel) throws Exception {
        return runService(securityFactory.forgotPassword(), userLoginModel);
    }

    @POST
    @Path("validatetoken")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateToken() throws Exception {
        return runService(securityFactory.validateToken());
    }

    @POST
    @Path("resetpassword")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPassword(UserLogin userLoginModel) throws Exception {
        return runService(securityFactory.resetPassword(), userLoginModel);
    }

    @POST
    @Path("changePassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changePassword(PasswordContainer passwordContainer) throws Exception {
        return runService(securityFactory.changePassword(), passwordContainer);
    }

    @POST
    @Path("loginRelatedData")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchLoginRelatedData(UserLogin userLogin) throws Exception {
        // Redirect to might be initialized in current user login data
        return runService(securityFactory.loginRelatedData(), userLogin);
    }

    @GET
    @Path("current")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentUser() throws Exception {
        return runService(securityFactory.getCurrentUser());
    }

    @GET
    @Path("canAnnotate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response canAnnotate() throws Exception {
        return runService(securityFactory.getCanAnnotateService());
    }
}

