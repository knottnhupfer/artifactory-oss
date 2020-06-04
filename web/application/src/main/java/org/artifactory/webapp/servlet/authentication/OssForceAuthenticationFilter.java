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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.security.filters.ArtifactoryAuthenticationFilter;
import org.artifactory.util.HttpUtils;
import org.artifactory.webapp.servlet.HttpArtifactoryRequest;
import org.artifactory.webapp.servlet.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.apache.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.artifactory.webapp.servlet.authentication.ArtifactoryBasicAuthenticationEntryPoint.REALM;
import static org.artifactory.webapp.servlet.authentication.AuthenticationFilterUtils.isRequestContainsAuthentication;

/**
 * A filter that sends a HTTP 401 challenge (WWW-Authenticate header) to all un-authenticated requests
 * going to Maven virtual repositories configured to force authentication.
 *
 * Note: this filter is extended for Pro versions at MavenForceAuthenticationFilter
 * and will not be loaded on Pro instances (due to Spring's addon.xml 'primary' attribute)
 *
 * We use the filter when ALL the following are true:
 * 1. Repo is Maven Virtual and flag {@link VirtualRepoDescriptor#forceMavenAuthentication} is turned ON
 * 2. Request is not to UI / API / WEBAPP
 * 3. Request has no authentication attached - no other filters accept the request
 *
 * @author Uriah Levy
 * @author Yuval Reches
 */
public class OssForceAuthenticationFilter implements ArtifactoryAuthenticationFilter {
    private static final Logger log = LoggerFactory.getLogger(OssForceAuthenticationFilter.class);

    private RepositoryService repoService;
    private SecurityService securityService;

    @Autowired
    public void setRepoService(RepositoryService repoService) {
        this.repoService = repoService;
    }

    @Autowired
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public boolean requiresReAuthentication(ServletRequest request, Authentication authentication) {
        // For cases in which we already have cached authentication (of anonymous for example)
        return acceptFilter(request);
    }

    @Override
    public boolean acceptFilter(ServletRequest request) {
        boolean repoIsForcingAuthentication = isMavenVirtualRepoForcedAuthentication(request);
        boolean noAuthentication = !isRequestContainsAuthentication((HttpServletRequest) request, securityService);
        String path = RequestUtils.getServletPathFromRequest((HttpServletRequest) request);
        log.trace("[{}] repoIsForcingAuthentication: {}; noAuthentication:{}", path, repoIsForcingAuthentication,
                noAuthentication);
        return repoIsForcingAuthentication && noAuthentication;
    }

    private boolean isMavenVirtualRepoForcedAuthentication(ServletRequest request) {
        String repoKey = tryExtractingRepoKeyFromRequest(request);
        if (StringUtils.isBlank(repoKey)) {
            log.trace("Request is not to a repo");
            return false;
        }
        RepoDescriptor repoDescriptor = repoService.repoDescriptorByKey(repoKey);
        if (repoDescriptor instanceof VirtualRepoDescriptor && RepoType.Maven.equals(repoDescriptor.getType())) {
            return ((VirtualRepoDescriptor) repoDescriptor).isForceMavenAuthentication();
        }
        log.trace("Request is to repo {} which is not a virtual Maven repo", repoKey);
        return false;
    }

    private String tryExtractingRepoKeyFromRequest(ServletRequest request) {
        ArtifactoryRequest artifactoryRequest;
        try {
            artifactoryRequest = new HttpArtifactoryRequest((HttpServletRequest) request);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        String repoKey = artifactoryRequest.getRepoKey();
        if ("ui".equals(repoKey) || "api".equals(repoKey) || "webapp".equals(repoKey)) {
            return null;
        }
        return repoKey;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        sendHttpAuthChallenge((HttpServletResponse) response);
    }

    private void sendHttpAuthChallenge(HttpServletResponse response) throws IOException {
        log.debug("Anonymous user resolving via virtual repo with authRequired flag on - sending auth challenge");
        response.addHeader(WWW_AUTHENTICATE, "Basic realm=\"" + REALM + "\"");
        HttpUtils.sendErrorResponse(response, HttpStatus.SC_UNAUTHORIZED, "Unauthorized");
    }

    @Override
    public String getCacheKey(ServletRequest request) {
        return null;
    }

    @Override
    public String getLoginIdentifier(ServletRequest request) {
        return null;
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}

