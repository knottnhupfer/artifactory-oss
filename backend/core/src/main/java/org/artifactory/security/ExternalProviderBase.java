package org.artifactory.security;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.springframework.security.core.Authentication;

import static org.artifactory.api.security.SecurityService.USER_XRAY;

/**
 * @author dudim
 */
public abstract class ExternalProviderBase {

    private AddonsManager addonsManager;

    public ExternalProviderBase(AddonsManager addonsManager) {
        this.addonsManager = addonsManager;
    }

    /**
     * preliminary checks if should reach to external provider
     */
    protected boolean shouldNotAuthenticate(Authentication authentication) {
        String userName = authentication.getName();
        // If it's an anonymous user or Xray user with Xray enabled don't bother searching for the user.
        return UserInfo.ANONYMOUS.equals(userName) || isXrayUserAndXrayEnabled(userName);
    }

    private boolean isXrayUserAndXrayEnabled(String userName) {
        if (!USER_XRAY.equals(userName)) {
            return false;
        }
        XrayAddon xrayAddon = addonsManager.addonByType(XrayAddon.class);
        return xrayAddon.isXrayEnabled();
    }
}