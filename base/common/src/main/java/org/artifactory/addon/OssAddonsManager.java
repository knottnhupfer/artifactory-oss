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

package org.artifactory.addon;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.artifactory.addon.conan.ConanAddon;
import org.artifactory.addon.docker.DockerAddon;
import org.artifactory.addon.eula.EulaService;
import org.artifactory.addon.helm.HelmAddon;
import org.artifactory.addon.license.*;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.service.ArtifactoryServersCommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.artifactory.addon.FooterMessage.FooterMessageVisibility.admin;
import static org.artifactory.addon.FooterMessage.createError;
import static org.artifactory.addon.license.VerificationResult.valid;

/**
 * @author Yossi Shaul
 */
@Component
public class OssAddonsManager implements AddonsManager, AddonsWebManager {
    private static final Logger log = LoggerFactory.getLogger(OssAddonsManager.class);

    public static final String OSS_LICENSE_KEY_HASH = "Artifactory OSS";
    public static final String FREE_LICENSE_KEY_HASH = "Artifactory Community Edition for C/C++";
    public static final String JCR_LICENSE_KEY_HASH = "JFrog Container Registry";
    protected ArtifactoryContext context;
    private ImmutableMap<String, AddonInfo> addonPathsByName;
    @Autowired
    private ArtifactoryServersCommonService serversService;
    private final Set<AddonType> jcrAddons = ImmutableSet.of(AddonType.DOCKER, AddonType.HELM, AddonType.PROPERTIES,
            AddonType.SMART_REPO, AddonType.S3, AddonType.DISTRIBUTION, AddonType.AQL, AddonType.AOL);

    @Autowired
    private void setApplicationContext(ApplicationContext context) {
        this.context = (ArtifactoryContext) context;
        addonPathsByName = this.context.getConfigPaths().getInstalledAddonPaths();
    }

    @Override
    public <T extends Addon> T addonByType(Class<T> type) {
        return context.beanForType(type);
    }

    @Override
    public String getProductName() {
        return "Artifactory";
    }

    @Override
    public String getLicenseRequiredMessage(String licensePageUrl) {
        return "Add-ons are currently disabled.";
    }

    @Override
    public void onNoInstalledLicense(boolean userVisitedLicensePage, NoInstalledLicenseAction action) {
        //Not relevant
    }

    @Override
    public boolean isAdminPageAccessible() {
        AuthorizationService authService = context.beanForType(AuthorizationService.class);
        return authService.isAdmin() || authService.hasRepoPermission(ArtifactoryPermission.MANAGE);
    }

    @Override
    public List<AddonInfo> getInstalledAddons(Set<String> excludedAddonKeys) {
        List<AddonInfo> addonInfos = Lists.newArrayList();
        for (AddonType addonType : AddonType.values()) {
            if (AddonType.AOL.equals(addonType)) {
                continue;
            }
            if (isJcrVersion() && isAddonSupportedInJcr(addonType)
                    && addonPathsByName.containsKey(addonType.getAddonName())) {
                updateAddonState(addonInfos, addonType);
                continue;
            }

            if (isConanCEVersion() && AddonType.CONAN.equals(addonType) &&
                    addonPathsByName.containsKey(addonType.getAddonName())) {
                updateAddonState(addonInfos, addonType);
            } else {
                addonInfos.add(new AddonInfo(addonType.getAddonName(), addonType.getAddonDisplayName(), null,
                        AddonState.INACTIVATED, null, addonType.getDisplayOrdinal()));
            }
        }

        Collections.sort(addonInfos);
        return addonInfos;
    }

    private void updateAddonState(List<AddonInfo> addonInfos, AddonType addonType) {
        AddonInfo addonInfo = addonPathsByName.get(addonType.getAddonName());
        if (!addonInfo.getAddonState().equals(AddonState.DISABLED) &&
                !addonInfo.getAddonState().equals(AddonState.ACTIVATED)) {
            addonInfo.setAddonState(AddonState.ACTIVATED);
        }
        addonInfos.add(addonInfo);
    }

    @Override
    public void prepareAddonManager() {
        // Do nothing as this type of AddonsManager does not need any preparation.
    }

    @Override
    public List<String> getEnabledAddonNames() {
        return Collections.emptyList();
    }

    @Override
    public boolean isLicenseInstalled() {
        return false;
    }

    @Override
    public boolean isAddonSupported(AddonType addonType) {
        return addonType == AddonType.CONAN && isConanCEVersion() ||
                isJcrVersion() && isAddonSupportedInJcr(addonType);
    }

    private boolean isAddonSupportedInJcr(AddonType addonType) {
        return jcrAddons.contains(addonType);
    }

    boolean isConanCEVersion() {
        return !addonByType(ConanAddon.class).isDefault();
    }

    boolean isJcrVersion() {
        return !addonByType(DockerAddon.class).isDefault() && !addonByType(HelmAddon.class).isDefault() ;
    }

    private boolean isJcrAolVersion() {
        // except JCR, OssAddonsManager should not have any of the Pro Addons implementation
        return isJcrVersion() && !addonByType(CoreAddons.class).isDefault();
    }

    @Override
    public boolean isHaLicensed() {
        return false;
    }

    @Override
    public boolean isTrialLicense() {
        return false;
    }

    @Override
    public boolean isXrayLicensed() {
        return false;
    }

    @Override
    public boolean xrayTrialSupported() {
        return false;
    }

    @Override
    public LicenseOperationStatus addAndActivateLicenses(Set<String> licenseKeys, boolean notifyListeners, boolean skipOnlineValidation) {
        if (isConanCEVersion()) {
            throw new UnsupportedOperationException("Operation is not supported on Artifactory Free");
        }
        if (isJcrVersion()) {
            throw new UnsupportedOperationException("Operation is not supported on Artifactory JCR");
        }
        throw new UnsupportedOperationException("Operation is not supported on Artifactory OSS");
    }

    @Override
    public LicenseOperationStatus addAndActivateLicense(String licenseKey, boolean notifyListeners, boolean skipOnlineValidation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLicenseKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLicensePerProduct(String productKey) {
        return null;
    }

    @Override
    public Date getLicenseValidUntil() {
        return null;
    }

    @Override
    public String getLicenseKeyHash(boolean excludeNewLicensesSuffix) {
        if (isConanCEVersion()) {
            return FREE_LICENSE_KEY_HASH;
        } else if (isJcrVersion()) {
            return JCR_LICENSE_KEY_HASH;
        }
        return OSS_LICENSE_KEY_HASH;
    }

    @Override
    public boolean isEdgeOrEntPlusLicensed(String licenseKeyHash) {
        return false;
    }

    @Override
    public boolean isOssLicensed(String licenseKeyHash) {
        return true;
    }

    @Override
    public boolean isLicenseKeyHashSupportMultiPush(String licenseKeyHash) {
        return false;
    }

    @Override
    public boolean lockdown() {
        return false;
    }

    @Override
    public List<ArtifactoryHaLicenseDetails> getClusterLicensesDetails() {
        String type = getLicencseType();
        ArtifactoryHaLicenseDetails haLicenseDetails = new ArtifactoryHaLicenseDetails(type, "", "", "", true, "", "");
        return Lists.newArrayList(haLicenseDetails);
    }

    @Override
    public ArtifactoryBaseLicenseDetails getProAndAolLicenseDetails() {
        String type = getLicencseType();
        return new ArtifactoryBaseLicenseDetails(type, "", "");
    }

    @Override
    public String getFooterMessage(boolean admin) {
        return null;
    }

    @Override
    public FooterMessage getLicenseFooterMessage() {
        return null;
    }

    @Override
    public FooterMessage getEULAFooterMessage() {
        if (isJcrAndEulaNotSigned()) {
            return createError("No signed EULA found. To activate, proceed to sign the EULA.", admin);
        }
        return null;
    }

    @Override
    public String getLicenseTypeForProduct(String productName) {
        return getLicencseType();
    }

    @Override
    public boolean isLicenseExpired(String productName) {
        return false;
    }

    @Override
    public boolean shouldReAcquireLicense() {
        return false;
    }

    @Override
    public AddRemoveLicenseVerificationResult isLicenseKeyValid(String licenseKey, boolean excludeTrial, boolean validateOnline) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLicenses(Set<String> licenses, LicenseOperationStatus status, boolean skipOnlineValidation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeLicenses(Set<String> licenses, LicenseOperationStatus status) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void releaseLicense() {
        // No license is available on this implementation
    }

    @Override
    public void activateLicense(Set<String> licenseHashesToIgnore, LicenseOperationStatus status, boolean notifyListeners,
            boolean initializing) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetLicenseCache() {
        //nop
    }

    @Override
    public boolean reloadLicensesStorageIfNeeded() {
        return false;
    }

    @Override
    public boolean isEdgeLicensed() {
        return false;
    }

    @Override
    public boolean isEnterprisePlusInstalled() {
        return false;
    }

    @Override
    public boolean isBinaryProviderConfigured(String type) {
        return false;
    }

    @Override
    public boolean isEdgeMixedInCluster() {
        return false;
    }

    @Override
    public boolean isUploadRequestBlocked(ArtifactoryRequest request,
            ArtifactoryResponse response) {
        return false;
    }

    @Override
    public VerificationResult verifyAllArtifactoryServers(boolean running) {
        List<ArtifactoryServer> otherMembers = serversService.getOtherActiveMembers();
        String type = getLicencseType();
        if (!isJcrAolVersion() && !otherMembers.isEmpty()) {
            context.setOffline(); //leave it here
            throw new IllegalStateException("Found active HA servers in DB, " + type + " is not supported in by active HA " +
                    "environment. Shutting down Artifactory.");
        }
        return valid;
    }

    @Override
    public ArtifactoryRunningMode getArtifactoryRunningMode() {
        if (isConanCEVersion()) {
            return ArtifactoryRunningMode.CONAN;
        } else if (isJcrAolVersion()) {
            return ArtifactoryRunningMode.AOL_JCR;
        } else if (isJcrVersion()) {
            return ArtifactoryRunningMode.JCR;
        }
        return ArtifactoryRunningMode.OSS;
    }

    @Override
    public boolean isPartnerLicense() {
        return false;
    }

    @Override
    public void interceptResponse(ArtifactoryResponse response)  throws IOException {
        interceptResponseInternally(response, true);
    }

    @Override
    public void interceptRestResponse(ArtifactoryResponse response, String path) throws IOException {
        for (String s : trialExpirationRestCallToBlock) {
            if (path.startsWith(s)) {
                interceptResponseInternally(response, false);
            }
        }
    }

    private void interceptResponseInternally(ArtifactoryResponse response, boolean doWriteResponse) throws IOException {
        if (isJcrAndEulaNotSigned()) {
            interceptResponseInternally(response, doWriteResponse, "In order to use Artifactory you must accept the EULA first");
        }
    }

    private boolean isJcrAndEulaNotSigned() {
        return getArtifactoryRunningMode().isJCR() && context.beanForType(EulaService.class).isRequired();
    }

    private void interceptResponseInternally(ArtifactoryResponse response, boolean writeResponse, String msg) throws IOException {
        if (writeResponse) {
            response.sendError(HttpStatus.SC_SERVICE_UNAVAILABLE, msg, log);
        }
        else {
            throw new HttpResponseException(HttpStatus.SC_SERVICE_UNAVAILABLE, msg);
        }
    }

    private String getLicencseType() {
        String type = "OSS";
        if (isConanCEVersion()) {
            type = "Community Edition for C/C++";
        } else if (isJcrVersion()) {
            type ="JCR Edition";
        }
        return type;
    }
}
