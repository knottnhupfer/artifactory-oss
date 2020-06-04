package org.artifactory.api.xray;

/**
 * @author Shay Bagants
 */
public interface XrayAuthState {

    /**
     * @return true if the new Access token authentication provider access is enabled for xray
     */
    boolean isXrayAccessTokenAuthProviderEnabled();
}
