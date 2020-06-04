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

package org.artifactory.security.filters;

import org.springframework.security.core.Authentication;

import javax.servlet.Filter;
import javax.servlet.ServletRequest;

/**
 * @author freds
 * @date Mar 10, 2009
 */
public interface ArtifactoryAuthenticationFilter extends Filter {
    /**
     * Authentications might get expired for various reasons, depending on the authenticator. This method is called
     * whenever the {@link org.artifactory.webapp.servlet.AccessFilter} detects a valid authentication.
     * The implementer should return true if it is
     * the one "responsible" for this request or was the one authenticated this session and it determines that the
     * authentication is not valid anymore (for example an expired cookie).
     *
     * @param request        The http request
     * @param authentication A valid authenticated authentication
     * @return True if the client should be re-authenticated
     */
    boolean requiresReAuthentication(ServletRequest request, Authentication authentication);

    /**
     * Return true if the current Artifactory entry point should be authenticated and managed by this filter.
     *
     * @param request The original HTTP request
     * @return True if the filter manage this request for Authentication
     */
    boolean acceptFilter(ServletRequest request);

    /**
     * The value of usually the header used to authenticate for this filter.
     *
     * @param request
     * @return A unique string for this authentication in this request
     */
    String getCacheKey(ServletRequest request);

    /**
     * @param request        The http request
     * @return Login identifier such as user, sessionId, apiKey, etc.
     */
    String getLoginIdentifier(ServletRequest request);
}
