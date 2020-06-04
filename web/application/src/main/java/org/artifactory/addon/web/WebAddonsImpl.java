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

package org.artifactory.addon.web;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonType;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.addon.build.BuildAddon;
import org.artifactory.addon.build.artifacts.ProducedBy;
import org.artifactory.addon.build.artifacts.UsedBy;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.config.VersionInfo;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.DockerRepositoryAction;
import org.artifactory.api.rest.build.artifacts.BuildArtifactsRequest;
import org.artifactory.api.rest.build.diff.BuildsDiff;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.build.ArtifactoryBuildArtifact;
import org.artifactory.build.BuildRun;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.mail.MailServerDescriptor;
import org.artifactory.fs.FileInfo;
import org.artifactory.request.RequestThreadLocal;
import org.artifactory.security.UserInfo;
import org.artifactory.util.HttpUtils;
import org.jfrog.build.api.BaseBuildFileBean;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.BuildRetention;
import org.jfrog.build.api.dependency.BuildPatternArtifacts;
import org.jfrog.build.api.dependency.BuildPatternArtifactsRequest;
import org.jfrog.build.api.release.BuildArtifactsMapping;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

/**
 * Default implementation of the addons interface. Represents a normal execution of artifactory.
 * <p/>
 * <strong>NOTE!</strong> Do not create anonymous or non-static inner classes in addon
 *
 * @author freds
 * @author Yossi Shaul
 */
@Component
public final class WebAddonsImpl implements CoreAddons, BuildAddon {
    private static final Logger log = LoggerFactory.getLogger(WebAddonsImpl.class);

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public Set<ArtifactoryBuildArtifact> getBuildArtifactsFileInfosWithFallback(Build build, List<String> repoToSearch) {
        return Sets.newHashSet();
    }

    @Override
    public void renameBuildNameProperty(String from, String to) {
        //Not relevant
    }

    @Override
    public BasicStatusHolder discardOldBuilds(String buildName, BuildRetention buildRetention, boolean async) {
        BasicStatusHolder basicStatusHolder = new BasicStatusHolder();
        basicStatusHolder.error("Build retention policy is available only with Artifactory commercial license.",
                HttpStatus.SC_BAD_REQUEST, null, log);
        return basicStatusHolder;
    }

    @Override
    public BuildPatternArtifacts getBuildPatternArtifacts(
            @Nonnull BuildPatternArtifactsRequest buildPatternArtifactsRequest, String servletContextUrl) {
        return new BuildPatternArtifacts();
    }

    @Override
    public Map<FileInfo, String> getBuildArtifacts(BuildArtifactsRequest buildArtifactsRequest) {
        return null;
    }

    @Override
    public File getBuildArtifactsArchive(BuildArtifactsRequest buildArtifactsRequest) {
        return null;
    }

    @Override
    public BuildsDiff getBuildsDiff(Build firstBuild, Build secondBuild, String baseStorageInfoUri) {
        return null;
    }

    @Override
    public FileInfo getFileBeanInfo(BaseBuildFileBean artifact, Build build) {
        return null;
    }

    @Override
    public String getListBrowsingVersion() {
        VersionInfo versionInfo = centralConfigService.getVersionInfo();
        return format("Artifactory/%s", versionInfo.getVersion());
    }

    @Override
    public String getArtifactoryServerName() {
        MutableCentralConfigDescriptor mutableCentralConfigDescriptor = centralConfigService.getMutableDescriptor();
        return mutableCentralConfigDescriptor.getServerName();
    }

    /**
     * Get the Artifactory URL for mail operations. The URL in this implementations is taken from the Mail server
     * configs adding the '/webapp/' suffix.
     *
     * @return The Artifactory URL from the mail server configurations
     */
    @Override
    public String getMailConfigArtifactoryUrl() {
        // mail configurations is always the best match
        MutableCentralConfigDescriptor mutableCentralConfigDescriptor = centralConfigService.getMutableDescriptor();
        MailServerDescriptor mailServer = mutableCentralConfigDescriptor.getMailServer();
        if (mailServer != null && StringUtils.isNotBlank(mailServer.getArtifactoryUrl())) {
            return PathUtils.addTrailingSlash(mailServer.getArtifactoryUrl()) + HttpUtils.WEBAPP_URL_PATH_PREFIX + "/";
        }

        // request is the second best match
        HttpServletRequest request = RequestThreadLocal.getRequest();
        if (request != null) {
            String url;
            url = HttpUtils.getServletContextUrl(request);
            if (StringUtils.isNotBlank(url)) {
                return PathUtils.addTrailingSlash(url) + HttpUtils.WEBAPP_URL_PATH_PREFIX + "/";
            }
        }

        // baseUrl is the last option
        MutableCentralConfigDescriptor descriptor = centralConfigService.getMutableDescriptor();
        String urlBase = descriptor.getUrlBase();
        if (StringUtils.isNotBlank(urlBase)) {
            return PathUtils.addTrailingSlash(urlBase) + HttpUtils.WEBAPP_URL_PATH_PREFIX + "/";
        }
        return "";
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public boolean isCreateDefaultAdminAccountAllowed() {
        return true;
    }

    @Override
    public boolean isAolAdmin(UserInfo userInfo) {
        return false;
    }

    @Override
    public boolean isAol() {
        return false;
    }

    @Override
    public boolean isDashboardUser() {
        return false;
    }

    @Override
    @Nonnull
    public List<String> getUsersForBackupNotifications() {
        List<UserInfo> allUsers = ContextHelper.get().beanForType(UserGroupService.class).getAllUsers(true);
        List<String> adminEmails = Lists.newArrayList();
        for (UserInfo user : allUsers) {
            if (user.isEffectiveAdmin()) {
                if (StringUtils.isNotBlank(user.getEmail())) {
                    adminEmails.add(user.getEmail());
                } else {
                    log.debug("The user: '{}' has no email address.", user.getUsername());
                }
            }
        }
        return adminEmails;
    }

    /**
     * Validates that given licenseHash is different from license installed on this instance,
     * unless artifactoryId and current instance artifactoryId are equal (e.g same Artifactory)
     *
     * @param licenseHash license to check
     * @param artifactoryId artifactory id of the checked license
     */
    @Override
    public boolean validateTargetHasDifferentLicense(String licenseHash, String artifactoryId) {
        AddonsManager addonsManager = getAddonsManager();
        if (Strings.isNullOrEmpty(licenseHash)) {
            log.debug("LicenseHash is empty, validation isn't possible");
        } else {
            if (!addonsManager.getLicenseKeyHash(false).equals(licenseHash)) {
                return true;
            } else {
                if (!Strings.isNullOrEmpty(artifactoryId)) {
                    return HttpUtils.getHostId().equals(artifactoryId);
                } else {
                    log.debug("LicenseHash is equal to currently used license, but artifactoryId is empty, " +
                            "validation of destination and source artifactories being same instance isn't possible");
                }
            }
        }
        return false;
    }

    @Override
    public void validateTargetHasDifferentLicenseKeyHash(String targetLicenseHash, List<String> addons) {
        AddonsManager addonsManager = getAddonsManager();
        // Skip Trial license
        if (isTrial(addonsManager)) {
            log.debug("Source has trial license, skipping target instance license validation.");
            return;
        }

        if (StringUtils.isNotBlank(targetLicenseHash) && targetLicenseHash.equals("Artifactory OSS")) {
            throw new IllegalArgumentException("Replication to remote open-source Artifactory instance is not supported.");
        }

        if (StringUtils.isNotBlank(targetLicenseHash) && targetLicenseHash.equals("JFrog Container Registry")) {
            throw new IllegalArgumentException("Replication to remote JFrog Container Registry instance is not supported.");
        }

        if (StringUtils.isNotBlank(targetLicenseHash) && targetLicenseHash.equalsIgnoreCase("Artifactory Community Edition for C/C++")) {
            throw new IllegalArgumentException("Replication to remote Artifactory community edition instance is not supported.");
        }

        if (StringUtils.isBlank(targetLicenseHash)) {
            if (addons == null || !addons.contains(AddonType.REPLICATION.getAddonName())) {
                throw new IllegalArgumentException(
                        "Replication between an open-source Artifactory instance is not supported.");
            }

            throw new IllegalArgumentException(
                    "Could not retrieve license key from remote target, user must have deploy permissions.");
        }
        if (addonsManager.getLicenseKeyHash(false).equals(targetLicenseHash)) {
            throw new IllegalArgumentException("Replication between same-license servers is not supported.");
        }
    }

    @Override
    public void validateMultiPushReplicationSupportedForTargetLicense(String targetLicenseKey,
            boolean isMultiPushConfigure, String targetUrl) {
        AddonsManager addonsManager = getAddonsManager();
        if (!addonsManager.isLicenseKeyHashSupportMultiPush(targetLicenseKey) && isMultiPushConfigure) {
            log.info("Multi Push Replication is not supported for target :{}", targetUrl);
            throw new IllegalArgumentException(
                    "Multi Push Replication is supported for targets with an enterprise license only");
        }
    }

    @Override
    public String getBuildNum() {
        VersionInfo versionInfo = centralConfigService.getVersionInfo();
        return format("%s rev %s", versionInfo.getVersion(), versionInfo.getRevision());
    }

    private boolean isTrial(AddonsManager addonsManager) {
        return addonsManager.isLicenseInstalled() && "Trial".equalsIgnoreCase(addonsManager.getProAndAolLicenseDetails().getType());
    }

    private AddonsManager getAddonsManager() {
        return ContextHelper.get().beanForType(AddonsManager.class);
    }

    @Override
    public void sendDockerRepoEvent(String repoName, String dockerApiVersion, DockerRepositoryAction action) {
        //Not Relevant
    }

    @Override
    public Map<FileInfo, String > mapBuildArtifactsToOutputPaths(Set<FileInfo> fileInfos, BuildArtifactsMapping mapping) {
        return Maps.newHashMap();
    }

    @Override
    public Set<FileInfo> filterBuildArtifactsByPattern(Set<FileInfo> buildArtifacts,
            BuildArtifactsMapping mapping) {
        return Sets.newHashSet();
    }

    @Override
    public void populateArtifactBuildInfo(FileInfo artifact, List<BuildRun> dependencyBuilds,
            List<BuildRun> producedByBuilds, List<ProducedBy> producedByList, List<UsedBy> usedByList) {

    }
}
