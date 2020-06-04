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

package org.artifactory.util;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ArtifactoryContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.artifactory.util.distribution.DistributionConstants.EDGE_UPLOADS_REPO_KEY;


/**
 * @author Chen Keinan
 */
public abstract class UiRequestUtils {

    private static final Set<String> NON_UI_PATH_PREFIXES = new HashSet<>();
    private static final Set<String> UI_PATH_PREFIXES = new HashSet<>();

    private UiRequestUtils() {
        // utility class
    }

    public static void setNonUiPathPrefixes(Collection<String> uriPathPrefixes) {
        NON_UI_PATH_PREFIXES.clear();
        NON_UI_PATH_PREFIXES.addAll(uriPathPrefixes);
    }

    public static void setUiPathPrefixes(Collection<String> uriPathPrefixes) {
        UI_PATH_PREFIXES.clear();
        UI_PATH_PREFIXES.addAll(uriPathPrefixes);
    }

    public static boolean isUiRestRequest(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String ui_uri = contextPath.endsWith("/") ? "ui/" : "/ui/";
        String requestAgent = request.getHeader("Request-Agent");
        return request.getRequestURI().startsWith(contextPath + ui_uri) ||
                (requestAgent != null && requestAgent.equals("artifactoryUI"));
    }

    public static boolean isReservedName(String pathPrefix) {
        return Stream.concat(UI_PATH_PREFIXES.stream(), NON_UI_PATH_PREFIXES.stream())
                .anyMatch(pathPrefix::equalsIgnoreCase)
                || "list".equalsIgnoreCase(pathPrefix)
                || EDGE_UPLOADS_REPO_KEY.equalsIgnoreCase(pathPrefix);
    }

    /**
     * Returns the un-decoded servlet path from the request
     *
     * @param req The received request
     * @return String - Servlet path
     */
    public static String getServletPathFromRequest(HttpServletRequest req) {
        String contextPath = req.getContextPath();
        if (StringUtils.isBlank(contextPath)) {
            return req.getRequestURI();
        }
        return req.getRequestURI().substring(contextPath.length());
    }

    /**
     * @param servletContext The servlet context
     * @return The artifactory spring context
     */
    public static ArtifactoryContext getArtifactoryContext(ServletContext servletContext) {
        return (ArtifactoryContext) servletContext.getAttribute(ArtifactoryContext.APPLICATION_CONTEXT_KEY);
    }
}
