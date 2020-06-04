package org.artifactory.rest.common.access;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.artifactory.rest.exception.ForbiddenWebAppException;
import org.artifactory.security.access.AccessTokenAuthentication;
import org.jfrog.access.token.JwtAccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import javax.annotation.Nonnull;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author haims
 */
public class AccessTokenScopeResourceFilter implements ContainerRequestFilter {

    private static final LoadingCache<String, Optional<RestPathScopePattern>> PATH_PATTERNS = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(100)
            .build(new CacheLoader<String, Optional<RestPathScopePattern>>() {
                @Override
                public Optional<RestPathScopePattern> load(@Nonnull String scopeToken) throws Exception {
                    return RestPathScopePattern.parseOptional(scopeToken);
                }
            });

    private static final Logger log = LoggerFactory.getLogger(AccessTokenScopeResourceFilter.class);

    @Override
    public void filter(ContainerRequestContext request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AccessTokenAuthentication) {
            JwtAccessToken accessToken = ((AccessTokenAuthentication) authentication).getAccessToken();
            checkRequestPath(accessToken, request);
        }
    }

    private void checkRequestPath(JwtAccessToken accessToken, ContainerRequestContext request) {
        if (!requestPathMatchesAnyPathPatternScope(request, accessToken)) {
            if (log.isDebugEnabled()) {
                log.debug("Request failed token authorization - " +
                                "request path '{}' does not match allowed patterns in the token scope: {}",
                        request.getUriInfo().getAbsolutePath(), String.join(" ", accessToken.getScope()));
            }
            throw new ForbiddenWebAppException("Request path not allowed");
        }
    }

    private boolean requestPathMatchesAnyPathPatternScope(ContainerRequestContext request, JwtAccessToken accessToken) {
        return accessToken.getScope().stream().anyMatch(scopeToken -> {
            try {
                Optional<RestPathScopePattern> pathScopePattern = PATH_PATTERNS.get(scopeToken);
                return pathScopePattern.map(pattern -> pattern.matches(request)).orElse(false);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
