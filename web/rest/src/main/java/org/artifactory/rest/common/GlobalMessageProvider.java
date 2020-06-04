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

package org.artifactory.rest.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.FooterMessage;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.storage.StorageQuotaInfo;
import org.artifactory.common.ConstantValues;
import org.artifactory.security.access.AccessService;
import org.artifactory.storage.StorageService;
import org.artifactory.storage.db.conversion.version.v213.V213ConversionFailFunction;
import org.artifactory.storage.fs.service.ConfigsService;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.Files;
import org.jfrog.common.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerResponseContext;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static org.artifactory.addon.FooterMessage.FooterMessageVisibility.admin;
import static org.artifactory.addon.FooterMessage.FooterMessageVisibility.isVisible;

/**
 * @author Gidi Shabat
 */
class GlobalMessageProvider {
    private static final Logger log = LoggerFactory.getLogger(GlobalMessageProvider.class);

    static final String UI_MESSAGES_TAG = "Artifactory-UI-messages";

    private long lastUpdateTime = 0;
    private List<FooterMessage> cache = Lists.newArrayList();
    private volatile ReentrantLock lock = new ReentrantLock();
    private String footerMessageRawJson = null;
    private long lastFooterMessageFileChecked;

    void decorateWithGlobalMessages(ContainerResponseContext response, AddonsManager addonsManager, StorageService storageService,
            AuthorizationService authenticationService, ConfigsService configsService, AccessService accessService) {
        try {
            boolean admin = authenticationService.isAdmin();
            boolean notAnonymous = !authenticationService.isAnonymous();
            // Try to update the cache if needed
            triggerCacheUpdateProcessIfNeeded(addonsManager, storageService, configsService, accessService);
            // update response header with message in cache
            List<FooterMessage> footerMessages = cache.stream()
                    .filter(message -> isVisible(message.getVisibility(), admin, notAnonymous))
                    .collect(Collectors.toList());
            String messagesRawJson = JsonUtils.getInstance().valueToString(footerMessages);
            response.getHeaders().add(UI_MESSAGES_TAG, messagesRawJson);
        } catch (Exception e) {
            log.error("Failed to attache global message to response header", e);
        }
    }

    private void triggerCacheUpdateProcessIfNeeded(AddonsManager addonsManager, StorageService storageService,
            ConfigsService configsService, AccessService accessService) {
        long currentTime = System.currentTimeMillis();
        // update the cache every 10 seconds
        int maxCacheAge = ConstantValues.globalMessageCacheAgeSecs.getInt();
        if (currentTime - lastUpdateTime > TimeUnit.SECONDS.toMillis(maxCacheAge)) {
            // Only one thread is allowed to update the cache
            // all the other requests will use the old cache value
            boolean acquireLock = lock.tryLock();
            try {
                if (acquireLock) {
                    List<FooterMessage> list = Lists.newArrayList();
                    decorateHeadersWithLicenseNotInstalled(list, addonsManager);
                    decorateHeadersWithUnsupportedUrls(list, addonsManager);
                    decorateHeaderWithQuotaMessage(list, storageService);
                    decorateHeadersWithXrayStatus(list, addonsManager);
                    decorateWithCustomMessage(list);
                    decorateWithPsqlIndexMismatch(list, configsService);
                    decorateNeedToChangeAdminPassword(list, accessService);
                    decorateJcrAndEulaNotSigned(list, addonsManager);
                    // update the cache and the last update time
                    lastUpdateTime = currentTime;
                    cache = list;
                }
            } finally {
                if (acquireLock) {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * when using PostgreSQL, in some scenarios (i.e. upgrade of existing env), it might be that we have large
     * properties in the node_props that causing a conversion of node_props index to fail due to props too large. This
     * method should decorate and notify admin user that a manual operation of index trim is required, and re-run of
     * the index conversion is required as well.
     */
    private void decorateWithPsqlIndexMismatch(List<FooterMessage> list, ConfigsService configsService) {
        if (hasPostgresIncompatibleIndex(configsService)) {
            String message =
                    "An incompatible index has been found for the Artifactory ‘node_props’ database table. This may " +
                            "be caused by certain property values that exceed the allowed length limit, causing " +
                            "creation of the database index to fail during a version upgrade. " + newLine() +
                            "For instructions on how to fix this error " + psqlIndexMismatchLink();
            FooterMessage psqlIndexWarn = FooterMessage
                    .createError(message, FooterMessage.FooterMessageVisibility.admin);
            list.add(psqlIndexWarn);
        }
    }

    private void decorateNeedToChangeAdminPassword(List<FooterMessage> list, AccessService accessService) {
        if (accessService.isAdminUsingOldDefaultPassword()) {
            String message =
                    "<b>Admin Notice</b>: " +
                    "Your Artifactory instance is set up with default credentials to connect to the security datastore via localhost. " +
                            "It is highly recommended to change the default password. See details " + changeAccessAdminPassword() + ".";
            FooterMessage footerMessage = FooterMessage
                    .createDismissibleError(message, FooterMessage.FooterMessageVisibility.admin);
            list.add(footerMessage);
        }
    }

    private boolean hasPostgresIncompatibleIndex(ConfigsService configsService) {
        return configsService.hasConfig(V213ConversionFailFunction.PSQL_NODE_PROPS_INDEX_MISSING_MARKER);
    }

    private String newLine() {
        return "<br/>";
    }

    private String psqlIndexMismatchLink() {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href='https://www.jfrog.com/confluence/display/RTF/Troubleshooting#Troubleshooting-RecoveringfromError:AnincompatibleindexhasbeenfoundfortheArtifactory%E2%80%98node_props%E2%80%99databasetable'>").append("click here").append("</a>");
        return sb.toString();
    }

    private String changeAccessAdminPassword() {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href='https://jfrog.com/knowledge-base/How-to-change-the-default-password-for-access-admin-user' target='_blank'>").append("here").append("</a>");
        return sb.toString();
    }

    private void decorateHeadersWithLicenseNotInstalled(List<FooterMessage> list, AddonsManager addonsManager) {
        FooterMessage licenseMessage = addonsManager.getLicenseFooterMessage();
        if (licenseMessage != null) {
            list.add(licenseMessage);
        }
    }

    private void decorateJcrAndEulaNotSigned(List<FooterMessage> list, AddonsManager addonsManager) {
        FooterMessage licenseMessage = addonsManager.getEULAFooterMessage();
        if (licenseMessage != null) {
            list.add(licenseMessage);
        }
    }

    private void decorateHeadersWithUnsupportedUrls(List<FooterMessage> list, AddonsManager addonsManager) {
        FooterMessage licenseMessage = addonsManager.getRemoteRepUnsupportedUrls();
        if (licenseMessage != null) {
            list.add(licenseMessage);
        }
    }
    private void decorateHeaderWithQuotaMessage(List<FooterMessage> list, StorageService storageService) {
        StorageQuotaInfo storageQuotaInfo = storageService.getStorageQuotaInfo(0);
        if (storageQuotaInfo != null) {
            boolean limitReached = storageQuotaInfo.isLimitReached();
            boolean warningReached = storageQuotaInfo.isWarningLimitReached();
            if (limitReached) {
                String errorMessage = storageQuotaInfo.getErrorMessage();
                list.add(FooterMessage.createError(errorMessage, admin));
            } else if (warningReached) {
                String warningMessage = storageQuotaInfo.getWarningMessage();
                list.add(FooterMessage.createWarning(warningMessage, admin));
            }
        }
    }

    private void decorateHeadersWithXrayStatus(List<FooterMessage> list, AddonsManager addonsManager) {
        XrayAddon xrayAddon = addonsManager.addonByType(XrayAddon.class);
        if (!xrayAddon.isXrayEnabled()) {
            return;
        }
        if (!xrayAddon.isXrayAlive()) {
            list.add(FooterMessage.createWarning("JFrog Xray is unavailable. For details, please check Artifactory's log files.", admin));
        } else if (!xrayAddon.isXrayVersionValid()) {
            list.add(FooterMessage.createWarning("The version of Xray connected is incompatible with this" +
                    " version of Artifactory. Use Xray version 1.12.0 or higher.", admin));
        }
    }

    /**
     * Gives us a chance to insert system messages in SaaS, currently the content is retrieved from artifactory.system.properties
     * but a future version of this feature must support a REST call to a public-facing url we can insert messages in.
     *
     * These messages may be given a time range {@link FooterMessage#showFrom}, {@link FooterMessage#showUntil} that will
     * cause this mechanism to decide if they should be shown.
     */
    private void decorateWithCustomMessage(List<FooterMessage> list) {
        checkCustomMessage();
        if (footerMessageRawJson == null) {
            log.trace("Custom UI footer message set to null");
            return;
        }
        List<FooterMessage> messages;
        try {
            messages = JsonUtils.getInstance().readValueRef(footerMessageRawJson, new TypeReference<List<FooterMessage>>() {});
            if (CollectionUtils.isNullOrEmpty(messages)) {
                log.debug("Had a non-null value for custom message but couldn't deserialize: {}", footerMessageRawJson);
                return;
            }
        } catch (Exception e) {
            log.debug("Couldn't deserialize custom message from json: {}", footerMessageRawJson);
            log.debug("", e);
            return;
        }
        messages.forEach(msg -> addCustomMessageIfNeeded(list, msg));
    }

    private void checkCustomMessage() {
        if (lastFooterMessageFileChecked + TimeUnit.HOURS.toMillis(1) <= System.currentTimeMillis()) {
            lastFooterMessageFileChecked = System.currentTimeMillis();
            String footerMessageFileLocation = ConstantValues.uiCustomFooterMessageFile.getString();
            if (footerMessageFileLocation == null) {
                log.trace("Custom UI footer message path set to null");
                return;
            }
            File footerMessageFile = new File(footerMessageFileLocation);
            if (!footerMessageFile.exists()) {
                log.trace("Custom UI footer message set to not existing file");
            }
            try {
                footerMessageRawJson = Files.readFileToString(footerMessageFile);
            } catch (RuntimeException e) {
                log.warn("Unable to read custom UI footer message file at {}", footerMessageFile.getAbsolutePath());
                log.debug("", e);
            }
        }
    }

    /**
     * Filters {@param customMessage} by date range (if given).
     * Will add to message list if date range validated (or no date range)
     */
    private void addCustomMessageIfNeeded(List<FooterMessage> list, FooterMessage customMessage) {
        //We're inside the specified time range, or no range was specified?
        if (customMessage.shouldShowMessage()) {
            //Clear the range prop, no need to output it to ui (and its not ignored since we deserialize the custom messages)
            list.add(customMessage);
        }
    }
}
