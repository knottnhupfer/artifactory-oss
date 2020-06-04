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

import org.artifactory.security.filters.ArtifactoryAuthenticationFilter;
import org.artifactory.security.props.auth.PropsAuthenticationToken;
import org.artifactory.security.props.auth.model.TokenKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Chen Keinan
 */
public class PropsAuthenticationFilter implements ArtifactoryAuthenticationFilter {
    private static final Logger log = LoggerFactory.getLogger(PropsAuthenticationFilter.class);

    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private RememberMeServices rememberMeServices = new NullRememberMeServices();
    private AuthenticationManager authenticationManager;
    private BasicAuthenticationEntryPoint authenticationEntryPoint;

    public PropsAuthenticationFilter(AuthenticationManager authenticationManager,
                                     BasicAuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationManager = authenticationManager;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    /**
     * This filter allows any authentication retrieved from cache to be used, without the need of re-authentication.
     * In case users would like to get "tighter" security they should reduce the cache expiration time
     * (See {@link org.artifactory.common.ConstantValues#securityAuthenticationCacheIdleTimeSecs})
     *
     * @param request        The http request
     * @param authentication A valid authenticated authentication retrieved from authentication cache
     */
    public boolean requiresReAuthentication(ServletRequest request, Authentication authentication) {
        HttpServletRequest req = (HttpServletRequest) request;
        TokenKeyValue tokenKeyValue = AuthenticationFilterUtils.getTokenKeyValueFromHeader(req);
        if (tokenKeyValue != null && authentication != null) {
            return false;
        }
        return acceptFilter(request);
    }

    @Override
    public boolean acceptFilter(ServletRequest request) {
        return AuthenticationFilterUtils.getTokenKeyValueFromHeader((HttpServletRequest) request) != null;
    }

    @Override
    public String getCacheKey(ServletRequest request) {
        TokenKeyValue tokenKeyValue = AuthenticationFilterUtils.getTokenKeyValueFromHeader((HttpServletRequest) request);
        if (tokenKeyValue != null) {
            return tokenKeyValue.getToken();
        }
        return null;
    }

    /**
     * @param request        The http request
     * @return Login identifier such as user, sessionId, apiKey, etc.
     */
    @Override
    public String getLoginIdentifier(ServletRequest request) {
        TokenKeyValue tokenKeyValue = AuthenticationFilterUtils.getTokenKeyValueFromHeader((HttpServletRequest) request);
        return tokenKeyValue != null ? tokenKeyValue.getToken() : getCacheKey(request);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        try {
            // try authenticate
            TokenKeyValue tokenKeyValue = AuthenticationFilterUtils.getTokenKeyValueFromHeader(request);
            if (tokenKeyValue != null) {
                log.trace("try authenticate with {}", tokenKeyValue.getKey());
                // try authenticate with prop token
                Authentication authResult = tryAuthenticate(request, tokenKeyValue);
                // update security context with new authentication
                updateContext(request, response, authResult);
                log.trace("authentication with props token {} succeeded", tokenKeyValue.getKey());
            }
        } catch (AuthenticationException failed) {
            // clear security context
            clearContext(request, response, failed);
            authenticationEntryPoint.commence(request, response, failed);
            return;
        }
        chain.doFilter(request, response);
    }

    /**
     * clear security context from authentication data
     *
     * @param request  - http servlet request
     * @param response - http servlet response
     * @param failed   - authentication run time exception
     */
    private void clearContext(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        SecurityContextHolder.clearContext();
        log.debug("Authentication request for failed: " + failed);
        rememberMeServices.loginFail(request, response);
    }

    /**
     * update security context with new authentication
     *
     * @param request    - http servlet request
     * @param response   - http servlet response
     * @param authResult - new authentication
     */
    private void updateContext(HttpServletRequest request, HttpServletResponse response, Authentication authResult) {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        rememberMeServices.loginSuccess(request, response, authResult);
    }


    /**
     * create pre authentication instance
     *
     * @param request - http servlet request
     * @param tokenKeyValue  - token key and value
     * @return authentication
     */
    private Authentication tryAuthenticate(HttpServletRequest request, TokenKeyValue tokenKeyValue) {
        // create pre authentication
        PropsAuthenticationToken authentication = new PropsAuthenticationToken(null,
                tokenKeyValue.getKey(), tokenKeyValue.getToken(), null);
        authentication.setDetails(authenticationDetailsSource.buildDetails(request));
        // try authenticate via token
        return authenticationManager.authenticate(authentication);
    }

    @Override
    public void destroy() {
    }
}
