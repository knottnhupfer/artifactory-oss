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

package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.addon.webstart.KeyStoreNotFoundException;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteNetworkRepositoryConfigModel;
import org.artifactory.util.HttpClientConfigurator;
import org.jfrog.client.util.KeyStoreProvider;
import org.jfrog.client.util.KeyStoreProviderException;
import org.jfrog.client.util.KeyStoreProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;

/**
 * @author Aviad Shikloshi
 */
public class RemoteRepositoryProvider {
    private static final Logger log = LoggerFactory.getLogger(RemoteRepositoryProvider.class);

    protected static final int RETRY_COUNT = 1;
    protected static final int DEFAULT_TIMEOUT = 15000;

    public static CloseableHttpClient getRemoteRepoHttpClient(String remoteUrl,
            RemoteNetworkRepositoryConfigModel networkConfig, boolean useClientCertificate) {
        // In case network model was not sent in the request we are using the default values
        if (networkConfig == null) {
            networkConfig = new RemoteNetworkRepositoryConfigModel();
        }
        CentralConfigService configService = ContextHelper.get().getCentralConfig();
        ProxyDescriptor proxyDescriptor = configService.getDescriptor().getProxy(networkConfig.getProxy());
        int socketTimeout =
                networkConfig.getSocketTimeout() == null ? DEFAULT_TIMEOUT : networkConfig.getSocketTimeout();
        HttpClientConfigurator configurator = new HttpClientConfigurator();
        configurator
                .hostFromUrl(remoteUrl)
                .connectionTimeout(socketTimeout)
                .socketTimeout(socketTimeout)
                .staleCheckingEnabled(true)
                .retry(RETRY_COUNT, false)
                .localAddress(networkConfig.getLocalAddress())
                .authentication(networkConfig.getUsername(),
                        CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), networkConfig.getPassword()),
                        networkConfig.getLenientHostAuth() != null)
                .enableCookieManagement(networkConfig.getCookieManagement() != null);
        if (useClientCertificate) {
            String certificateToUse = networkConfig.getSelectedInstalledCertificate();
            if (StringUtils.isNotBlank(certificateToUse)) {
                KeyStoreProvider keyStoreProvider = getKeyStoreProvider();
                if (keyStoreProvider != null) {
                    configurator.clientCertKeyStoreProvider(keyStoreProvider);
                    configurator.clientCertAlias(ArtifactWebstartAddon.SSL_CERT_ALIAS_PREFIX + certificateToUse);
                }
            }
        }
        return configurator.proxy(proxyDescriptor).build();
    }

    private static KeyStoreProvider getKeyStoreProvider() {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        ArtifactWebstartAddon webstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
        KeyStore existingKeyStore = null;
        try {
            existingKeyStore = webstartAddon.getExistingKeyStore();
        } catch (KeyStoreNotFoundException e) {
            return null;
        }
        String keystorePass = webstartAddon.getKeystorePassword();
        if (existingKeyStore != null) {
            try {
                return KeyStoreProviderFactory.getProvider(existingKeyStore, keystorePass);
            } catch (KeyStoreProviderException e) {
                log.warn("Cannot set client certificate on repo. {}", e.getMessage());
                log.debug("Cannot set client certificate on repo.", e);
            }
        }
        return null;
    }
}
