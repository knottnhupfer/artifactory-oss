/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2020 JFrog Ltd.
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

import org.apache.commons.lang3.StringUtils;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.security.AccessLogger;
import org.artifactory.security.HttpAuthenticationDetailsSource;
import org.artifactory.security.UserInfo;
import org.artifactory.security.filters.ArtifactoryAuthenticationFilter;
import org.artifactory.security.filters.AuthCacheKey;
import org.artifactory.security.filters.AuthenticationCacheService;
import org.artifactory.security.providermgr.TokenProviderResponseAuthentication;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.SessionUtils;
import org.artifactory.util.UiRequestUtils;
import org.artifactory.webapp.servlet.authentication.ArtifactoryAuthenticationFilterChain;
import org.artifactory.webapp.servlet.authentication.interceptor.anonymous.AnonymousAuthenticationInterceptor;
import org.artifactory.webapp.servlet.authentication.interceptor.anonymous.AnonymousAuthenticationInterceptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;


/**
 * @author Yoav Landman
 */
public class AccessFilter extends DelayedFilterBase {
    private static final Logger log = LoggerFactory.getLogger(AccessFilter.class);

    public static final String AUTHENTICATED_USERNAME_ATTRIBUTE = "authenticated_username";

    private ArtifactoryContext context;
    private ArtifactoryAuthenticationFilterChain authFilterChain;
    private BasicAuthenticationEntryPoint authenticationEntryPoint;
    private AnonymousAuthenticationInterceptors authInterceptors;
    private AuthenticationCacheService authenticationCacheService;

    @Override
    public void initLater(FilterConfig filterConfig) throws ServletException {
        ServletContext servletContext = filterConfig.getServletContext();
        this.context = RequestUtils.getArtifactoryContext(servletContext);
        this.authenticationEntryPoint = context.beanForType(BasicAuthenticationEntryPoint.class);
        this.authFilterChain = new ArtifactoryAuthenticationFilterChain(authenticationEntryPoint);
        // Add all the authentication filters
        authFilterChain.addFilters(context.beansForType(ArtifactoryAuthenticationFilter.class).values());
        authenticationCacheService = context.beanForType(AuthenticationCacheService.class);
        authFilterChain.init(filterConfig);
        authInterceptors = new AnonymousAuthenticationInterceptors();
        RequestUtils.setPackagesEndpointUseBasicAuth();
        authInterceptors.addInterceptors(context.beansForType(AnonymousAuthenticationInterceptor.class).values());
    }

    @Override
    public void destroy() {
        //May not be inited yet
        if (authFilterChain != null) {
            authFilterChain.destroy();
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        if (shouldSkipFilter(req)) {
            chain.doFilter(req, resp);
            return;
        }
        doFilterInternal((HttpServletRequest) req, ((HttpServletResponse) resp), chain);
    }

    @Override
    boolean shouldSkipFilter(ServletRequest req) {
        return super.shouldSkipFilter(req) || isOpenidGatewayResource(((HttpServletRequest) req));
    }

    private ConcurrentMap<AuthCacheKey, Authentication> getNonUiAuthCache() {
        return authenticationCacheService.getNonUiAuthCache();
    }

    /**
     * Special temporary hack for accessfactory to make sure we use access audience instead of artifactory's
     */
    private boolean isOpenidGatewayResource(HttpServletRequest request) {
        return !isDefaultAuthorizationRequired(request) &&
                request.getRequestURI().contains("api/" + SystemRestConstants.PATH_OPENID);
    }

    private boolean isDefaultAuthorizationRequired(HttpServletRequest request) {
        String defaultAuth = request.getHeader("X-Default-Authorization");
        return defaultAuth != null && defaultAuth.toLowerCase().equals("true");
    }

    private void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (shouldRedirectWebapp(request, response, RequestUtils.getServletPathFromRequest(request), request.getMethod())) {
            return;
        }

        // Reuse the authentication if it exists
        Authentication authentication = SessionUtils.getAuthentication(request);
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();

        // Find the good filter chain for this request
        ArtifactoryAuthenticationFilter authenticationFilter = authFilterChain.acceptFilter(request);

        // Make sure this is called only once (FRED: it's called twice ?!?)
        boolean reauthRequired = handleReauthentication(request, response, authentication, authenticationFilter);

        boolean authenticationRequired = !isAuthenticated || reauthRequired;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (authenticationRequired) {
            if (authenticationFilter != null && authenticationFilter.acceptFilter(request)) {
                authenticateAndExecute(request, response, chain, securityContext, authenticationFilter, authFilterChain);
            } else {
                useAnonymousIfPossible(request, response, chain, securityContext, authenticationFilter);
            }
        } else {
            log.debug("Using authentication {} from Http session.", authentication);
            useAuthenticationAndContinue(request, response, chain, authentication, securityContext);
        }
    }

    private boolean handleReauthentication(HttpServletRequest request, HttpServletResponse response, Authentication authentication, ArtifactoryAuthenticationFilter authenticationFilter) {
        if (isReAuthenticationRequired(request, authentication, authenticationFilter)) {
            // A re-authentication is required but we might still have data that needs to be invalidated (like the web session)
            Map<String, LogoutHandler> logoutHandlers = ContextHelper.get().beansForType(LogoutHandler.class);
            for (LogoutHandler logoutHandler : logoutHandlers.values()) {
                logoutHandler.logout(request, response, authentication);
            }

            return true;
        }

        return false;
    }

    private boolean shouldRedirectWebapp(HttpServletRequest request, HttpServletResponse response, String servletPath, String method) throws IOException {
        // add no cache header to web app request
        RequestUtils.addAdditionalHeadersToWebAppRequest(request, response);

        if ("get".equalsIgnoreCase(method) &&
                (servletPath == null || "/".equals(servletPath) || servletPath.length() == 0 ||
                        servletPath.equals("/" + HttpUtils.WEBAPP_URL_PATH_PREFIX))) {
            //We were called with an empty path - redirect to the app main page
            String baseUrl = HttpUtils.getServletContextUrl(request);
            response.sendRedirect(baseUrl + "/" + HttpUtils.WEBAPP_URL_PATH_PREFIX + "/");
            return true;
        }
        return false;
    }

    private boolean isReAuthenticationRequired(HttpServletRequest request, Authentication authentication,
            ArtifactoryAuthenticationFilter authenticationFilter) {
        // Not authenticated so not required to redo ;-)
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // If the user object changed in the DB: new groups or became admin since last login,
        // Then we need to force re-authentication
        if (authenticationCacheService.reAuthenticatedRequiredUserChanged(authentication)) {
            return true;
        }

        if (isAuthenticatedUIRequest(request, authenticationFilter, authentication)) {
            return false;
        }

        // Ask the filter chain if we need to re authenticate
        return authenticationFilter == null || authenticationFilter.requiresReAuthentication(request, authentication);
    }

    /**
     * if request related to UI and already authenticated the it do not require re-authentication
     *
     * @param request              - http servlet request
     * @param authenticationFilter - accepted authentication filter if null no filter accept this request
     * @return true if require authentication
     */
    private boolean isAuthenticatedUIRequest(HttpServletRequest request,
            ArtifactoryAuthenticationFilter authenticationFilter, Authentication authentication) {
        // @Todo need to do the ui request identification from the UI by sending to user Agent
        // @Todo header to identified UI request for download and upload requests
        if (authenticationFilter == null && (authentication.getClass().getSimpleName()
                .equals("HttpSsoAuthenticationToken"))) {
            return false;
        }
        return UiRequestUtils.isUiRestRequest(request) ||
                ((request.getRequestURI().contains(HttpUtils.WEBAPP_URL_PATH_PREFIX)) && authenticationFilter == null) ||
                authenticationFilter == null;
    }

    private void authenticateAndExecute(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain, SecurityContext securityContext,
            ArtifactoryAuthenticationFilter authFilter, ArtifactoryAuthenticationFilterChain authFilterChain)
            throws IOException, ServletException {
        AuthCacheKey authCacheKey = getAuthCacheKey(request, authFilter);
        Authentication authentication = authenticationCacheService.getCachedAuthentication(authCacheKey, request);
        if (authentication != null && authentication.isAuthenticated()
                && !isReAuthenticationRequired(request, authentication, authFilter)) {
            log.debug("Header authentication {} found in cache.", authentication);
            useAuthenticationAndContinue(request, response, chain, authentication, securityContext);
            // Add to user change cache the login state
            authenticationCacheService.addToUserChange(authentication);
            return;
        }
        try {
            authFilterChain.doFilter(request, response, authFilter, chain);
        } finally {
            String username = "non_authenticated_user";
            Authentication newAuthentication = securityContext.getAuthentication();
            if (newAuthentication != null && newAuthentication.isAuthenticated()) {
                username = cacheAuthIfNeededAndRetrieveUsername(request, authCacheKey, username, newAuthentication);
            } else {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(extractUsername(request),null);
                authenticationToken.setDetails(new WebAuthenticationDetails(request));
                AccessLogger.loginDenied(authenticationToken);
            }
            securityContext.setAuthentication(null);
            request.setAttribute(AUTHENTICATED_USERNAME_ATTRIBUTE, username);
        }
    }

    private String cacheAuthIfNeededAndRetrieveUsername(HttpServletRequest request, AuthCacheKey authCacheKey, String username,
            Authentication newAuthentication) {
        if (newAuthentication instanceof TokenProviderResponseAuthentication) {
            log.debug("Not caching authentication as it was already cached at the token provider login handler");
        } else {
            username = authenticationCacheService.cacheAuthAndRetrieveUsername(request, authCacheKey, newAuthentication);
        }
        return username;
    }

    private String extractUsername(HttpServletRequest request) {
        String username = RequestUtils.extractUsernameFromRequest(request);
        return StringUtils.isNotBlank(username) ? username : "NA";
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    private void useAnonymousIfPossible(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain, SecurityContext securityContext,
            ArtifactoryAuthenticationFilter authFilter) throws IOException, ServletException {
        boolean anonAccessEnabled = context.getAuthorizationService().isAnonAccessEnabled();
        if ((anonAccessEnabled) || authInterceptors.accept(request)) {
            log.debug("Using anonymous");
            AuthCacheKey authCacheKey = getAuthCacheKey(request, authFilter);
            Authentication authentication = authenticationCacheService.getCachedAuthentication(authCacheKey, request);
            if (authentication == null) {
                log.debug("Creating the Anonymous token");
                UsernamePasswordAuthenticationToken authRequest =
                        new UsernamePasswordAuthenticationToken(UserInfo.ANONYMOUS, "");
                AuthenticationDetailsSource ads = new HttpAuthenticationDetailsSource();
                //noinspection unchecked
                authRequest.setDetails(ads.buildDetails(request));
                // explicitly ask for the default spring authentication manager by name (we have another one which
                // is only used by the basic authentication filter)
                AuthenticationManager authenticationManager =
                        context.beanForType("authenticationManager", AuthenticationManager.class);
                authentication = authenticationManager.authenticate(authRequest);
                if (authentication != null && authentication.isAuthenticated() && !RequestUtils.isUiRequest(request)) {
                    getNonUiAuthCache().put(authCacheKey, authentication);
                    log.debug("Added anonymous authentication {} to cache", authentication);
                }
            } else {
                log.debug("Using cached anonymous authentication");
            }
            useAuthenticationAndContinue(request, response, chain, authentication, securityContext);
        } else {
            if (!RequestUtils.isUiRequest(request)) {
                log.debug("Sending request requiring authentication");
                authenticationEntryPoint.commence(request, response,
                        new InsufficientAuthenticationException("Authentication is required"));
            } else {
                log.debug("No filter or entry just chain");
                chain.doFilter(request, response);
            }
        }
    }

    /**
     * Provides a cache key instance based on the {@link ArtifactoryAuthenticationFilter#getCacheKey(ServletRequest)}
     * provided as param.
     *
     * @param authFilter to use it's cache key
     */
    private AuthCacheKey getAuthCacheKey(HttpServletRequest request, ArtifactoryAuthenticationFilter authFilter) {
        AuthCacheKey authCacheKey;
        if (authFilter != null) {
            // Try to see if authentication in cache based on the hashed header and client ip
            String cacheKey = authFilter.getCacheKey(request);
            log.debug("Cached key has been found for request: '{}' with method: '{}'",
                    request.getRequestURI(), request.getMethod());
            authCacheKey = new AuthCacheKey(cacheKey, request.getRemoteAddr());
        } else {
            authCacheKey = new AuthCacheKey(null, request.getRemoteAddr());
        }
        return authCacheKey;
    }

    /**
     * Goes on with the original request (i.e moving on to the resource the request tried to reach)
     */
    private void useAuthenticationAndContinue(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authentication, SecurityContext securityContext) throws IOException, ServletException {
        try {
            securityContext.setAuthentication(authentication);
            // Continues with original request
            chain.doFilter(request, response);
            authenticationCacheService.addToUserChange(authentication);
        } finally {
            securityContext.setAuthentication(null);
            request.setAttribute(AUTHENTICATED_USERNAME_ATTRIBUTE,
                    authentication != null ? authentication.getName() : "non_authenticated_user");
        }
    }

}