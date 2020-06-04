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
import org.apache.http.HttpHeaders;
import org.artifactory.common.ConstantValues;
import org.artifactory.request.RequestThreadLocal;
import org.artifactory.request.RequestWrapper;
import org.artifactory.response.ResponseThreadLocal;
import org.artifactory.response.ResponseWrapper;
import org.artifactory.traffic.RequestLogger;
import org.artifactory.util.HttpUtils;
import org.jfrog.common.logging.logback.filters.contextaware.request.RequestAwareLogbackContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Optional;

/**
 * A dedicated filter for the Request Logger that sits after the ArtifactoryFilter
 *
 * @author Noam Tenne
 */
public class RequestFilter extends DelayedFilterBase {
    private static final Logger log = LoggerFactory.getLogger(RequestFilter.class);


    @Override
    public void initLater(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain)
            throws IOException, ServletException {
        if (shouldSkipFilter(req)) {
            chain.doFilter(req, resp);
            return;
        }
        long start = System.currentTimeMillis();
        //Wrap the response
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        CapturingHttpServletResponseWrapper responseWrapper = new CapturingHttpServletResponseWrapper(response);
        RequestAwareLogbackContext context = null;
        try {
            RequestThreadLocal.bind(new RequestWrapper(request));
            ResponseThreadLocal.bind(new ResponseWrapper(response));
            if (ConstantValues.enableContextAwareLoggerEnabled.getBoolean()) {
                context = bindAndFillRequestAwareLogbackContext(request);
            }
            chain.doFilter(req, responseWrapper);
            String servletPath = RequestUtils.getServletPathFromRequest(request);
            String method = request.getMethod();
            long contentLength = getContentLength(request, responseWrapper, method);
            Object userAtt = req.getAttribute(AccessFilter.AUTHENTICATED_USERNAME_ATTRIBUTE);
            String username = userAtt != null ? userAtt.toString() : "non_authenticated_user";
            String remoteAddress = HttpUtils.getRemoteClientAddress(request);
            RequestLogger.request(remoteAddress, username, method, servletPath, request.getProtocol(),
                    responseWrapper.getStatus(), contentLength, System.currentTimeMillis() - start);
        } finally {
            RequestThreadLocal.unbind();
            ResponseThreadLocal.unbind();
            if (context != null) {
                context.destroy();
            }
        }
    }

    long getContentLength(HttpServletRequest request, CapturingHttpServletResponseWrapper responseWrapper,
            String method) {
        if ("get".equalsIgnoreCase(method)) {
            return responseWrapper.getContentLength();
        }
        if (("put".equalsIgnoreCase(method)) || ("post".equalsIgnoreCase(method)) ||
                ("patch".equalsIgnoreCase(method))) {
            return HttpUtils.getContentLength(request);
        }
        return 0;
    }

    private RequestAwareLogbackContext bindAndFillRequestAwareLogbackContext(HttpServletRequest request) {
        RequestAwareLogbackContext context = RequestAwareLogbackContext.fromRequest(request);
        context.setKeyValueFetcher("request.remote_ip", () -> HttpUtils.getRemoteClientAddress(request));
        context.setKeyValueFetcher("user",
                () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                        .map(Authentication::getPrincipal).map(Object::toString).orElse(null));
        return context;
    }

    @Override
    public void destroy() {
    }

    /**
     * A custom response wrapper the helps capture the return code and the content length
     */
    static class CapturingHttpServletResponseWrapper extends HttpServletResponseWrapper {
        private int status;
        private long contentLength;

        /**
         * Constructs a response adaptor wrapping the given response.
         *
         * @throws IllegalArgumentException if the response is null
         */
        CapturingHttpServletResponseWrapper(HttpServletResponse response) {
            super(response);
            status = 200;
        }

        public int getStatus() {
            return status;
        }

        public long getContentLength() {
            return contentLength;
        }

        @Override
        public void addHeader(String name, String value) {
            super.addHeader(name, value);
            captureString(name, value);
        }

        @Override
        public void addIntHeader(String name, int value) {
            super.addIntHeader(name, value);
            captureInt(name, value);
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(name, value);
            captureString(name, value);
        }

        @Override
        public void setIntHeader(String name, int value) {
            super.setIntHeader(name, value);
            captureInt(name, value);
        }

        @Override
        public void setContentLength(int len) {
            contentLength = len;
            super.setContentLength(len);
        }

        @Override
        public void setStatus(int sc) {
            if (notCommitted(sc)) {
                status = sc;
                super.setStatus(sc);
            }
        }

        @Override
        public void setStatus(int sc, String sm) {
            if (notCommitted(sc, sm)) {
                status = sc;
                super.setStatus(sc, sm);
            }
        }

        @Override
        public void sendError(int sc) throws IOException {
            if (notCommitted(sc)) {
                status = sc;
                HttpUtils.sendErrorResponse((HttpServletResponse) getResponse(), sc, null);
            }
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            if (notCommitted(sc, msg)) {
                status = sc;
                if (status == SC_NOT_FOUND) {
                    msg = "Not Found";
                }
                HttpUtils.sendErrorResponse((HttpServletResponse) getResponse(), sc, msg);
            }
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            super.sendRedirect(location);
            status = SC_FOUND;
        }

        private boolean notCommitted(int status) {
            return notCommitted(status, null);
        }

        private boolean notCommitted(int status, String reason) {
            if (isCommitted()) {
                log.debug("Cannot change status " + status + (reason != null ? " (" + reason + ")" : "") + ": " +
                        "response already committed.");
                return false;
            }
            return true;
        }

        private void captureString(String name, String value) {
            if (name.equals(HttpHeaders.CONTENT_LENGTH) && StringUtils.isNumeric(value)) {
                contentLength = Long.parseLong(value);
            }
        }

        private void captureInt(String name, int value) {
            captureString(name, value + "");
        }
    }
}