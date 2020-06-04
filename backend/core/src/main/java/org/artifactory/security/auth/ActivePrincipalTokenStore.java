package org.artifactory.security.auth;

/**
 * @author Uriah Levy
 * A store for active-principal Access Tokens. The tokens are "impersonation tokens" that can be used against external
 * services such as the Metadata Service. They are scoped based on the groups associated with the logged in user during
 * the current active session
 */
public interface ActivePrincipalTokenStore {
    /**
     * Get a token for the current logged in user
     */
    String getOrLoadToken();


    /**
     * Invalidate the cached token of the given user
     *
     * @param username the username whose token will be invalidated
     */
    void invalidateToken(String username);
}
