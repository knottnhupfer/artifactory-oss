/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2019 JFrog Ltd.
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

package org.artifactory.rest.common.service;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.addon.CoreAddons;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.addon.smartrepo.SmartRepoAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.config.VersionInfo;
import org.artifactory.api.jackson.JacksonFactory;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.api.repo.ResearchService;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.rest.search.result.VersionRestResult;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.delegation.ContentSynchronisation;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.features.VersionFeature;
import org.artifactory.features.matrix.SmartRepoVersionFeatures;
import org.artifactory.repo.HttpRepositoryConfiguration;
import org.artifactory.repo.HttpRepositoryConfigurationImpl;
import org.artifactory.rest.exception.ForbiddenException;
import org.artifactory.util.HttpClientConfigurator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.List;

import static org.artifactory.request.ArtifactoryRequest.ARTIFACTORY_ORIGINATED;
import static org.artifactory.util.CollectionUtils.notNullOrEmpty;

/**
 * Service used to discover capabilities of another artifactory
 *
 * @author michaelp
 */
@SuppressWarnings("squid:S1161")
@Component
public class ResearchServiceImpl implements ResearchService {
    private static final Logger log = LoggerFactory.getLogger(ResearchServiceImpl.class);

    private static final String ARTIFACTORY_SYSTEM_VERSION_PATH = "/api/system/version";
    private static final String ARTIFACTORY_REPOSITORIES_PATH = "/api/repositories";
    private static final String ARTIFACTORY_APP_PATH = "/artifactory";
    private static final ObjectMapper mapper;

    private SmartRepoVersionFeatures smartRepoVersionFeatures;
    private CentralConfigService configService;
    private AddonsManager addonsManager;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Autowired
    public void setSmartRepoVersionFeatures(SmartRepoVersionFeatures smartRepoVersionFeatures) {
        this.smartRepoVersionFeatures = smartRepoVersionFeatures;
    }

    @Autowired
    public void setConfigService(CentralConfigService configService) {
        this.configService = configService;
    }

    @Autowired
    public void setAddonsManager(AddonsManager addonsManager) {
        this.addonsManager = addonsManager;
    }

    public ResearchResponse getSmartRepoCapabilities(HttpRepositoryConfigurationImpl configuration) {
        try (CloseableHttpClient client = createHttpClient(configuration)) {
            return getSmartRepoCapabilities(configuration.getUrl(), client);
        } catch (IOException ioe) {
            log.warn("Error creating research client to url {} : {}", configuration.getUrl(), ioe.getMessage());
            log.debug(ioe.getMessage(), ioe);
        }
        return ResearchResponse.notArtifactory();
    }

    public ResearchResponse getSmartRepoCapabilities(HttpRepoDescriptor httpRepoDescriptor) {
        try (CloseableHttpClient client = createHttpClient(httpRepoDescriptor)) {
            return getSmartRepoCapabilities(httpRepoDescriptor.getUrl(), client);
        } catch (IOException ioe) {
            log.warn("Error creating research client to url {} : {}", httpRepoDescriptor.getUrl(), ioe.getMessage());
            log.debug(ioe.getMessage(), ioe);
        }
        return ResearchResponse.notArtifactory();

    }

    public ResearchResponse getSmartRepoCapabilities(String url, CloseableHttpClient client) {
        URI uri = getUri(url);
        if (uri == null) {
            return ResearchResponse.notArtifactory();
        }
        return getArtifactoryCapabilities(client, uri, uri.getPath().startsWith(ARTIFACTORY_APP_PATH));
    }


    @Override
    public boolean isSmartRemote(HttpRepoDescriptor repoDescriptor) {
        CloseableHttpClient client = createHttpClient(repoDescriptor);
        URI uri = getUri(repoDescriptor.getUrl());
        if (uri == null) {
            return false;
        }
        boolean inArtifactoryContext = uri.getPath().startsWith(ARTIFACTORY_APP_PATH);
        ResearchResponse artifactoryCapabilities = getArtifactoryCapabilities(client, uri, inArtifactoryContext);
        if (artifactoryCapabilities != null) {
            List<VersionFeature> features = artifactoryCapabilities.getFeatures();
            return artifactoryCapabilities.isArtifactory() && artifactoryCapabilities.getVersion() != null
                    && notNullOrEmpty(features);
        }
        return false;
    }

    @Override
    public boolean isRepoConfiguredToSyncProperties(HttpRepoDescriptor repoDescriptor) {
        ContentSynchronisation contentSynchronisation = repoDescriptor.getContentSynchronisation();
        return repoDescriptor.isSynchronizeProperties() ||
                (contentSynchronisation != null
                        && contentSynchronisation.isEnabled()
                        && contentSynchronisation.getProperties().isEnabled()
                );

    }

    /**
     * Fetches artifactory capabilities (if target is artifactory)
     *
     * @param client               http client to use
     * @param uri                  remote uri to test against
     * @param inArtifactoryContext whether given app deployed
     *                             in /artifactory context or not
     * @return {@link VersionInfo} if target is artifactory or null
     */
    private ResearchResponse getArtifactoryCapabilities(CloseableHttpClient client, URI uri,
            boolean inArtifactoryContext) {
        String requestUrl = produceVersionUrl(uri, inArtifactoryContext);
        HttpGet getMethod = new HttpGet(requestUrl);
        addOriginatedHeader(getMethod);
        CloseableHttpResponse response = null;
        try {
            response = client.execute(getMethod);
            if (response != null) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String returnedInfo = EntityUtils.toString(response.getEntity());
                    if (!StringUtils.isBlank(returnedInfo)) {
                        VersionRestResult vrr = parseVersionRestResult(returnedInfo);
                        if (vrr != null && !StringUtils.isBlank(vrr.version) &&
                                validateLicense(vrr.license, getArtifactoryId(response))) {

                            Boolean isRealRepo = isRealRepo(client, uri, // make sure it not a virt repo
                                    PathUtils.getLastPathElement(uri.getPath()),
                                    inArtifactoryContext
                            );

                            if (isRealRepo == null) {
                                // we were unable to check repoType
                                // what may happen if config doesn't
                                // have credentials or has insufficient permissions
                                // (api requires it even if anonymous login is on)
                                log.debug("We were unable to check repoType, it may happen if config doesn't have " +
                                        "credentials or user permissions insufficient");
                            }
                            if (isRealRepo == null || isRealRepo.booleanValue()) {
                                log.debug(
                                        "Repo is artifactory repository (not virtual) and has supported version for SmartRepo");
                                VersionInfo versionInfo = vrr.toVersionInfo();
                                return ResearchResponse.artifactoryMeta(
                                        true,
                                        versionInfo,
                                        smartRepoVersionFeatures.getFeaturesByVersion(versionInfo)
                                );
                            } else {
                                log.debug("Virtual repository is not supported in this version of SmartRepo");
                            }
                        } else {
                            log.debug("Unsupported version: {}", vrr);
                        }
                    }
                } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    return ResearchResponse.artifactory();
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            log.debug("Checking remote artifactory version has failed: {}.", e);
        } finally {
            IOUtils.closeQuietly(response);
        }
        return ResearchResponse.notArtifactory();
    }

    /**
     * Fetches ARTIFACTORY_ID header from {@link CloseableHttpResponse}
     *
     * @return ArtifactoryId if present or null
     */
    @Nullable
    private String getArtifactoryId(CloseableHttpResponse response) {
        assert response != null : "HttpResponse cannot be empty";
        Header artifactoryIdHeader = response.getFirstHeader(ArtifactoryResponse.ARTIFACTORY_ID);
        if (artifactoryIdHeader != null && !StringUtils.isBlank(artifactoryIdHeader.getValue())) {
            return artifactoryIdHeader.getValue();
        }
        return null;
    }

    /**
     * Unmarshals VersionRestResult from string response
     *
     * @return {@link VersionRestResult}
     *
     * @throws IOException if conversion fails
     */
    private VersionRestResult parseVersionRestResult(String versionRestResult) throws IOException {
        try (JsonParser jsonParser = JacksonFactory.createJsonParser(versionRestResult.getBytes())) {
            return mapper.readValue(jsonParser, new TypeReference<VersionRestResult>() {});
        }
    }

    /**
     * add originated header to request
     *
     * @param request - http servlet request
     */
    private void addOriginatedHeader(HttpRequestBase request) {
        request.addHeader(ARTIFACTORY_ORIGINATED, addonsManager.addonByType(HaCommonAddon.class).getHostId());
    }

    /**
     * Produces url to be used against target host
     *
     * @param uri                  original URI
     * @param inArtifactoryContext if application resides under
     *                             /artifactory path
     * @return url to be used
     */
    private String produceVersionUrl(URI uri, boolean inArtifactoryContext) {
        return new StringBuilder()
                .append(uri.getScheme())
                .append("://")
                .append(uri.getHost())
                .append(uri.getPort() != -1 ?
                        ":" + uri.getPort()
                        :
                        ""
                )
                .append(getServiceName(uri, inArtifactoryContext))
                .append(ARTIFACTORY_SYSTEM_VERSION_PATH)
                .toString();
    }

    /**
     * Fetches service name from the original URI
     *
     * @return service name
     */
    private String getServiceName(URI uri, boolean inArtifactoryContext) {
        if (inArtifactoryContext) {
            return ARTIFACTORY_APP_PATH;
        }
        if (uri.getPath() != null) {
            String[] parts = uri.getPath().split("/");
            if (parts.length >= 2) {
                if (!(parts.length == 2 && uri.getPath().startsWith("/") && uri.getPath().endsWith("/"))) {
                    return "/" + PathUtils.getFirstPathElement(uri.getPath()); // .../serviceName/repoName
                }
            }
        }
        return ""; // repoName
    }

    private URI getUri(String url) {
        if (StringUtils.isBlank(url)) {
            log.debug("Url is a mandatory (query) parameter.");
            return null;
        }
        try {
            return URI.create(url);
        } catch (IllegalArgumentException e) {
            log.debug("Url is malformed.");
            return null;
        }
    }

    /**
     * Validates that remote Artifactory uses different license and it is PRO license
     * @param license remote license
     * @param artifactoryId remote artifactory id
     * @return true if both constraints are true otherwise false
     */
    private boolean validateLicense(String license, String artifactoryId) {
        CoreAddons coreAddons = addonsManager.addonByType(CoreAddons.class);

        boolean isDifferentLicense = coreAddons.validateTargetHasDifferentLicense(license, artifactoryId);
        boolean isOssLicensed = addonsManager.isOssLicensed(license);
        ArtifactoryRunningMode artifactoryRunningMode = addonsManager.getArtifactoryRunningMode();

        if (isDifferentLicense) {
            // if source has Edge license target should have either Edge or EntPlus license
            if ((addonsManager.isEdgeLicensed() || addonsManager.isEdgeMixedInCluster()) &&
                    !addonsManager.isEdgeOrEntPlusLicensed(license)) {
                log.warn("License validation against target repository has failed - only Edge or Enterprise Plus licenses " +
                        "are valid for artifactory Edge, SmartRepo capabilities won't be enabled");
                return false;
            }
            if (!isOssLicensed || artifactoryRunningMode.isJcrOrJcrAol()) {
                return true;
            }
            log.warn("License PRO validation against target repository has failed, " +
                    "SmartRepo capabilities won't be enabled");
        } else if (coreAddons.isAol()) {
            return true;
        } else {
            log.warn("License uniqueness validation against target repository " +
                    "has failed, SmartRepo capabilities won't be enabled");
        }
        return false;
    }

    /**
     * Checks if target repository is Virtual
     *
     * @return true if not virtual, otherwise false
     */
    private Boolean isRealRepo(CloseableHttpClient client, URI uri, String repoKey,
            boolean inArtifactoryContext) {

        assert client != null : "HttpClient cannot be empty";

        CloseableHttpResponse response = null;
        String requestUrl = produceRepoInfoUrl(uri, repoKey, inArtifactoryContext);
        HttpGet getMethod = new HttpGet(requestUrl);
        addOriginatedHeader(getMethod);
        try {
            response = client.execute(getMethod);
            if (response != null) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpRepositoryConfiguration httpRepositoryConfiguration =
                            JacksonReader.streamAsClass(
                                    response.getEntity().getContent(),
                                    HttpRepositoryConfigurationImpl.class
                            );
                    if (httpRepositoryConfiguration != null) {
                        if (httpRepositoryConfiguration.getType().equals("virtual")) {
                            log.debug("Found virtual repository '{}'", repoKey);
                            return false;
                        } else {
                            log.debug("Found real repository '{}'", repoKey);
                            return true;
                        }
                    } else {
                        log.debug("Cannot fetch '{}' metadata, no response received", repoKey);
                    }
                } else {
                    log.debug("Cannot fetch '{}' metadata, cause: ", response);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            log.debug(
                    "Checking remote artifactory type has failed: {}.",
                    e.getMessage()
            );
        } finally {
            IOUtils.closeQuietly(response);
        }
        return null;
    }

    /**
     * Generates repository info URL
     *
     * @param uri default target URI
     * @return url
     */
    private String produceRepoInfoUrl(URI uri, String repoKey, boolean inArtifactoryContext) {
        return new StringBuilder()
                .append(uri.getScheme())
                .append("://")
                .append(uri.getHost())
                .append(uri.getPort() != -1 ?
                        ":" + uri.getPort()
                        :
                        ""
                )
                .append(getServiceName(uri, inArtifactoryContext))
                .append(ARTIFACTORY_REPOSITORIES_PATH)
                .append("/")
                .append(repoKey)
                .toString();
    }

    private CloseableHttpClient createHttpClient(HttpRepoDescriptor repoDescriptor) {
        ProxyDescriptor proxyDescriptor = repoDescriptor.getProxy();
        HttpClientConfigurator configurator = new HttpClientConfigurator();
        configurator.hostFromUrl(repoDescriptor.getUrl())
                .connectionTimeout(repoDescriptor.getSocketTimeoutMillis())
                .socketTimeout(repoDescriptor.getSocketTimeoutMillis())
                .staleCheckingEnabled(true)
                .retry(0, false)
                .localAddress(repoDescriptor.getLocalAddress())
                .authentication(
                        repoDescriptor.getUsername(),
                        CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), repoDescriptor.getPassword()),
                        repoDescriptor.isAllowAnyHostAuth())
                .enableCookieManagement(repoDescriptor.isEnableCookieManagement());
        return configurator.proxy(proxyDescriptor).build();
    }

    private CloseableHttpClient createHttpClient(HttpRepositoryConfigurationImpl configuration) {
        ProxyDescriptor proxyDescriptor = configService.getDescriptor().getProxy(configuration.getProxy());
        HttpClientConfigurator configurator = new HttpClientConfigurator();
        configurator.hostFromUrl(configuration.getUrl())
                .connectionTimeout(configuration.getSocketTimeoutMillis())
                .socketTimeout(configuration.getSocketTimeoutMillis())
                .staleCheckingEnabled(true)
                .retry(0, false)
                .localAddress(configuration.getLocalAddress())
                .authentication(
                        configuration.getUsername(),
                        CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), configuration.getPassword()),
                        configuration.isAllowAnyHostAuth())
                .enableCookieManagement(configuration.isEnableCookieManagement());
        return configurator.proxy(proxyDescriptor).build();
    }

    /**
     * ResearchResponse
     */
    @XStreamAlias("researchResponse")
    public static class ResearchResponse implements Serializable {

        private final boolean isArtifactory;
        private final VersionInfo versionInfo;
        private final List<VersionFeature> features;

        /**
         * Produces response with artifactory=false
         *
         * @return {@link ResearchResponse}
         */
        private ResearchResponse() {
            this.isArtifactory = false;
            this.versionInfo = null;
            this.features = Lists.newLinkedList();
        }

        /**
         * Produces response with metadata describing remote
         * artifactory instance
         *
         * @return {@link ResearchResponse}
         */
        private ResearchResponse(boolean isArtifactory, VersionInfo versionInfo,
                List<VersionFeature> features) {
            this.isArtifactory = isArtifactory;
            this.versionInfo = versionInfo;
            this.features = features;
        }

        /**
         * Produces response with artifactory=false
         *
         * @return {@link ResearchResponse}
         */
        public static ResearchResponse notArtifactory() {
            return new ResearchResponse();
        }

        /**
         * Produces response with metadata describing remote
         * artifactory instance
         *
         * @return {@link ResearchResponse}
         */
        public static ResearchResponse artifactoryMeta(boolean isArtifactory, VersionInfo versionInfo,
                List<VersionFeature> features) {
            return new ResearchResponse(isArtifactory, versionInfo, features);
        }

        /**
         * Produces response without any metadata, but implies artifactory=true
         *
         * @return {@link ResearchResponse}
         */
        public static ResearchResponse artifactory() {
            return new ResearchResponse(true, null, Lists.newLinkedList());
        }

        /**
         * @return definition of remote host being artifactory or not
         */
        public boolean isArtifactory() {
            return isArtifactory;
        }

        /**
         * @return remote artifactory version
         */
        public VersionInfo getVersion() {
            return versionInfo;
        }

        /**
         * @return available features for remote artifactory
         * (based on its version)
         */
        public List<VersionFeature> getFeatures() {
            return features;
        }
    }
}
