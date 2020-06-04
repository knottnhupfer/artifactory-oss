package org.artifactory.security.filters;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Yuval Reches
 */
public interface AuthenticationCacheService {

    /**
     * Holds cached Authentication instances for the non ui requests based on the Authorization header and client ip
     */
    ConcurrentMap<AuthCacheKey, Authentication> getNonUiAuthCache();

    /**
     * @return either the non-UI cached auth or the UI cached auth. The UI cache has a short expiry time to:
     * 1. Allow Artifactory's auth logic to be fully enforced often and thus keep data that is assigned through that process
     * up-to-date (e.g group membership from an external provider).
     * 2. Prevent a cached external auth from using the UI for a prolonged period if that auth is already considered invalid.
     * {@code null} in case authentication not found in either of the caches.
     */
    Authentication getCachedAuthentication(AuthCacheKey authCacheKey,
            HttpServletRequest servletRequest);

    /**
     * Caches the authentication in either the UI or non-UI cache, according to request.
     *
     * @return The username retrieved from newAuthentication
     */
    String cacheAuthAndRetrieveUsername(HttpServletRequest request, AuthCacheKey authCacheKey,
            Authentication newAuthentication);

    void addToUserChange(Authentication authentication);

    /**
     * If the user object changed in the DB: new groups or became admin since last login,
     * then we need to force re-authentication
     */
    boolean reAuthenticatedRequiredUserChanged(Authentication authentication);

}
