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

package org.artifactory.layout;

import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.config.ConfigurationChangesInterceptor;
import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.mail.MailServerDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.accesstoken.AccessClientSettings;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.descriptor.security.ldap.SearchPattern;
import org.artifactory.descriptor.security.oauth.OAuthProviderSettings;
import org.artifactory.descriptor.security.oauth.OAuthSettings;
import org.artifactory.descriptor.security.signingkeys.SigningKeysSettings;
import org.artifactory.descriptor.security.sso.CrowdSettings;
import org.artifactory.descriptor.sumologic.SumoLogicConfigDescriptor;
import org.jfrog.security.crypto.exception.CryptoRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Fred Simon
 */
@Component
public class EncryptConfigurationInterceptor implements ConfigurationChangesInterceptor {

    private final static Logger log = LoggerFactory.getLogger(EncryptConfigurationInterceptor.class);

    public static void decrypt(MutableCentralConfigDescriptor descriptor) {
        encryptOrDecrypt(descriptor, false);
    }

    private static void encryptOrDecrypt(MutableCentralConfigDescriptor descriptor, boolean encrypt) {
        for (RemoteRepoDescriptor remoteRepoDescriptor : descriptor.getRemoteRepositoriesMap().values()) {
            if (remoteRepoDescriptor instanceof HttpRepoDescriptor) {
                HttpRepoDescriptor httpRepoDescriptor = (HttpRepoDescriptor) remoteRepoDescriptor;
                String newPassword = getNewPassword(encrypt, httpRepoDescriptor.getPassword());
                if (StringUtils.isNotBlank(newPassword)) {
                    httpRepoDescriptor.setPassword(newPassword);
                }
            }
        }
        for (LocalReplicationDescriptor localReplicationDescriptor : descriptor.getLocalReplications()) {
            String newPassword = getNewPassword(encrypt, localReplicationDescriptor.getPassword());
            if (StringUtils.isNotBlank(newPassword)) {
                localReplicationDescriptor.setPassword(newPassword);
            }
        }
        SecurityDescriptor security = descriptor.getSecurity();
        for (LdapSetting ldapSetting : security.getLdapSettings()) {
            SearchPattern search = ldapSetting.getSearch();
            if (search != null) {
                String newPassword = getNewPassword(encrypt, search.getManagerPassword());
                if (StringUtils.isNotBlank(newPassword)) {
                    search.setManagerPassword(newPassword);
                }
            }
        }
        SigningKeysSettings signingKeysSettings = security.getSigningKeysSettings();
        if (signingKeysSettings != null) {
            String newPassword = getNewPassword(encrypt, signingKeysSettings.getPassphrase());
            if (StringUtils.isNotBlank(newPassword)) {
                signingKeysSettings.setPassphrase(newPassword);
            }
            newPassword = getNewPassword(encrypt, signingKeysSettings.getKeyStorePassword());
            if (StringUtils.isNotBlank(newPassword)) {
                signingKeysSettings.setKeyStorePassword(newPassword);
            }
        }
        CrowdSettings crowdSettings = security.getCrowdSettings();
        if (crowdSettings != null) {
            String newPassword = getNewPassword(encrypt, crowdSettings.getPassword());
            if (StringUtils.isNotBlank(newPassword)) {
                crowdSettings.setPassword(newPassword);
            }
        }
        List<ProxyDescriptor> proxies = descriptor.getProxies();
        if (proxies != null) {
            for (ProxyDescriptor proxy : proxies) {
                String newPassword = getNewPassword(encrypt, proxy.getPassword());
                if (StringUtils.isNotBlank(newPassword)) {
                    proxy.setPassword(newPassword);
                }
            }
        }
        MailServerDescriptor mailServer = descriptor.getMailServer();
        if (mailServer != null) {
            String newPassword = getNewPassword(encrypt, mailServer.getPassword());
            if (StringUtils.isNotBlank(newPassword)) {
                mailServer.setPassword(newPassword);
            }
        }
        BintrayConfigDescriptor bintraySettings = descriptor.getBintrayConfig();
        if (bintraySettings != null) {
            String newApiKey = getNewPassword(encrypt, bintraySettings.getApiKey());
            if (StringUtils.isNotBlank(newApiKey)) {
                bintraySettings.setApiKey(newApiKey);
            }
        }

        XrayDescriptor xrayDescriptor = descriptor.getXrayConfig();
        if (xrayDescriptor != null) {
            String newpassoword = getNewPassword(encrypt, xrayDescriptor.getPassword());
            if (StringUtils.isNotBlank(newpassoword)) {
                xrayDescriptor.setPassword(newpassoword);
            }
        }
        OAuthSettings oauthSettings = descriptor.getSecurity().getOauthSettings();
        if (oauthSettings != null) {
            List<OAuthProviderSettings> oauthProvidersSettings = oauthSettings.getOauthProvidersSettings();
            if (oauthProvidersSettings != null) {
                for (OAuthProviderSettings oauthProvidersSetting : oauthProvidersSettings) {
                    String secret = getNewPassword(encrypt, oauthProvidersSetting.getSecret());
                    if (StringUtils.isNotBlank(secret)) {
                        oauthProvidersSetting.setSecret(secret);
                    }
                }
            }
        }

        Map<String, BintrayApplicationConfig> bintrayApplicationConfigs = descriptor.getBintrayApplications();
        if (bintrayApplicationConfigs != null) {
            bintrayApplicationConfigs.values().forEach(appConfig -> {
                        String encryptedClientId = getNewPassword(encrypt, appConfig.getClientId());
                        String encryptedSecret = getNewPassword(encrypt, appConfig.getSecret());
                        String encryptedRefreshToken = getNewPassword(encrypt, appConfig.getRefreshToken());
                        appConfig.setClientId(encryptedClientId);
                        appConfig.setSecret(encryptedSecret);
                        appConfig.setRefreshToken(encryptedRefreshToken);
                    });
        }

        SumoLogicConfigDescriptor sumoLogicConfig = descriptor.getSumoLogicConfig();
        if (sumoLogicConfig != null) {
            String clientId = getNewPassword(encrypt, sumoLogicConfig.getClientId());
            if (StringUtils.isNotBlank(clientId)) {
                sumoLogicConfig.setClientId(clientId);
            }
            String secret = getNewPassword(encrypt, sumoLogicConfig.getSecret());
            if (StringUtils.isNotBlank(secret)) {
                sumoLogicConfig.setSecret(secret);
            }
        }

        AccessClientSettings accessClientSettings = security.getAccessClientSettings();
        if (accessClientSettings != null) {
            String adminToken = null;
            try {
                adminToken = getNewPassword(encrypt, accessClientSettings.getAdminToken());
            } catch (CryptoRuntimeException e) {
                log.debug("Failed to decrypt deprecated access admin token, no action required", e);
            }
            if (StringUtils.isNotBlank(adminToken)) {
                accessClientSettings.setAdminToken(adminToken);
            }
        }
    }

    private static String getNewPassword(boolean encrypt, String password) {
        if (StringUtils.isNotBlank(password)) {
            if (encrypt) {
                return CryptoHelper.encryptIfNeeded(ArtifactoryHome.get(), password);
            } else {
                return CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), password);
            }
        }
        return null;
    }

    @Override
    public void onBeforeSave(CentralConfigDescriptor newDescriptor) {
        if (newDescriptor instanceof MutableCentralConfigDescriptor && CryptoHelper.hasArtifactoryKey(ArtifactoryHome.get())) {
            // Find all sensitive data and encrypt them
            encrypt((MutableCentralConfigDescriptor) newDescriptor);
        }
    }

    private void encrypt(MutableCentralConfigDescriptor descriptor) {
        encryptOrDecrypt(descriptor, true);
    }

}
