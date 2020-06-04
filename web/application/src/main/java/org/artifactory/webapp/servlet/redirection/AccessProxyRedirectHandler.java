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
import org.artifactory.common.ConstantValues;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.artifactory.api.rest.constant.SystemRestConstants.PATH_ACCESS_PROXY;

/**
 * @author Tomer Mayost
 */
@Component
public class AccessProxyRedirectHandler implements RedirectionHandler {
    private static final Logger log = LoggerFactory.getLogger(AccessProxyRedirectHandler.class);

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean shouldRedirect(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = PathUtils.trimLeadingSlashes(httpRequest.getRequestURI()).toLowerCase();

        return path.startsWith(getAccessProxyPrefix());
    }

    @Override
    public void redirect(ServletRequest req, ServletResponse resp) {
        try {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) resp;
            String path = request.getRequestURI();
            String targetUrl = StringUtils.replace(path, getAccessProxyPrefix(), "");
            ServletContext accessContext = request.getServletContext().getContext(ConstantValues.accessContextPath.getString());
            RequestDispatcher dispatcher = accessContext.getRequestDispatcher(targetUrl);
            dispatcher.forward(request, response);
        } catch (Exception e) {
            log.error("Failed to redirect Access Proxy request.", e);
        }
    }

    private String getAccessProxyPrefix() {
        return PathUtils.trimLeadingSlashes(ConstantValues.contextPath.getString() + PATH_ACCESS_PROXY);
    }
}
