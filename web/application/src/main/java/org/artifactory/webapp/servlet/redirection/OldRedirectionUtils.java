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

package org.artifactory.webapp.servlet.redirection;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.util.HttpUtils;
import org.artifactory.webapp.servlet.RequestUtils;
import org.jfrog.client.util.PathUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Yinon Avraham
 */
final class OldRedirectionUtils {

    private OldRedirectionUtils() {}

    static boolean requestPathEndsWith(ServletRequest request, String expectedEnd) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();
        path = path.toLowerCase();
        return path.endsWith(expectedEnd.toLowerCase());
    }

    static boolean servletPathStartsWith(ServletRequest request, String expectedStart) {
        String servletPath = RequestUtils.getServletPathFromRequest((HttpServletRequest) request);
        servletPath = servletPath.trim();
        servletPath = servletPath.toLowerCase();
        servletPath = "/" + PathUtils.trimLeadingSlashes(servletPath);
        return servletPath.startsWith(expectedStart.toLowerCase());
    }

    static void redirect(ServletRequest request, ServletResponse response, String oldPathPart, String newPathPart) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String servletPath = RequestUtils.getServletPathFromRequest(httpRequest);
        String queryString = httpRequest.getQueryString() != null ? "?" + httpRequest.getQueryString() : "";
        String targetPath = StringUtils.replace(servletPath, oldPathPart, newPathPart) + queryString;
        ArtifactoryContext artifactoryContext = RequestUtils.getArtifactoryContext(request.getServletContext());
        String baseUrl = HttpUtils.getServletContextUrl(artifactoryContext, httpRequest);
        String targetUrl = baseUrl + targetPath;
        httpResponse.sendRedirect(targetUrl);
    }
}
