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

package org.artifactory.security.props.auth;

import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.security.props.auth.model.TokenKeyValue;
import org.jfrog.security.crypto.JFrogCryptoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Chen Keinan
 */
@Component
public abstract class PropsTokenManager implements TokenManager, EncryptedTokenManager {
    static final Logger log = LoggerFactory.getLogger(PropsTokenManager.class);
    private final Set<String> propKeys = newHashSet();

    @Autowired
    private UserGroupService userGroupService;

    PropsTokenManager() {
        propKeys.add(getPropKey());
    }

    /**
     * return props key for each token type (oauth , apiKey and etc)
     *
     * @return key
     */
    public abstract String getPropKey();

    @Override
    public TokenKeyValue createToken(String userName) {
        TokenKeyValue token = null;
        String tokenValue = null;
        String key = getPropKey();
        try {
            tokenValue = JFrogCryptoHelper.generateUniqueApiKeyToken();
            boolean tokenPropCreated = userGroupService.createPropsToken(userName, key, tokenValue);
            if (tokenPropCreated) {
                token = new TokenKeyValue(key, tokenValue);
            }
        } catch (GeneralSecurityException e) {
            log.debug("error with generating token for user: '{}' with key {}", userName, key, e);
        } catch (Exception e) {
            log.debug("error with adding token for user: '{}' with key {} and value {}", userName, key, tokenValue, e);
        }
        return token;
    }

    @Override
    public TokenKeyValue addExternalToken(String userName, String tokenValue) {
        TokenKeyValue token = null;
        String key = getPropKey();
        try {
            boolean propsToken = userGroupService.createPropsToken(userName, key, tokenValue);
            if (propsToken) {
                token = new TokenKeyValue(key, tokenValue);
            }
        } catch (Exception e) {
            log.debug("error with adding external token for user: '{}' with key {} and value {}", userName, key, tokenValue, e);
        }
        return token;
    }

    @Override
    public TokenKeyValue refreshToken(String userName) {
        TokenKeyValue token = null;
        try {
            String value = JFrogCryptoHelper.generateUniqueApiKeyToken();
            token = updateToken(userName, value);
        } catch (GeneralSecurityException e) {
            log.debug("error with refreshing token for user: '{}'", userName, e);
        }
        return token;
    }

    @Override
    public TokenKeyValue updateToken(String userName, String value) {
        String key = getPropKey();
        TokenKeyValue token = null;
        try {
            boolean propsToken = userGroupService.updatePropsToken(userName, key, value);
            if (propsToken) {
                token = new TokenKeyValue(key, value);
            }
        } catch (Exception e) {
            log.debug("error with updating token for user: '{}' with key {} and value {}", userName, key, value, e);
        }
        return token;
    }

    @Override
    public TokenKeyValue getToken(String userName) {
        String key = getPropKey();
        TokenKeyValue token = null;
        String value = userGroupService.getPropsToken(userName, key);
        if (value != null) {
            token = new TokenKeyValue(key, CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), value));
        }
        return token;
    }

    @Override
    public boolean revokeToken(String userName) {
        boolean tokenRevokeSucceeded = false;
        String key = getPropKey();
        try {
            tokenRevokeSucceeded = userGroupService.revokePropsToken(userName, key);
        } catch (Exception e) {
            log.debug("error with revoking token for user: '{}' with key {}", userName, key, e);
        }
        return tokenRevokeSucceeded;
    }

    @Override
    public boolean revokeAllTokens() {
        boolean tokenRevokeSucceeded = false;
        String key = getPropKey();
        try {
            userGroupService.revokeAllPropsTokens(key);
            tokenRevokeSucceeded = true;
        } catch (Exception e) {
            log.debug("error with revoking all tokens with key {}", key, e);
        }
        return tokenRevokeSucceeded;
    }

    @Override
    public Set<String> getPropKeys() {
        return propKeys;
    }
}
