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
package org.artifactory.webapp.servlet;

import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ConstantValues;
import org.artifactory.security.AccessLogger;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.SessionUtils;
import org.jfrog.client.util.PathUtils;
import org.springframework.security.core.Authentication;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Tamir Hadad
 */
public class ArtifactoryCsrfFilter extends DelayedFilterBase {

    private final String HEADER_NAME = "X-Requested-With";

    private static final Set<String> METHODS_TO_IGNORE;
    static {
        Set<String> mti = new HashSet<>();
        mti.add("GET");
        mti.add("OPTIONS");
        mti.add("HEAD");
        METHODS_TO_IGNORE = Collections.unmodifiableSet(mti);
    }

    private static final Set<String> PATHS_TO_IGNORE;
    static {
        Set<String> pti = new HashSet<>();
        pti.add("ui/builds/exportLicenses");
        pti.add("ui/saml/loginResponse");
        PATHS_TO_IGNORE = Collections.unmodifiableSet(pti);
    }

    @Override
    public void initLater(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (shouldSkipFilter(request)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (METHODS_TO_IGNORE.contains(httpRequest.getMethod()) || shouldIgnorePath(PathUtils.trimTrailingSlashes(httpRequest.getRequestURI()))) {
            chain.doFilter(request, response);
            return;
        }
        Authentication authentication = SessionUtils.getAuthentication(httpRequest);
        if (authentication != null) {
            if (httpRequest.getHeader(HEADER_NAME) == null || !StringUtils.equals(httpRequest.getHeader(HEADER_NAME), getCsrfHeaderValue())) {
                AccessLogger.deniedAuthentication(true, authentication, "Cross-Site Request Forgery");
                HttpUtils.sendErrorResponse((HttpServletResponse) response, HttpServletResponse.SC_FORBIDDEN, "Request was blocked. Please refer to access.log");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private String getCsrfHeaderValue() {
        String headerValue = ConstantValues.csrfProtectionHeaderValue.getString();
        if(StringUtils.isNotEmpty(headerValue)) {
            return headerValue;
        }
        return "artUI";
    }

    @Override
    public void destroy() {

    }

    private boolean shouldIgnorePath(String requestUri) {
        return requestUri != null && PATHS_TO_IGNORE.stream().anyMatch(requestUri::endsWith);
    }
}