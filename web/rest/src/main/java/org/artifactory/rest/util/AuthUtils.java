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

package org.artifactory.rest.util;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.util.SessionUtils;
import org.artifactory.util.UiRequestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

/**
 * @author Chen Keinan
 */
public class AuthUtils {

    /**
     * validate that session is still active , if not try to restore if from remember me cookie if exist
     */
    public static void validateSession(HttpServletRequest request,UriInfo uriInfo,HttpServletResponse response) {
        if (UiRequestUtils.isUiRestRequest(request)) {
            // get existing session
            HttpSession session = request.getSession(false);
            if (session == null) {
                // try to restore auth if remember me cookie exist
                restoreRememberMeAuth(request,response);
            }
        }
    }

    /**
     * try to restore remember me authentication and create valid session of it
     */
    private static void restoreRememberMeAuth(HttpServletRequest request,HttpServletResponse response) {
        RememberMeServices rememberMeServices = ContextHelper.get().beanForType(RememberMeServices.class);
        Authentication authentication = rememberMeServices.autoLogin(request, response);
        if (authentication != null) {
            boolean sessionCreated = SessionUtils.setAuthentication(request, authentication, true);
            if (sessionCreated) {
                bindAuthentication(authentication);
            }
        }
    }

    /**
     * bind authentication to security context
     *
     * @param authentication - authentication
     */
    private static void bindAuthentication(Authentication authentication) {
        org.springframework.security.core.context.SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
    }

    /**
     * add session valid header to response in case  request sent to UI
     *
     * @param response
     */
    public static  void addSessionStatusToHeaders(ContainerResponseContext response,UriInfo uriInfo,HttpServletRequest request) {
        if (UiRequestUtils.isUiRestRequest(request) && !uriInfo.getPath().equals("auth/login")) {
            MultivaluedMap<String, Object> metadata = response.getHeaders();
            HttpSession session = request.getSession(false);
            if (session != null) {
                metadata.add("SessionValid", "true");
            } else {
                metadata.add("SessionValid", "false");
            }
        }
    }
}
