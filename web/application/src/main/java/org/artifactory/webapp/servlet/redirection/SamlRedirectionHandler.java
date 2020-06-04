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
import org.artifactory.webapp.servlet.RequestUtils;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Gidi Shabat
 */
public class SamlRedirectionHandler implements RedirectionHandler {
    private static final Logger log = LoggerFactory.getLogger(SamlRedirectionHandler.class);
    @Override
    public boolean shouldRedirect(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();
        path = PathUtils.trimLeadingSlashes(path);
        path = path.toLowerCase();
        return path.endsWith("/webapp/saml/loginrequest") ||
                path.endsWith("/webapp/saml/loginresponse") ||
                path.endsWith("/webapp/saml/logoutrequest");
    }

    @Override
    public void redirect(ServletRequest request, ServletResponse response) {
        try {
            String path = RequestUtils.getServletPathFromRequest((HttpServletRequest) request);
            String targetUrl = StringUtils.replace(path, "webapp", "ui");
            RequestDispatcher dispatcher = request.getRequestDispatcher(targetUrl);
            dispatcher.forward(request, response);
        } catch (Exception e) {
            log.error("Failed to redirect SAML request.",e);
        }
    }
}
