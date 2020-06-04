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

import org.artifactory.security.access.AccessService;
import org.artifactory.security.filters.ArtifactoryAuthenticationFilter;
import org.jfrog.access.token.JwtAccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Nonnull;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author nadavy
 */
public class ArtifactoryAccessTokenAuthenticationFilter implements ArtifactoryAuthenticationFilter {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryAccessTokenAuthenticationFilter.class);

    private static final String TOKEN_QUERY_PARAM_PREFIX = "token=";

    private AuthenticationManager passwordDecryptingManager;

    @Autowired
    private AccessService accessService;

    @Override
    public boolean requiresReAuthentication(ServletRequest request, Authentication authentication) {
        return false;
    }

    public ArtifactoryAccessTokenAuthenticationFilter(AuthenticationManager passwordDecryptingManager) {
        this.passwordDecryptingManager = passwordDecryptingManager;
    }

    @Override
    public boolean acceptFilter(ServletRequest request) {
        return extractQueryParamAccessToken((HttpServletRequest) request) != null;
    }

    @Override
    public String getCacheKey(ServletRequest request) {
        HttpServletRequest req = (HttpServletRequest) request;
        String queryString = req.getQueryString();
        if (queryString != null && queryString.startsWith(TOKEN_QUERY_PARAM_PREFIX)) {
            return queryString.replaceFirst(TOKEN_QUERY_PARAM_PREFIX, "");
        }
        return null;
    }

    @Override
    public String getLoginIdentifier(ServletRequest request) {
        JwtAccessToken jwtAccessToken = extractQueryParamAccessToken((HttpServletRequest) request);
        if (jwtAccessToken != null) {
            return extractUsername(jwtAccessToken);
        }
        return null;
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        JwtAccessToken jwtAccessToken = extractQueryParamAccessToken(request);
        try {
            // try authenticate
            if (jwtAccessToken != null) {
                log.trace("trying authenticate with query param access token {}", jwtAccessToken.getTokenValue());
                // try authenticate with access token
                String principalByToken = accessService.extractSubjectUsername(jwtAccessToken);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        principalByToken, jwtAccessToken.getTokenValue(), null);
                authentication = passwordDecryptingManager.authenticate(authentication);
                // update security context with new authentication
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.trace("authentication with query param access token {} succeeded", jwtAccessToken.getTokenValue());
            }
        } catch (AuthenticationException failed) {
            SecurityContextHolder.clearContext();
            String msg = "Failed to authenticate request with token " + jwtAccessToken.getTokenValue();
            log.error(msg);
            log.debug(msg, failed);
            response.sendError(401, msg);
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

    @Nonnull
    private String extractUsername(@Nonnull JwtAccessToken accessToken) {
        String username = accessService.extractSubjectUsername(accessToken);
        if (username != null) {
            return username;
        }
        return accessToken.getSubject();
    }

    private JwtAccessToken extractQueryParamAccessToken(HttpServletRequest httpRequest) {
        String queryString = httpRequest.getQueryString();
        if (queryString != null && queryString.startsWith(TOKEN_QUERY_PARAM_PREFIX)) {
            String accessToken = queryString.replaceFirst(TOKEN_QUERY_PARAM_PREFIX, "");
            return quietlyParseToken(accessToken);
        }
        return null;
    }

    private JwtAccessToken quietlyParseToken(String accessToken) {
        try {
            return accessService.parseToken(accessToken);
        } catch (IllegalArgumentException e) {
            log.trace("", e);
            return null;
        }
    }
}
