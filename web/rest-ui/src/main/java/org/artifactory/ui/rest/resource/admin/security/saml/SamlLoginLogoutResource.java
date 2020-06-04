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

package org.artifactory.ui.rest.resource.admin.security.saml;

import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Gidi Shabat
 */
@Component
@Path("saml")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SamlLoginLogoutResource extends BaseResource {

    @Autowired
    private SecurityServiceFactory securityFactory;

    @Path("loginRequest")
    @GET
    public Response loginRequest() {
        return runService(securityFactory.handleLoginRequest());
    }


    @Path("loginResponse")
    @POST
    public Response loginResponse() {
        return runService(securityFactory.handleLoginResponse());
    }

    @Path("logoutRequest")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response logoutRequest() {
        return runService(securityFactory.handleLogoutRequest());
    }
}
