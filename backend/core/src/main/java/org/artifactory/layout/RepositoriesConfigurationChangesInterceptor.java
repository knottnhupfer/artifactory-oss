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
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.ConstantValues;
import org.artifactory.config.ConfigurationChangesInterceptor;
import org.artifactory.config.ConfigurationException;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.request.UrlVerifier;
import org.artifactory.util.EdgeUtils;
import org.artifactory.util.UnsupportedByLicenseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.artifactory.addon.AddonType.WEBSTART;
import static org.artifactory.addon.build.BuildAddon.BUILD_INFO_REPO_NAME;
import static org.artifactory.descriptor.repo.SupportBundleRepoDescriptor.SUPPORT_BUNDLE_REPO_NAME;
import static org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor.RELEASE_BUNDLE_DEFAULT_REPO;
import static org.artifactory.util.distribution.DistributionConstants.EDGE_UPLOADS_REPO_KEY;

/**
 * @author Fred Simon
 */
@Component
public class RepositoriesConfigurationChangesInterceptor implements ConfigurationChangesInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RepositoriesConfigurationChangesInterceptor.class);

    private static final String ERR_DEL_BUILD_REPO = "Deletion of BuildInfo repository is not allowed";
    private static final String ERR_RESERVED_REPO_NAME = "%s is reserved repo name.";

    private AddonsManager addonsManager;
    private CentralConfigService configService;
    private UrlVerifier urlVerifier;

    @Autowired
    public RepositoriesConfigurationChangesInterceptor(AddonsManager addonsManager,
            CentralConfigService configService, UrlVerifier urlVerifier) {
        this.addonsManager = addonsManager;
        this.configService = configService;
        this.urlVerifier = urlVerifier;
    }

    @Override
    public void onBeforeSave(CentralConfigDescriptor newDescriptor) {
        CentralConfigDescriptor oldDescriptor = configService.getDescriptor();
        validateNoDistributionReposOnEdge(newDescriptor);
        validateReleaseBundleReposOnlyOnEntPlus(oldDescriptor, newDescriptor);
        assertRedirectOnlyInEntPlusOrEdge(newDescriptor);
        assertNoBlacklistedRemoteRepos(newDescriptor, oldDescriptor);
        handleBuildInfoRepo(newDescriptor, oldDescriptor);
        handleSupportBundleRepo(newDescriptor, oldDescriptor);
        handleReleaseBundlesRepo(newDescriptor, oldDescriptor);
        handleReverseProxySettings(newDescriptor);
        if (newDescriptor instanceof MutableCentralConfigDescriptor) {
            MutableCentralConfigDescriptor mutableCentralConfigDescriptor = (MutableCentralConfigDescriptor) newDescriptor;
            cleanClientCertificateIfDeleted(newDescriptor, mutableCentralConfigDescriptor);
            addEdgeUploadsRepoIfNeeded(mutableCentralConfigDescriptor);
            orderReposIfNeeded(newDescriptor, mutableCentralConfigDescriptor);
        }
    }

    void handleReverseProxySettings(CentralConfigDescriptor newDescriptor) {
        if (addonsManager.getArtifactoryRunningMode().isOss() && newDescriptor.getReverseProxies() != null && !newDescriptor.getReverseProxies().isEmpty()) {
            newDescriptor.getReverseProxies().clear();
        }
    }

    /**
     * Release-Bundles repository is allowed only on artifactory with Enterprise Plus license.
     */
    private void validateReleaseBundleReposOnlyOnEntPlus(CentralConfigDescriptor oldDescriptor, CentralConfigDescriptor newDescriptor) {
        if (!addonsManager.isClusterEnterprisePlus() &&
                !newDescriptor.getReleaseBundlesRepositoriesMap().isEmpty()) {
            Set<String> newRepos = newDescriptor.getReleaseBundlesRepositoriesMap().keySet();
            Set<String> oldRepos = oldDescriptor.getReleaseBundlesRepositoriesMap().keySet();
            if (!oldRepos.containsAll(newRepos)) {
                throw new UnsupportedByLicenseException(
                        "Release Bundles repositories are only supported with Artifactory Enterprise Plus license");
            }
            log.warn(
                    "Release Bundles repositories found, but are only supported with Artifactory Enterprise Plus license");
        }
    }

    /**
     * Except for Smart Remote repository, remote and Distribution repositories are not allowed on artifactory Edge.
     */
    private void validateNoDistributionReposOnEdge(CentralConfigDescriptor newDescriptor) {
        if (!addonsManager.isEdgeLicensed() && !addonsManager.isEdgeMixedInCluster()) {
            return;
        }
        if (!newDescriptor.getDistributionRepositoriesMap().isEmpty()) {
            throw new UnsupportedByLicenseException(
                    "Distribution repositories are not supported with Artifactory Edge license");
        }
    }

    private void assertNoBlacklistedRemoteRepos(CentralConfigDescriptor newDescriptor,
            CentralConfigDescriptor oldDescriptor) {
        Map<String, RemoteRepoDescriptor> newRemoteRepositoriesMap = newDescriptor.getRemoteRepositoriesMap();
        Map<String, String> blacklistedUrls = newRemoteRepositoriesMap.values().stream()
                .filter(url -> url.getUrl() != null)
                .filter(this::isRemoteRepoBlacklisted)
                .collect(Collectors.toMap(RepoBaseDescriptor::getKey, RemoteRepoDescriptor::getUrl));
        if (!blacklistedUrls.isEmpty()) {
            log.debug("Following repositories are Blacklisted: {}", blacklistedUrls);
            Map<String, RemoteRepoDescriptor> oldRemoteRepositoriesMap = oldDescriptor.getRemoteRepositoriesMap();
            Set<String> oldRemoteRepositories = oldRemoteRepositoriesMap.keySet();
            Set<String> newBlacklistedUrls = blacklistedUrls.keySet().stream()
                    .filter(repo -> !oldRemoteRepositories.contains(repo) ||
                            !isSameUrl(newRemoteRepositoriesMap, oldRemoteRepositoriesMap, repo))
                    .collect(Collectors.toSet());
            if (!newBlacklistedUrls.isEmpty()) {
                log.error("Config descriptor contains blocked URLs. blacklisted URLs: {}", newBlacklistedUrls);
                throw new IllegalArgumentException("Found a remote repository(ies) containing blacklisted URLs");
            }
            blacklistedUrls.keySet().stream().map(newRemoteRepositoriesMap::get)
                    .forEach(repo -> repo.setBlackedOut(true));
            log.warn("Found remote repositories with blocked URLs, which will be blacked out. blacklisted URLs: {}",
                    blacklistedUrls.keySet());
        }
    }

    private boolean isRemoteRepoBlacklisted(RemoteRepoDescriptor remoteRepoDescriptor) {
        return urlVerifier.isRemoteRepoBlocked(remoteRepoDescriptor.getUrl(), remoteRepoDescriptor.getKey());
    }

    private boolean isSameUrl(Map<String, RemoteRepoDescriptor> newRemoteRepositoriesMap,
            Map<String, RemoteRepoDescriptor> oldRemoteRepositoriesMap, String repo) {
        return newRemoteRepositoriesMap.get(repo).getUrl().equals(oldRemoteRepositoriesMap.get(repo).getUrl());
    }

    private void handleSupportBundleRepo(CentralConfigDescriptor newDescriptor, CentralConfigDescriptor oldDescriptor) {
        boolean isForbiddenSupportBundleRepoExist = isForbiddenSupportBundleRepoExist(newDescriptor);
        if (isForbiddenSupportBundleRepoExist) {
            if (isForbiddenSupportBundleRepoExist(oldDescriptor)) {
                log.warn("{} is internal repo name, please change any repo with the same key", SUPPORT_BUNDLE_REPO_NAME);
            } else {
                throw new ConfigurationException(format(ERR_RESERVED_REPO_NAME, SUPPORT_BUNDLE_REPO_NAME));
            }
        }
    }

    private boolean isForbiddenSupportBundleRepoExist(CentralConfigDescriptor descriptor) {
        return descriptor.getLocalRepositoriesMap().values().stream()
                .filter(repo -> repo.getKey().equals(SUPPORT_BUNDLE_REPO_NAME))
                .anyMatch(repo -> !RepoType.Support.equals(repo.getType())) ||
                descriptor.getRemoteRepositoriesMap().values().stream()
                        .anyMatch(repo -> repo.getKey().equals(SUPPORT_BUNDLE_REPO_NAME)) ||
                descriptor.getVirtualRepositoriesMap().values().stream()
                        .anyMatch(repo -> repo.getKey().equals(SUPPORT_BUNDLE_REPO_NAME));
    }

    /**
     * Validate that the new descriptor doesn't contain repository named 'release-bundles' which is not from
     * {@link RepoType#ReleaseBundles} type.
     */
    private void handleReleaseBundlesRepo(CentralConfigDescriptor newDescriptor, CentralConfigDescriptor oldDescriptor) {
        boolean isForbiddenReleaseBundleRepoExists = isForbiddenReleaseBundlesRepoExist(newDescriptor);
        if (isForbiddenReleaseBundleRepoExists) {
            if (isForbiddenReleaseBundlesRepoExist(oldDescriptor)) {
                log.warn("{} is internal repo name, please change any repo with the same key", RELEASE_BUNDLE_DEFAULT_REPO);
            } else {
                throw new ConfigurationException(format(ERR_RESERVED_REPO_NAME, RELEASE_BUNDLE_DEFAULT_REPO));
            }
        }
    }

    private boolean isForbiddenReleaseBundlesRepoExist(CentralConfigDescriptor descriptor) {
        return descriptor.getLocalRepositoriesMap().values().stream()
                .anyMatch(repo -> RELEASE_BUNDLE_DEFAULT_REPO.equals(repo.getKey())) ||
                descriptor.getRemoteRepositoriesMap().values().stream()
                        .anyMatch(repo -> RELEASE_BUNDLE_DEFAULT_REPO.equals(repo.getKey())) ||
                descriptor.getVirtualRepositoriesMap().values().stream()
                        .anyMatch(repo -> RELEASE_BUNDLE_DEFAULT_REPO.equals(repo.getKey())) ||
                descriptor.getDistributionRepositoriesMap().values().stream()
                        .anyMatch(repo -> RELEASE_BUNDLE_DEFAULT_REPO.equals(repo.getKey()));
    }

    /**
     * Asserting the following rules about BuildInfo repository:
     * 1. No more than 1 local BuildInfo Repo
     * 2. No remote or virtual BuildInfo Repo
     * 3. BuildInfo repository must exist. In case it doesn't exist, we add it with default name
     * {@link org.artifactory.addon.build.BuildAddon#BUILD_INFO_REPO_NAME}
     */
    private void handleBuildInfoRepo(CentralConfigDescriptor newDescriptor, CentralConfigDescriptor oldDescriptor) {
        assertNoMoreThanOneBuildInfo(newDescriptor);
        assertNoBuildInfoInRemoteVirtual(newDescriptor);
        addBuildInfoRepoIfNeeded(newDescriptor, oldDescriptor);
    }

    private void addBuildInfoRepoIfNeeded(CentralConfigDescriptor newDescriptor,
            CentralConfigDescriptor oldDescriptor) {
        if (!descriptorContainsBuildInfoRepo(newDescriptor)) {
            if (newDescriptor instanceof MutableCentralConfigDescriptor) {
                if (descriptorContainsBuildInfoRepo(oldDescriptor)) {
                    log.warn(ERR_DEL_BUILD_REPO);
                }
                addBuildInfoRepo(newDescriptor);
            } else {
                throw new ConfigurationException(ERR_DEL_BUILD_REPO);
            }
        }
    }

    private void addBuildInfoRepo(CentralConfigDescriptor newDescriptor) {
        String repoKey = BUILD_INFO_REPO_NAME;
        LocalRepoDescriptor localRepoDescriptor = generateBuildInfoDescriptor();
        // 5 fallbacks allowed for saving the repo
        for (int i = 0; i <= 5; i++) {
            repoKey = (i > 0) ? BUILD_INFO_REPO_NAME + "-" + i : BUILD_INFO_REPO_NAME;
            localRepoDescriptor.setKey(repoKey);
            if (newDescriptor.getLocalRepositoriesMap().putIfAbsent(repoKey, localRepoDescriptor) == null) {
                log.info("Added {} repository under {}", RepoType.BuildInfo.getType(), repoKey);
                break;
            }
            log.warn("'{}' is already taken as repo name for {} repo", repoKey, RepoType.BuildInfo.getType());
        }
        assertBuildInfoCreated(newDescriptor, repoKey);
    }

    private void assertBuildInfoCreated(CentralConfigDescriptor newDescriptor, String repoKey) {
        if (newDescriptor.getLocalRepositoriesMap().values().stream()
                .noneMatch(repo -> repoKey.equals(repo.getKey()) &&
                        RepoType.BuildInfo.equals(repo.getType()))) {
            throw new ConfigurationException("Can't save BuildInfo repository, all fallbacks names are taken.");
        }
    }

    private LocalRepoDescriptor generateBuildInfoDescriptor() {
        LocalRepoDescriptor localRepoDescriptor = new LocalRepoDescriptor();
        localRepoDescriptor.setType(RepoType.BuildInfo);
        return localRepoDescriptor;
    }

    private void assertNoBuildInfoInRemoteVirtual(CentralConfigDescriptor newDescriptor) {
        if (newDescriptor.getRemoteRepositoriesMap().values().stream()
                .anyMatch(repo -> RepoType.BuildInfo.equals(repo.getType()))
                || newDescriptor.getVirtualRepositoriesMap().values().stream()
                .anyMatch(repo -> RepoType.BuildInfo.equals(repo.getType()))) {
            throw new ConfigurationException(
                    "Configuring a Remote/virtual " + RepoType.BuildInfo.getType() + " repository is not allowed");
        }
    }

    private void assertNoMoreThanOneBuildInfo(CentralConfigDescriptor newDescriptor) {
        if (newDescriptor.getLocalRepositoriesMap().values().stream()
                .filter(repo -> RepoType.BuildInfo.equals(repo.getType()))
                .count() > 1) {
            throw new ConfigurationException("Can't have more than one " + RepoType.BuildInfo.getType() +
                    " repository.");
        }
    }
    private void assertRedirectOnlyInEntPlusOrEdge(CentralConfigDescriptor newDescriptor) {
        if (!(isEntPlus() || isEdgeLicensed())) {
            // get local and remote repos with redirect enabled
            List<RealRepoDescriptor> localRedirectRepos = getRedirectRepos(newDescriptor.getLocalRepositoriesMap());
            List<RealRepoDescriptor> remoteRedirectRepos = getRedirectRepos(newDescriptor.getRemoteRepositoriesMap());
            if (!localRedirectRepos.isEmpty() || !remoteRedirectRepos.isEmpty()) {
                log.warn("Redirect feature is only enabled with Enterprise plus or edge license!");
                // turn off redirect
                if (newDescriptor instanceof MutableCentralConfigDescriptor) {
                    localRedirectRepos.forEach(repo -> repo.setDownloadRedirectConfig(null));
                    remoteRedirectRepos.forEach(repo -> repo.setDownloadRedirectConfig(null));
                }
            }
        }
    }

    private boolean isEntPlus() {
        return addonsManager.isClusterEnterprisePlus();
    }

    private boolean isEdgeLicensed() {
        return addonsManager.isEdgeLicensed();
    }

    private List<RealRepoDescriptor> getRedirectRepos(Map<String, ? extends RealRepoDescriptor> repos) {
        return repos.values().stream()
                .filter(RealRepoDescriptor::isDownloadRedirect)
                .collect(Collectors.toList());
    }

    private void addEdgeUploadsRepoIfNeeded(MutableCentralConfigDescriptor mutableDescriptor) {
        if (addonsManager.isEdgeLicensed() && !mutableDescriptor.isRepositoryExists(EDGE_UPLOADS_REPO_KEY)) {
            EdgeUtils.addEdgeUploadsRepo(mutableDescriptor);
        }
    }

    private void orderReposIfNeeded(CentralConfigDescriptor newDescriptor,
            MutableCentralConfigDescriptor mutableCentralConfigDescriptor) {
        if (ConstantValues.disableGlobalRepoAccess.getBoolean()) {
            // If global repo is disabled, all repository key names are ordered
            mutableCentralConfigDescriptor.setRemoteRepositoriesMap(sortMap(newDescriptor.getRemoteRepositoriesMap()));
            mutableCentralConfigDescriptor.setLocalRepositoriesMap(sortMap(newDescriptor.getLocalRepositoriesMap()));
            mutableCentralConfigDescriptor
                    .setVirtualRepositoriesMap(sortMap(newDescriptor.getVirtualRepositoriesMap()));
        }
    }

    private void cleanClientCertificateIfDeleted(CentralConfigDescriptor newDescriptor,
            MutableCentralConfigDescriptor mutableCentralConfigDescriptor) {
        Map<String, RemoteRepoDescriptor> remoteRepos = newDescriptor.getRemoteRepositoriesMap();
        remoteRepos.forEach((key, descriptor) -> {
            if (descriptor instanceof HttpRepoDescriptor) {
                String clientCert = ((HttpRepoDescriptor) descriptor).getClientTlsCertificate();
                if (StringUtils.isNotBlank(clientCert) && addonsManager.isAddonSupported(WEBSTART)) {
                    ArtifactWebstartAddon webstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
                    List<String> certAliases = webstartAddon.getSslCertNames();
                    if (certAliases != null && certAliases.stream().noneMatch(clientCert::equalsIgnoreCase)) {
                        ((HttpRepoDescriptor) descriptor).setClientTlsCertificate(null);
                    }
                }
            }
        });
        mutableCentralConfigDescriptor.setRemoteRepositoriesMap(remoteRepos);
    }

    private <T extends RepoDescriptor> Map<String, T> sortMap(Map<String, T> map) {
        String[] origKeys = map.keySet().toArray(new String[0]);
        String[] orderedKeys = map.keySet().toArray(new String[0]);
        Arrays.sort(orderedKeys);
        if (Arrays.equals(origKeys, orderedKeys)) {
            return map;
        } else {
            Map<String, T> result = new LinkedHashMap<>(map.size());
            for (String orderedKey : orderedKeys) {
                result.put(orderedKey, map.get(orderedKey));
            }
            return result;
        }
    }

    private boolean descriptorContainsBuildInfoRepo(CentralConfigDescriptor descriptor) {
        return descriptor != null
                && descriptor.getLocalRepositoriesMap().values().stream()
                .anyMatch(repo -> RepoType.BuildInfo.equals(repo.getType()));
    }
}
