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

package org.artifactory.metrics.services;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.addon.CoreAddons;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.callhome.CallHomeRequest;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.jackson.JacksonWriter;
import org.artifactory.api.repo.Async;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.api.rest.subscription.Subscription;
import org.artifactory.descriptor.subscription.SubscriptionConfig;
import org.artifactory.metrics.providers.features.CallHomeFeature;
import org.artifactory.security.access.AccessService;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.DbMetaData;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.servers.service.ArtifactoryServersCommonService;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.HttpClientConfigurator;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.artifactory.common.ConstantValues.artifactoryVersion;

/**
 * @author Michael Pasternak
 */
@Service
public class CallHomeService {

    private static final Logger log = LoggerFactory.getLogger(CallHomeService.class);
    private static final String PARAM_OS_ARCH = "os.arch";
    private static final String PARAM_OS_NAME = "os.name";
    private static final String PARAM_JAVA_VERSION = "java.version";
    private static final String LOCALHOST = "localhost";
    private static int ATTEMPTS_BEFORE_OFFLINE = 3;
    public static final String CALL_HOME_BINTRAY_CONTEXT_PATH = "/products/jfrog/artifactory/stats/usage";

    private AtomicBoolean isOffline = new AtomicBoolean(false);
    private int failedAttempts = 0;

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private List<CallHomeFeature> callHomeFeatures = Lists.newArrayList();

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private ArtifactoryServersCommonService serversService;

    @Autowired
    private AccessService accessService;

    @Autowired
    private DbService dbService;

    public CallHomeService() {
    }

    /**
     * Sends post message with usage info to bintray
     */
    @Async
    public void callHome() {
        if (ConstantValues.versionQueryEnabled.getBoolean() && !configService.getDescriptor().isOfflineMode()) {
            try (CloseableHttpClient client = createHttpClient()) {
                HttpPost postMethod = new HttpPost(getCallHomeUrl());
                postMethod.setEntity(callHomeEntity());
                log.debug("Calling home...");
                client.execute(postMethod);
                clearData();
                failedAttempts = 0;
                isOffline.set(false);
            } catch (Exception e) {
                log.error("Failed calling home: " + e.getMessage(), e);
                if (isOffline.get()) {
                    clearData();
                } else if (++failedAttempts >= ATTEMPTS_BEFORE_OFFLINE) {
                    isOffline.getAndSet(true);
                    clearData();
                }
            }
        }
    }

    private String getCallHomeUrl() {
        String urlOverride = ConstantValues.callHomeOverrideUrl.getString();
        return StringUtils.isNotBlank(urlOverride) ? urlOverride :
                ConstantValues.bintrayApiUrl.getString() + CALL_HOME_BINTRAY_CONTEXT_PATH;
    }

    public AtomicBoolean getIsOffline() {
        return isOffline;
    }

    /**
     * @return {@link CloseableHttpClient}
     */
    private CloseableHttpClient createHttpClient() {
        ProxyDescriptor proxy = InternalContextHelper.get().getCentralConfig().getDescriptor().getDefaultProxy();
        return new HttpClientConfigurator()
                .proxy(proxy)
                .socketTimeout(15000)
                .connectionTimeout(1500)
                .noRetry()
                .build();
    }

    /**
     * Produces callHomeEntity
     *
     * @return {@link HttpEntity}
     *
     * @throws IOException on serialization errors
     */
    private HttpEntity callHomeEntity() throws IOException {
        CallHomeRequest request = new CallHomeRequest();

        addInstanceAndEnvInfo(request);
        addFeatures(request);
        return serializeToStringEntity(request);
    }

    /**
     * Serializes {@link CallHomeRequest} to {@link org.apache.http.entity.StringEntity}
     *
     * @param request {@link CallHomeRequest}
     * @return {@link org.apache.http.entity.StringEntity}
     *
     * @throws IOException happens if serialization fails
     */
    private StringEntity serializeToStringEntity(CallHomeRequest request) throws IOException {
        String serialized = JacksonWriter.serialize(request, true);
        return new StringEntity(serialized, ContentType.APPLICATION_JSON);
    }

    /**
     * Collects features metadata  {@see RTFACT-8412}
     *
     * @param request that holds the entire callhome content
     */
    private void addFeatures(CallHomeRequest request) {
        callHomeFeatures.stream()
                .map(CallHomeFeature::getFeature)
                .forEach(request::addCallHomeFeature);
    }

    /**
     * Optionally clears any data kept in memory for the job execution
     */
    private void clearData() {
        callHomeFeatures.forEach((CallHomeFeature::clearData));
    }

    /**
     * Collect environment and instance information
     *
     * @param request that should the entire call home information
     */
    private void addInstanceAndEnvInfo(CallHomeRequest request) {
        request.version = artifactoryVersion.getString();

        String license = getLicenseType();
        if (license != null && license.equals("aol")) {
            boolean isDedicated = ArtifactoryHome.get().getArtifactoryProperties()
                    .getBooleanProperty(ConstantValues.aolDedicatedServer);
            license = isDedicated ? license + " dedicated" : license;
        }
        request.licenseType = license;

        request.licenseOEM = addonsManager.isPartnerLicense() ? "VMware" : null;
        Date licenseValidUntil = addonsManager.getLicenseValidUntil();
        if (licenseValidUntil != null) {
            request.licenseExpiration = ISODateTimeFormat.dateTime().print(new DateTime(licenseValidUntil));
        }
        request.setDist(System.getProperty("artdist"));
        populateSetup(request);
        request.environment.hostId = addonsManager.addonByType(HaCommonAddon.class).getHostId();
        request.environment.serviceId = accessService.getArtifactoryServiceId().getFormattedName();
        request.environment.licenseHash = addonsManager.getLicenseKeyHash(false);
        request.environment.attributes.osName = System.getProperty(PARAM_OS_NAME);
        request.environment.attributes.osArch = System.getProperty(PARAM_OS_ARCH);
        request.environment.attributes.javaVersion = System.getProperty(PARAM_JAVA_VERSION);
        setDbInfo(request);
        SubscriptionConfig subscriptionConfig = configService.getMutableDescriptor().getSubscriptionConfig();
        if (subscriptionConfig != null && CollectionUtils.notNullOrEmpty(subscriptionConfig.getEmails())) {
            request.subscription = new Subscription(subscriptionConfig.getEmails());
        }
    }

    private void setDbInfo(CallHomeRequest request) {
        try {
            DbMetaData metaData = dbService.getDbMetaData();
            request.dbType = dbService.getDatabaseType().toString();
            request.dbVersion = metaData.getProductVersion();
        } catch (StorageException e) {
            log.warn("Can not retrieve database and driver name / version", e);
        }
    }

    private String getAccountName(String aolAccountURL) {
        if (aolAccountURL != null) {
            try {
                URL url = new URL(aolAccountURL);
                String host = url.getHost();
                if (!LOCALHOST.equalsIgnoreCase(host)) {
                    return host.split("\\.")[0];
                }
            } catch (Exception e) {
                String msg = String.format("Caught exception while trying to parse aol Account URL: %s", aolAccountURL);
                log.debug(msg, e);
                return "N/A";
            }
        }
        return "N/A";
    }

    private void populateSetup(CallHomeRequest request) {
        String aolAccountURL = ArtifactoryHome.get().getArtifactoryProperties()
                .getProperty(ConstantValues.artifatoryServiceName);
        request.accountName = getAccountName(aolAccountURL);
        int numberOfNodes;
        if (isAolInstance()) {
            boolean isDedicated = ArtifactoryHome.get().getArtifactoryProperties()
                    .getBooleanProperty(ConstantValues.aolDedicatedServer);
            request.setup = isDedicated ? "AOL-Dedicated" : "AOL";
        } else if (addonsManager.getArtifactoryRunningMode() == ArtifactoryRunningMode.HA &&
                (numberOfNodes = getNumberOfNodes()) > 1) {
            request.setup = "cluster";
            request.numberOfNodes = numberOfNodes;
            return;
        }
        request.setup = "standalone";
    }

    private int getNumberOfNodes() {
        return serversService.getAllArtifactoryServers().size();
    }


    private String getLicenseType() {
        if (addonsManager instanceof OssAddonsManager) {
            return ((OssAddonsManager) addonsManager).getProAndAolLicenseDetails().getType().toLowerCase();
        }
        if (addonsManager.addonByType(CoreAddons.class).isAol()) {
            return "aol";
        }
        String type = addonsManager.getProAndAolLicenseDetails().getType();
        switch (type) {
            case "Trial":
            case "Edge":
            case "Edge Trial":
            case "Enterprise Plus":
            case "Enterprise Plus Trial":
                return type.toLowerCase();
            case "Commercial":
                return "pro";
        }
        if (addonsManager.isHaLicensed()) {
            return "ent";
        }
        return null;
    }

    private Boolean isAolInstance() {
        return addonsManager.addonByType(CoreAddons.class).isAol();
    }
}
