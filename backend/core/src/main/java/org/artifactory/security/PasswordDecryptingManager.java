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

package org.artifactory.security;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.security.PasswordSettings;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.access.AccessTokenAuthentication;
import org.artifactory.security.access.AccessTokenAuthenticationProvider;
import org.artifactory.security.db.apikey.PropsAuthenticationProvider;
import org.artifactory.security.props.auth.ApiKeyManager;
import org.artifactory.security.props.auth.PropsAuthenticationToken;
import org.jfrog.access.client.AccessClientException;
import org.jfrog.security.crypto.DecodedKeyPair;
import org.jfrog.security.crypto.EncodedKeyPair;
import org.jfrog.security.crypto.EncryptionWrapper;
import org.jfrog.security.crypto.EncryptionWrapperFactory;
import org.jfrog.security.crypto.result.DecryptionStatusHolder;
import org.jfrog.security.crypto.result.DecryptionStringResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * This authentication manager will decrypted any encrypted passwords according to the password and encryption policy
 * and delegate the authentication to a standard authentication provider.
 *
 * @author Yossi Shaul
 * @deprecated since artifactory shouldn't encrypt private/public keys anymore (access does)
 */
@Deprecated
public class PasswordDecryptingManager implements AuthenticationManager {
    private static final Logger log = LoggerFactory.getLogger(PasswordDecryptingManager.class);

    private AuthenticationManager delegate;
    private PropsAuthenticationProvider delegateProps;
    private AccessTokenAuthenticationProvider delegateAccessToken;

    private AuthorizationService authService;
    private CentralConfigService centralConfigService;
    private UserGroupService userGroupService;
    private AddonsManager addonsManager;

    @Autowired
    public PasswordDecryptingManager(AuthorizationService authService, CentralConfigService centralConfigService,
            UserGroupService userGroupService, AddonsManager addonsManager) {
        this.authService = authService;
        this.centralConfigService = centralConfigService;
        this.userGroupService = userGroupService;
        this.addonsManager = addonsManager;
    }

    /**
     * Attempts to authenticate the passed {@link Authentication} object, returning a fully populated
     * <code>Authentication</code> object (including granted authorities) if successful.
     * <p>
     * An <code>AuthenticationManager</code> must honour the following contract concerning exceptions:
     * <ul>
     * <li>A {@link org.springframework.security.authentication.DisabledException} must be thrown if an account is disabled and the
     * <code>AuthenticationManager</code> can test for this state.</li>
     * <li>A {@link org.springframework.security.authentication.LockedException} must be thrown if an account is locked and the
     * <code>AuthenticationManager</code> can test for account locking.</li>
     * <li>A {@link org.springframework.security.authentication.BadCredentialsException} must be thrown if incorrect credentials are presented. Whilst the
     * above exceptions are optional, an <code>AuthenticationManager</code> must <B>always</B> test credentials.</li>
     * </ul>
     * Exceptions should be tested for and if applicable thrown in the order expressed above (i.e. if an
     * account is disabled or locked, the authentication request is immediately rejected and the credentials testing
     * process is not performed). This prevents credentials being tested against  disabled or locked accounts.
     *
     * @param authentication the authentication request object
     * @return a fully authenticated object including credentials
     * @throws AuthenticationException if authentication fails
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.trace("Received authentication request for {}", authentication);
        String password = authentication.getCredentials().toString();
        Object principal = authentication.getPrincipal();
        String username = null;
        if (principal != null) {
            username = principal.toString();
        }

        if (authentication instanceof PropsAuthenticationToken) {
            try {
                return delegateProps.authenticate(authentication);
            } catch (AuthenticationException e) {
                Object propsKey = ((PropsAuthenticationToken) authentication).getPropsKey();
                if (!ApiKeyManager.API_KEY.equals(propsKey) &&
                        delegateAccessToken.isAccessToken(String.valueOf(authentication.getCredentials()))) {
                    authentication = new AccessTokenAuthentication(password, username, null);
                    return delegateAccessToken.authenticate(authentication);
                }
                throw e;
            }
        }

        boolean isApiKey = CryptoHelper.isApiKey(password);
        if (isApiKey) {
            // The API Key is used as a password => Transforming the token
            authentication = new PropsAuthenticationToken(username, ApiKeyManager.API_KEY, password, null);
            return delegateProps.authenticate(authentication);
        } else if (delegateAccessToken.isAccessToken(password)) {
            authentication = new AccessTokenAuthentication(password, username, null);
            return delegateAccessToken.authenticate(authentication);
        } else {
            if (needsDecryption(password, username, (authentication instanceof InternalUsernamePasswordAuthenticationToken))) {
                log.trace("Decrypting user password for user: '{}'", username);
                try {
                    password = decryptPassword(password, username);
                    UsernamePasswordAuthenticationToken newAuthToken = new UsernamePasswordAuthenticationToken(username,
                            password);
                    newAuthToken.setDetails(authentication.getDetails());
                    authentication = newAuthToken;
                } catch (PasswordEncryptionException e) {
                    log.warn("Failed decrypting password. Trying to authenticate with original password.", e);
                }
            }
        }

        return delegate.authenticate(authentication);
    }

    private boolean needsDecryption(String password, String username, boolean internalRequest) {
        CentralConfigDescriptor centralConfigDescriptor = centralConfigService.getDescriptor();
        SecurityDescriptor securityDescriptor = centralConfigDescriptor.getSecurity();
        XrayAddon xrayAddon = addonsManager.addonByType(XrayAddon.class);
        PasswordSettings passwordSettings = securityDescriptor.getPasswordSettings();
        boolean encryptionEnabled = passwordSettings.isEncryptionEnabled();
        if (!encryptionEnabled) {
            return false;
        }
        boolean isEncrypted = CryptoHelper.isEncryptedUserPassword(password);
        log.trace("Detected {} password", isEncrypted ? "encrypted" : "cleartext");
        if (isEncrypted) {
            return true;
        }
        boolean mustBeEncrypted =
                 !internalRequest && passwordSettings.isEncryptionRequired()
                         && xrayAddon.isDecryptionNeeded(username)
                         && (StringUtils.isNotBlank(password) || !authService.isAnonymous());
        log.trace("Password encryption is {}required", mustBeEncrypted ? "" : "not ");
        if (!mustBeEncrypted) {
            return false;
        }
        log.debug("Cleartext passwords not allowed. Sending unauthorized response");
        throw new PasswordEncryptionException("Artifactory configured to accept only " +
                "encrypted passwords but received a clear text password, getting the encrypted password can be done via the WebUI.");
    }

    private String decryptPassword(String encryptedPassword, String username) {
        if (!CryptoHelper.isEncryptedUserPassword(encryptedPassword)) {
            throw new IllegalArgumentException("Password not encrypted");
        }
        try {
            DecryptionStringResult decryptionResult = getEncryptionWrapper(username).decryptIfNeeded(encryptedPassword);
            return decryptionResult.getDecryptedData();

        } catch (Exception e) {
            // Disgusting hack in order to present a nice proper error message to the user when Access is down
            if (e instanceof AccessClientException) {
                throw e;
            }
            log.debug("Failed to decrypt password for user: '{}' : {}", username, e.getMessage());
            throw new PasswordEncryptionException("Failed to decrypt password.", e);
        }
    }

    private EncryptionWrapper getEncryptionWrapper(String username) {
        UserInfo userInfo = userGroupService.findUser(username);
        String privateKey = userInfo.getPrivateKey();
        String publicKey = userInfo.getPublicKey();
        if (privateKey == null || publicKey == null) {
            String message = String.format(
                    "The user: '%s' with no key pair tries to authenticate with encrypted password.", username);
            log.debug(message);
            throw new PasswordEncryptionException(message);
        }
        EncryptionWrapper artifactoryKeyWrapper = ArtifactoryHome.get().getArtifactoryEncryptionWrapper();
        EncodedKeyPair encodedKeyPair = new EncodedKeyPair(privateKey, publicKey);
        EncodedKeyPair newEncodedKeyPair = encodedKeyPair.toSaveEncodedKeyPair(artifactoryKeyWrapper);
        if (newEncodedKeyPair != null) {
            log.info("Re-encoding key pair for user: '{}' since keys are using the old format", username);
            userGroupService.createEncryptedPasswordIfNeeded(userInfo, "dummy");
        }

        DecryptionStatusHolder decryptionStatus = new DecryptionStatusHolder();
        DecodedKeyPair decodedKeyPair = encodedKeyPair.decode(null, decryptionStatus);
        reEncryptKeysIfNeeded(userInfo, decryptionStatus, decodedKeyPair);
        // We have created different constructor that accepts decoded decrypted keys, to avoid decrypt the encoded again keychain and save performance
        return EncryptionWrapperFactory.createKeyWrapper(decodedKeyPair);
    }

    private void reEncryptKeysIfNeeded(UserInfo userInfo,
                                       DecryptionStatusHolder decryptionStatus, DecodedKeyPair decodedKeyPair) {
        EncodedKeyPair encodedKeyPair;
        log.trace("Checking if re-encoding of keys is required");
        if (decryptionStatus.hadFallback()) {
            log.trace("Re encoding keys of keys is required");
            MutableUserInfo mutableUser = InfoFactoryHolder.get().copyUser(userInfo);
            encodedKeyPair = new EncodedKeyPair(decodedKeyPair, null);
            mutableUser.setPrivateKey(encodedKeyPair.getEncodedPrivateKey());
            mutableUser.setPublicKey(encodedKeyPair.getEncodedPublicKey());
            userGroupService.updateUser(mutableUser, false);
        }
    }

    public void setDelegate(AuthenticationManager delegate) {
        this.delegate = delegate;
    }

    public void setDelegateProps(PropsAuthenticationProvider delegateProps) {
        this.delegateProps = delegateProps;
    }

    public void setDelegateAccessToken(AccessTokenAuthenticationProvider delegateAccessToken) {
        this.delegateAccessToken = delegateAccessToken;
    }
}
