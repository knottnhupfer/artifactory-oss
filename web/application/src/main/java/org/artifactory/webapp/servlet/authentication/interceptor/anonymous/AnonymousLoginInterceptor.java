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

import javax.servlet.http.HttpServletRequest;

/**
 * Allows anonymous to login if anonymous access is disabled (RTFACT-7073)
 *
 * @author Dan Feldman
 */
public class AnonymousLoginInterceptor implements AnonymousAuthenticationInterceptor {

    @Override
    public boolean accept(HttpServletRequest request) {
        // TODO[ShayBagants]: 7/14/16 This is quick and dirty fix for RTFACT-10617. Need to find a better way + move the npm code to an npm interceptor.
        //We can probably use the 'Request-Agent': 'artifactoryUI' header + "ui/auth" for ui auth requests but it was too risky to do it now.
        String requestURI = request.getRequestURI();

        return requestURI.contains("ui/auth") || requestURI.contains("api/oauth2") ||
                requestURI.contains("ui/oauth2");
    }
}