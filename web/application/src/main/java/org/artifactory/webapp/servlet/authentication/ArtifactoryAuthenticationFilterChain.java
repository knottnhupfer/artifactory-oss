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

package org.artifactory.webapp.servlet.authentication;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.collections.iterators.EnumerationIterator;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.SecurityService;
import org.artifactory.security.exceptions.LoginDisabledException;
import org.artifactory.security.filters.ArtifactoryAuthenticationFilter;
import org.artifactory.util.HttpUtils;
import org.artifactory.webapp.servlet.HttpArtifactoryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author freds
 * @date Mar 10, 2009
 */
public class ArtifactoryAuthenticationFilterChain {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryAuthenticationFilterChain.class);

    private SecurityService securityService;

    private BasicAuthenticationEntryPoint authenticationEntryPoint;

    private final List<ArtifactoryAuthenticationFilter> authenticationFilters = new ArrayList<>();

    public ArtifactoryAuthenticationFilterChain(BasicAuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    public void addFilters(Collection<ArtifactoryAuthenticationFilter> filters) {
        ArtifactoryAuthenticationFilter beforeLast = null;
        ArtifactoryAuthenticationFilter last = null;
        for (ArtifactoryAuthenticationFilter filter : filters) {
            if (filter.getClass().getName().endsWith("SignedUrlAuthenticationFilter")) {
                this.authenticationFilters.add(0, filter);
            } else if (filter instanceof ArtifactoryBasicAuthenticationFilter) {
                //TODO: [by YS] Not sure the comment below is true. All basic authentications are done by the same filter
                //HACK! ArtifactoryBasicAuthenticationFilter should always be last so it doesn't handle basic auth intended
                //for other sso filters
                last = filter;
            } else if (filter.getClass().getName().endsWith("CasAuthenticationFilter")) {
                // Other Hack! The CAS should be after other SSO filter
                beforeLast = filter;
            } else {
                this.authenticationFilters.add(filter);
            }
        }
        if (beforeLast != null) {
            this.authenticationFilters.add(beforeLast);
        }
        if (last != null) {
            this.authenticationFilters.add(last);
        }
    }

    /**
     * Find the correct Artifactory Authentication filter for the request
     *
     * @param request The HTTP request object
     * @return The correct auth filter
     */
    public ArtifactoryAuthenticationFilter acceptFilter(ServletRequest request) {
        List<ArtifactoryAuthenticationFilter> matchedFilters = Lists.newArrayList();
        for (ArtifactoryAuthenticationFilter filter : authenticationFilters) {
            if (filter.acceptFilter(request)) {
                matchedFilters.add(filter);
            }
        }
        if (matchedFilters.size() > 1) {
            if (log.isDebugEnabled()) {
                String method = request instanceof HttpServletRequest ? ((HttpServletRequest) request).getMethod() : "?METHOD?";
                String uri = request instanceof HttpServletRequest ? ((HttpServletRequest) request).getRequestURI() : "?URI?";
                String filterNames = Joiner.on("\n").join(matchedFilters.stream().map(f -> f.getClass().getName()).iterator());
                String headerNames = request instanceof HttpServletRequest ? Joiner.on(", ").join(new EnumerationIterator(((HttpServletRequest) request).getHeaderNames())) : "?HEADERS?";
                log.debug("Ignored filters for request {} {}:\n{}\n\nHeaders: {}", method, uri, filterNames, headerNames);
            }
        }
        return (matchedFilters.isEmpty()) ? null : matchedFilters.get(0);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        for (ArtifactoryAuthenticationFilter filter : authenticationFilters) {
            filter.init(filterConfig);
        }
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
            ArtifactoryAuthenticationFilter authFilter,
            final FilterChain servletChain)
            throws IOException, ServletException {
        FilterChain chainWithAdditive = (request, response) -> {
            try {
                AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
                addonsManager.addonByType(PluginsAddon.class)
                        .executeAdditiveRealmPlugins(new HttpArtifactoryRequest((HttpServletRequest) request));
                servletChain.doFilter(request, response);
            } catch (AuthenticationException e) {
                ContextHelper.get().beanForType(BasicAuthenticationEntryPoint.class).commence(
                        (HttpServletRequest) request, (HttpServletResponse) response, e);
            }
        };

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        long accessTime = HttpUtils.getSessionAccessTime(httpServletRequest);
        String remoteClientAddress = HttpUtils.getRemoteClientAddress((HttpServletRequest) servletRequest);

        String loginIdentifier = null;
        try {
            // The loginIdentifier can be the actual access token or the Api Key itself. Should not be printed to the logs.
            loginIdentifier = getLoginIdentifier(servletRequest, authFilter);
            if (loginIdentifier == null) {
                log.debug("Login identifier was not resolved");
                authFilter.doFilter(servletRequest, servletResponse, chainWithAdditive);
            } else {
                if (Strings.isNullOrEmpty(loginIdentifier)) {
                    // makes sure that session is not locked
                    getSecurityService().ensureSessionIsNotLocked(loginIdentifier);

                    // delay session if applicable
                    getSecurityService().ensureSessionShouldNotBeDelayed(loginIdentifier);
                } else {
                    // makes sure that user is not locked
                    getSecurityService().ensureUserIsNotLocked(loginIdentifier);

                    // delay login if applicable
                    getSecurityService().ensureLoginShouldNotBeDelayed(loginIdentifier, accessTime);
                }

                // memorise user last access time
                getSecurityService().updateUserLastAccess(loginIdentifier, remoteClientAddress, accessTime);

                authFilter.doFilter(servletRequest, servletResponse, chainWithAdditive);

                HttpServletResponse response = (HttpServletResponse) servletResponse;

                if (response.getStatus() == 401) {
                    log.debug("Filter responded with code {}, registering authentication failure!", response.getStatus());
                    // register incorrect login attempt
                    getSecurityService().interceptLoginFailure(loginIdentifier, accessTime);
                } else if (response.getStatus() < 400 && response.getStatus() >= 200) {
                    log.debug("Filter responded with code {}, registering authentication success!", response.getStatus());
                    // intercept successful login
                    getSecurityService().interceptLoginSuccess(loginIdentifier);
                } else {
                    log.debug("Filter responded with code {}, skipping result interception", response.getStatus());
                }
            }
        } catch (LockedException | LoginDisabledException | CredentialsExpiredException e) {
            log.debug("{}, cause: {}", e.getMessage(), e);
            authenticationEntryPoint.commence(httpServletRequest, httpServletResponse, e);
        } catch (AuthenticationException e) {
            log.debug("User authentication has failed, {}", e);
            if (!Strings.isNullOrEmpty(loginIdentifier)) {
                // register incorrect login attempt (may be caused by CredentialsExpired)
                getSecurityService().interceptLoginFailure(loginIdentifier, accessTime);
            }
            authenticationEntryPoint.commence(httpServletRequest, httpServletResponse, e);
        }
    }

    /**
     * @return login identifier
     */
    private String getLoginIdentifier(ServletRequest servletRequest, ArtifactoryAuthenticationFilter filter) {
        String loginIdentifier = filter.getCacheKey(servletRequest);
        try {
            // fetch context LoginIdentifier
            loginIdentifier = filter.getLoginIdentifier(servletRequest);
            if (filter instanceof ArtifactoryAccessTokenAuthenticationFilter || filter instanceof ArtifactoryBasicAuthenticationFilter) {
                log.debug("Found loginIdentifier {}", loginIdentifier);
            }
        } catch (BadCredentialsException e) {
            log.debug("Resolving uses access details has failed, {}", e.getMessage());
            if (loginIdentifier == null)
                loginIdentifier = "";
        }
        return loginIdentifier;
    }

    public void destroy() {
        for (ArtifactoryAuthenticationFilter filter : authenticationFilters) {
            filter.destroy();
        }
    }

    private SecurityService getSecurityService() {
        if (securityService == null)
            securityService = ContextHelper.get().beanForType(SecurityService.class);
        return securityService;
    }
}
