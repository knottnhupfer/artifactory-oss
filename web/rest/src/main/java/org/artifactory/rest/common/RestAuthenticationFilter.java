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

package org.artifactory.rest.common;


import org.artifactory.api.rest.constant.HaRestConstants;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.exception.AuthorizationRestException;
import org.artifactory.rest.util.AuthUtils;
import org.artifactory.security.HaSystemAuthenticationToken;
import org.artifactory.security.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.security.Principal;

/**
 * Authorization filter for all the REST requests.
 *
 * @author Fred Simon
 * @author Yossi Shaul
 * @author Yoav Landman
 */
@Component
@Scope()
@Provider
@Priority(Priorities.AUTHENTICATION)
public class RestAuthenticationFilter implements ContainerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RestAuthenticationFilter.class);

    @Context
    HttpServletResponse response;
    @Context
    HttpServletRequest request;
    @Context
    UriInfo uriInfo;

    @Autowired
    AuthorizationService authorizationService;

    @Override
    public void filter(ContainerRequestContext request) {
        // validate session still active
        AuthUtils.validateSession(this.request, uriInfo, response);
        boolean authenticated = authorizationService.isAuthenticated();
        boolean anonAccessEnabled = authorizationService.isAnonAccessEnabled();
        if (!authenticated) {
            // TODO: (chenk) need to do this validation in more elegant way
            if (anonAccessEnabled || uriInfo.getPath().indexOf("auth") != -1 ||
                    uriInfo.getPath().matches(".*npm/.*/-/user/.*") || uriInfo.getPath().endsWith("v2/token") ||
                    // TODO [NS] https://gph.is/1LFQGbL Remove after moving openid to access
                    uriInfo.getPath().contains(SystemRestConstants.PATH_OPENID)) {
                //If anon access is allowed and we didn't bother authenticating try to perform the action as a user
                request.setSecurityContext(new RoleAuthenticator(UserInfo.ANONYMOUS, AuthorizationService.ROLE_USER));
            } else {
                throw new AuthorizationRestException();
            }
        } else {
            //Set the authenticated user and role
            String username = authorizationService.currentUsername();
            boolean admin = authorizationService.isAdmin();

            boolean ha = SecurityContextHolder.getContext().getAuthentication() instanceof HaSystemAuthenticationToken;
            if (ha) {
                request.setSecurityContext(new RoleAuthenticator(username, HaRestConstants.ROLE_HA));
                return;
            }

            if (admin) {
                request.setSecurityContext(new RoleAuthenticator(username, AuthorizationService.ROLE_ADMIN));
            } else {
                request.setSecurityContext(new RoleAuthenticator(username, AuthorizationService.ROLE_USER));
            }
        }
    }

    private class RoleAuthenticator implements SecurityContext {
        private final Principal principal;
        private final String role;

        RoleAuthenticator(String name, String role) {
            this.role = role;
            this.principal = () -> name;
        }

        @Override
        public Principal getUserPrincipal() {
            return principal;
        }

        @Override
        public boolean isUserInRole(String role) {
            return role.equals(this.role);
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public String getAuthenticationScheme() {
            return SecurityContext.BASIC_AUTH;
        }
    }
}
