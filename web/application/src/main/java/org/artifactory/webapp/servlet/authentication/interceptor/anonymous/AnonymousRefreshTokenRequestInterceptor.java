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

package org.artifactory.webapp.servlet.authentication.interceptor.anonymous;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.artifactory.webapp.servlet.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Set;

/**
 * Allows anonymous to request refresh token even if anonymous access is disabled and the request contains only the
 * refresh token and the original access token. In this case the pair are used for authentication.
 *
 * @author Yinon Avraham
 */
public class AnonymousRefreshTokenRequestInterceptor implements AnonymousAuthenticationInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AnonymousRefreshTokenRequestInterceptor.class);
    private static final Set<String> EXPECTED_QUERY_PARAMS = ImmutableSet.of("grant_type", "refresh_token", "access_token");

    @Override
    public boolean accept(HttpServletRequest request) {
        try {
            return refreshTokenRequest(request) && requestContainSufficientCredentials(request);
        } catch (Exception e) {
            log.debug("Could not check for allowing anonymous refresh token request.", e);
            return false;
        }
    }

    private boolean refreshTokenRequest(HttpServletRequest request) {
        return requestEqualsTo(request, "POST", "/api/security/token") &&
                notAuthenticated(request) &&
                grantTypeIsRefreshToken(request);
    }

    private boolean grantTypeIsRefreshToken(HttpServletRequest request) {
        String grantType = request.getParameter("grant_type");
        return "refresh_token".equals(grantType);
    }

    private boolean requestEqualsTo(HttpServletRequest request, String method, String path) {
        return request.getMethod().equals(method) &&
                RequestUtils.getServletPathFromRequest(request).equals(path);
    }

    private boolean notAuthenticated(HttpServletRequest request) {
        return !RequestUtils.isBasicAuthHeaderPresent(request)
                || RequestUtils.extractUsernameFromRequest(request).equalsIgnoreCase("anonymous");
    }

    private boolean requestContainSufficientCredentials(HttpServletRequest request) {
        Set<String> paramNames = Sets.newHashSet(Collections.list(request.getParameterNames()));
        return paramNames.equals(EXPECTED_QUERY_PARAMS);
    }
}
