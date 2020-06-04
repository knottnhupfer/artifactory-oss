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

package org.artifactory.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.*;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.mbean.MBeanRegistrationService;
import org.artifactory.repo.http.IdleConnectionMonitorService;
import org.artifactory.repo.http.mbean.HTTPConnectionPool;
import org.artifactory.util.bearer.RepoSpecificBearerSchemeFactory;
import org.jfrog.client.http.CloseableHttpClientDecorator;
import org.jfrog.client.http.CloseableObserver;
import org.jfrog.client.http.HttpBuilderBase;
import org.jfrog.client.http.auth.PreemptiveAuthInterceptor;
import org.jfrog.client.http.auth.ProxyPreemptiveAuthInterceptor;
import org.jfrog.client.http.model.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Artifactory builder extension for the for the common HTTP client builder.
 *
 * @author Yossi Shaul
 */
public class HttpClientConfigurator extends HttpBuilderBase<HttpClientConfigurator> {
    private static final Logger log = LoggerFactory.getLogger(HttpClientConfigurator.class);

    private String repoKey;
    private String jmxName;

    public HttpClientConfigurator() {
        super();
        userAgent(HttpUtils.getArtifactoryUserAgent());
        handleGzipResponse(ConstantValues.httpAcceptEncodingGzip.getBoolean());
        connectionPoolTTL(ConstantValues.httpConnectionPoolTimeToLive.getInt());
        config.setMaxRedirects(20);
        config.setCircularRedirectsAllowed(true);
        maxConnectionsTotal = ConstantValues.httpClientMaxTotalConnections.getInt();
        maxConnectionsPerRoute = ConstantValues.httpClientMaxConnectionsPerRoute.getInt();
    }

    @Override
    public CloseableHttpClient build(boolean withParams) {
        // TODO: [by fsi] This all system looks messy. Too many fields and connection between elements.
        CloseableHttpClientDecorator client = (CloseableHttpClientDecorator) super.build(withParams);
        IdleConnectionMonitorService idleConnectionMonitorService =
                ContextHelper.get().beanForType(IdleConnectionMonitorService.class);
        idleConnectionMonitorService.add(client.getId(), client.getClientConnectionManager());
        client.registerCloseableObserver((CloseableObserver) idleConnectionMonitorService);
        if (StringUtils.isNotBlank(jmxName)) {
            try {
                ContextHelper.get().beanForType(MBeanRegistrationService.class)
                        .register(new HTTPConnectionPool(client.getClientConnectionManager()), "HTTPConnectionPool",
                                jmxName);
            } catch (Exception e) {
                String err = "Failed register HTTPConnectionPool mbean for " + jmxName + ": " + e.getMessage();
                log.error(err);
                log.debug(err, e);
            }
        }
        return client;
    }

    /**
     * backward compatibility - use socketTimeout(int) instead
     */
    @Deprecated
    public HttpClientConfigurator soTimeout(int soTimeout) {
        return socketTimeout(soTimeout);
    }


    /**
     * backward compatibility - use build() instead
     */
    public CloseableHttpClient getClient() {
        return build();
    }

    public HttpClientConfigurator registerMBean(String jmxName) {
        this.jmxName = jmxName;
        return this;
    }

    @Override
    protected PoolingHttpClientConnectionManager configConnectionManager() {
        additionalConfigByAuthScheme();
        return super.configConnectionManager();
    }

    @Override
    public boolean isCookieSupportEnabled() {
        return cookieSupportEnabled || ConstantValues.enableCookieManagement.getBoolean();
    }

    /**
     * Disable the automatic gzip compression on read.
     * Once disabled cannot be activated.
     */
    public HttpClientConfigurator handleGzipResponse(boolean handleGzipResponse) {
        if (!handleGzipResponse) {
            disableGzipResponse();
        }
        return this;
    }

    public HttpClientConfigurator enableTokenAuthentication(boolean enableTokenAuthentication, String repoKey,
            @Nullable HttpRequestInterceptor requestInterceptor) {
        if (enableTokenAuthentication) {
            if (defaultHost == null || StringUtils.isBlank(defaultHost.getHostName())) {
                throw new IllegalStateException("Cannot configure authentication when host is not set.");
            }
            this.repoKey = repoKey;
            config.setTargetPreferredAuthSchemes(Collections.singletonList("Bearer"));
            // The repository key is passed to the Bearer to reuse it's http client
            Registry<AuthSchemeProvider> bearerRegistry = RegistryBuilder.<AuthSchemeProvider>create()
                    .register("Bearer", new RepoSpecificBearerSchemeFactory(repoKey))
                    .build();
            builder.setDefaultAuthSchemeRegistry(bearerRegistry);
            if (requestInterceptor != null) {
                addRequestInterceptor(requestInterceptor);
            }
            chosenAuthScheme = JFrogAuthScheme.BEARER;
        }
        return this;
    }

    public HttpClientConfigurator proxy(@Nullable ProxyDescriptor proxyDescriptor) {
        if (proxyDescriptor != null) {
            ProxyConfig proxyConfig = proxyDescriptor.toProxyConfig();
            if (isNotBlank(proxyConfig.getPassword())) {
                proxyConfig.setPassword(CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), proxyConfig.getPassword()));
            }
            proxy(proxyConfig);
        }
        return this;
    }

    //TODO: [by YS] remove this static method by reusing the builder in the caller method
    public static void configureProxy(ProxyDescriptor proxy, HttpClientBuilder clientBuilder,
            RequestConfig.Builder requestConfig, BasicCredentialsProvider credsProvider) {
        requestConfig.setProxy(new HttpHost(proxy.getHost(), proxy.getPort()));
        if (StringUtils.isNotBlank(proxy.getUsername())) {
            Credentials creds = null;
            if (proxy.getDomain() == null) {
                creds = new UsernamePasswordCredentials(proxy.getUsername(),
                        CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), proxy.getPassword()));
                //This will demote the NTLM authentication scheme so that the proxy won't barf
                //when we try to give it traditional credentials. If the proxy doesn't do NTLM
                //then this won't hurt it (jcej at tragus dot org)
                List<String> authPrefs = Arrays.asList(AuthSchemes.DIGEST, AuthSchemes.BASIC, AuthSchemes.NTLM);
                requestConfig.setProxyPreferredAuthSchemes(authPrefs);
                // preemptive proxy authentication
                clientBuilder.addInterceptorFirst(new ProxyPreemptiveAuthInterceptor());
            } else {
                try {
                    String ntHost =
                            StringUtils.isBlank(proxy.getNtHost()) ? InetAddress.getLocalHost().getHostName() :
                                    proxy.getNtHost();
                    creds = new NTCredentials(proxy.getUsername(),
                            CryptoHelper.decryptIfNeeded(
                                    ArtifactoryHome.get(), proxy.getPassword()), ntHost, proxy.getDomain());
                } catch (UnknownHostException e) {
                    log.error("Failed to determine required local hostname for NTLM credentials.", e);
                }
            }
            if (creds != null) {
                credsProvider.setCredentials(
                        new AuthScope(proxy.getHost(), proxy.getPort(), AuthScope.ANY_REALM), creds);
                if (proxy.getRedirectedToHostsList() != null) {
                    for (String hostName : proxy.getRedirectedToHostsList()) {
                        credsProvider.setCredentials(
                                new AuthScope(hostName, AuthScope.ANY_PORT, AuthScope.ANY_REALM), creds);
                    }
                }
            }
        }
    }

    public RequestConfig.Builder getConfig() {
        return this.config;
    }

    /**
     * Sets required configuration based on the final chosen config for this client so that configurations
     * don't interfere with each other based on when they were created in the chain (i.e. auth before token etc.)
     */
    protected void additionalConfigByAuthScheme() {
        switch (chosenAuthScheme) {
            case BASIC:
                builder.addInterceptorFirst(new PreemptiveAuthInterceptor());
                break;
            case BEARER:
                if (shouldConfigureBearerDummyCredentials()) {
                    // We need dummy credentials hack to enforce httpClient behavior, otherwise we won't respond to a
                    //challenge properly... Dummy:dummy is the specification for forcing token authentication
                    credsProvider.setCredentials(
                            new AuthScope(defaultHost.getHostName(), AuthScope.ANY_PORT, AuthScope.ANY_REALM),
                            new UsernamePasswordCredentials("dummy", "dummy"));
                } else {
                    //Valid credentials exist for target host - set basic auth preference and register additional scheme
                    //so we can respond to basic challenges from target as required
                    List<String> authPrefs = Arrays.asList("Bearer", AuthSchemes.BASIC);
                    config.setTargetPreferredAuthSchemes(authPrefs);
                    Registry<AuthSchemeProvider> bearerRegistry = RegistryBuilder.<AuthSchemeProvider>create()
                            .register("Bearer", new RepoSpecificBearerSchemeFactory(repoKey))
                            .register(AuthSchemes.BASIC, new BasicSchemeFactory())
                            .build();
                    builder.setDefaultAuthSchemeRegistry(bearerRegistry);
                }
                break;
        }
    }

    /**
     * @return false if credentials were configured for this client's host (or any host if lenient) -> also
     * verifies the credential set is not the one configured for the proxy (proxy credentials are not considered
     * host credentials), true if dummy credentials should be configured for Bearer auth
     */
    private boolean shouldConfigureBearerDummyCredentials() {
        boolean shouldSetDummy = false;
        Credentials hostCreds = credsProvider.getCredentials(new AuthScope(defaultHost.getHostName(), AuthScope.ANY_PORT, AuthScope.ANY_REALM));
        Credentials anyHostCreds = credsProvider.getCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM));
        Credentials proxyCreds = null;
        if (StringUtils.isNotBlank(proxyHost)) {
            proxyCreds = credsProvider.getCredentials(new AuthScope(proxyHost, AuthScope.ANY_PORT, AuthScope.ANY_REALM));
        }

        //Any host allowed - make sure credentials were set and that the credsProvider didn't just return
        //the proxy's credentials for the ANY_HOST scope.
        if (allowAnyHostAuth && anyHostCreds != null
                && (proxyCreds == null || (!proxyCreds.getUserPrincipal().equals(anyHostCreds.getUserPrincipal())))) {
            shouldSetDummy = true;
        } else if (hostCreds == null) {
            shouldSetDummy = true;
        }
        return shouldSetDummy;
    }

}
