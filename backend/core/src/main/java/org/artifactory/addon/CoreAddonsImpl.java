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

import org.apache.http.HttpStatus;
import org.artifactory.addon.binary.provider.BinaryProviderApiAddon;
import org.artifactory.addon.blob.BlobInfoAddon;
import org.artifactory.addon.bower.BowerAddon;
import org.artifactory.addon.build.ArtifactBuildAddon;
import org.artifactory.addon.chef.ChefAddon;
import org.artifactory.addon.cocoapods.CocoaPodsAddon;
import org.artifactory.addon.composer.ComposerAddon;
import org.artifactory.addon.conan.ConanAddon;
import org.artifactory.addon.conda.CondaAddon;
import org.artifactory.addon.cran.CranAddon;
import org.artifactory.addon.debian.DebianAddon;
import org.artifactory.addon.distribution.DistributionAddon;
import org.artifactory.addon.docker.DockerAddon;
import org.artifactory.addon.filteredresources.FilteredResourcesAddon;
import org.artifactory.addon.gems.GemsAddon;
import org.artifactory.addon.gitlfs.GitLfsAddon;
import org.artifactory.addon.go.GoAddon;
import org.artifactory.addon.ha.workitem.HaMessageWorkItem;
import org.artifactory.addon.helm.HelmAddon;
import org.artifactory.addon.keys.KeysAddon;
import org.artifactory.addon.ldapgroup.LdapUserGroupAddon;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.addon.npm.NpmAddon;
import org.artifactory.addon.nuget.UiNuGetAddon;
import org.artifactory.addon.oauth.OAuthSsoAddon;
import org.artifactory.addon.opkg.OpkgAddon;
import org.artifactory.addon.properties.ArtifactPropertiesAddon;
import org.artifactory.addon.puppet.PuppetAddon;
import org.artifactory.addon.pypi.PypiAddon;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.addon.replication.PushReplicationSettings;
import org.artifactory.addon.replication.RemoteReplicationSettings;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.addon.replication.event.ReplicationEventQueueWorkItem;
import org.artifactory.addon.replication.event.ReplicationOwnerModel;
import org.artifactory.addon.replicator.ReplicatorAddon;
import org.artifactory.addon.security.JvmConflictGuardProvider;
import org.artifactory.addon.signed.url.SignedUrlAddon;
import org.artifactory.addon.smartrepo.EdgeSmartRepoAddon;
import org.artifactory.addon.smartrepo.SmartRepoAddon;
import org.artifactory.addon.sso.HttpSsoAddon;
import org.artifactory.addon.sso.crowd.CrowdAddon;
import org.artifactory.addon.sso.saml.SamlSsoAddon;
import org.artifactory.addon.support.SupportAddon;
import org.artifactory.addon.watch.ArtifactWatchAddon;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.addon.yum.YumAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.config.ConfigurationException;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.RepoResource;
import org.artifactory.md.Properties;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.cache.expirable.CacheExpiryStrategyImpl;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.request.Request;
import org.artifactory.resource.UnfoundRepoResource;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.security.UserGroupInfo;
import org.artifactory.storage.fs.lock.FsItemsVault;
import org.artifactory.util.RepoLayoutUtils;
import org.jfrog.storage.common.ConflictGuard;
import org.jfrog.storage.common.ConflictsGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of the core-related addon factories.
 *
 * @author Yossi Shaul
 */
@Component
public class CoreAddonsImpl
        implements WebstartAddon, LicensesAddon, LdapGroupAddon, CrowdAddon, PropertiesAddon, LayoutsCoreAddon,
        FilteredResourcesAddon, ReplicationAddon, YumAddon, NuGetAddon, RestCoreAddon,
        GemsAddon, HaAddon, NpmAddon, BowerAddon, DebianAddon, OpkgAddon, PypiAddon, PuppetAddon, DockerAddon,
        VagrantAddon, ArtifactWatchAddon, ArtifactBuildAddon, UiNuGetAddon, LdapUserGroupAddon, GitLfsAddon,
        CocoaPodsAddon, ArtifactWebstartAddon, SamlSsoAddon, OAuthSsoAddon, HttpSsoAddon, SmartRepoAddon,
        EdgeSmartRepoAddon, ArtifactPropertiesAddon, SupportAddon, XrayAddon, ComposerAddon, ConanAddon, ChefAddon,
        ReleaseBundleAddon, HelmAddon, GoAddon, DistributionAddon, BlobInfoAddon, KeysAddon, BinaryProviderApiAddon,
        ReplicatorAddon,SecurityResourceAddon, CranAddon, CondaAddon, SignedUrlAddon, Addon {

    private static final Logger log = LoggerFactory.getLogger(CoreAddonsImpl.class);

    private JvmConflictGuardProvider jvmConflictGuardProvider = new JvmConflictGuardProvider();

    public CoreAddonsImpl() {
        log.debug("Initializing JVM locking provider");
    }

    private JvmConflictGuardProvider getConflictGuardProvider() {
        return jvmConflictGuardProvider;
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public void addExternalGroups(String userName, Set<UserGroupInfo> groups) {
        //noop
    }

    @Override
    public RepoResource getFilteredResource(Request request, FileInfo fileInfo, InputStream fileInputStream) {
        return new UnfoundRepoResource(fileInfo.getRepoPath(),
                "Creation of a filtered resource requires the Properties add-on.", HttpStatus.SC_FORBIDDEN);
    }

    @Override
    public RepoResource getZipResource(Request request, FileInfo fileInfo, InputStream stream) {
        return new UnfoundRepoResource(fileInfo.getRepoPath(),
                "Direct resource download from zip requires the Filtered resources add-on.", HttpStatus.SC_FORBIDDEN);
    }

    @Override
    public void assertLayoutConfigurationsBeforeSave(CentralConfigDescriptor newDescriptor) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        // Even that we are in the CoreAddonsImpl, it is not for sure that that we are on OSS, it might be that we got
        // the the CoreAddonsImpl because there is not yet license activated, so we got this bean from the
        // AddonManager#addonByType due to the missing license
        if (addonsManager.getArtifactoryRunningMode().isOss()) {
            List<RepoLayout> repoLayouts = newDescriptor.getRepoLayouts();
            if ((repoLayouts == null) || repoLayouts.isEmpty()) {
                throw new ConfigurationException("Could not find any repository layouts.");
            }
            if (repoLayouts.size() != 15) {
                throw new ConfigurationException(
                        "There should be 15 default repository layouts, but found " + repoLayouts.size());
            }

            assertLayoutsExistsAndEqual(repoLayouts, RepoLayoutUtils.MAVEN_2_DEFAULT, RepoLayoutUtils.IVY_DEFAULT,
                    RepoLayoutUtils.GRADLE_DEFAULT, RepoLayoutUtils.MAVEN_1_DEFAULT);
        }
    }

    private void assertLayoutsExistsAndEqual(List<RepoLayout> repoLayouts, RepoLayout... expectedLayouts) {
        for (RepoLayout expectedLayout : expectedLayouts) {
            assertLayoutExistsAndEqual(repoLayouts, expectedLayout);
        }
    }

    private void assertLayoutExistsAndEqual(List<RepoLayout> repoLayouts, RepoLayout expectedLayout) {
        if (!repoLayouts.contains(expectedLayout)) {
            throw new ConfigurationException("Could not find the default repository layout: " +
                    expectedLayout.getName());
        }

        RepoLayout existingLayoutConfig = repoLayouts.get(repoLayouts.indexOf(expectedLayout));
        if (!expectedLayout.equals(existingLayoutConfig)) {
            throw new ConfigurationException("The configured repository layout '" + expectedLayout.getName() +
                    "' is different from the default configuration.");
        }
    }

    @Override
    public BasicStatusHolder performRemoteReplication(RemoteReplicationSettings settings) {
        return getReplicationRequiredStatusHolder();
    }

    @Override
    public BasicStatusHolder performLocalReplication(PushReplicationSettings settings) {
        return getReplicationRequiredStatusHolder();
    }

    @Override
    public void scheduleImmediateLocalReplicationTask(LocalReplicationDescriptor replicationDescriptor,
                                                      BasicStatusHolder statusHolder) {
        printMissingReplicationTaskSupportError(statusHolder, HttpStatus.SC_BAD_REQUEST);
    }

    @Override
    public void scheduleImmediateRemoteReplicationTask(RemoteReplicationDescriptor replicationDescriptor,
                                                       BasicStatusHolder statusHolder) {
        printMissingReplicationTaskSupportError(statusHolder, HttpStatus.SC_BAD_REQUEST);
    }

    private void printMissingReplicationTaskSupportError(BasicStatusHolder statusHolder, int status) {
        statusHolder.error("Error: the replication addon is required for this operation.", status, log);
    }

    @Override
    public BasicStatusHolder filterIfMultiPushIsNotAllowed(List<LocalReplicationDescriptor> pushReplications) {
        BasicStatusHolder multiStatusHolder = new BasicStatusHolder();
        multiStatusHolder.error("Error: an HA license and the replication addon are required for this operation.",
                HttpStatus.SC_BAD_REQUEST, log);
        return multiStatusHolder;
    }

    private BasicStatusHolder getReplicationRequiredStatusHolder() {
        BasicStatusHolder multiStatusHolder = new BasicStatusHolder();
        multiStatusHolder.error("Error: the replication addon is required for this operation.",
                HttpStatus.SC_BAD_REQUEST, log);
        return multiStatusHolder;
    }

    @Override
    public void deployArchiveBundle(ArtifactoryRequest request, ArtifactoryResponse response, LocalRepo repo)
            throws IOException {
        response.sendError(HttpStatus.SC_BAD_REQUEST, "This REST API is available only in Artifactory Pro.", log);
    }

    /**
     * <pre>@Async</pre> implementors
     */

    @Override
    public void reindexAsync(String repoKey) {
        //noop
    }

    @Override
    public void notifyAsync(HaMessageWorkItem workItem) {
        //noop
    }

    @Override
    public void propagateReplicationListener(ReplicationOwnerModel replicationChannelModel) {
        //noop
    }

    @Override
    public void propagateRemoveReplicationListener(ReplicationOwnerModel replicationChannelModel) {
        //noop
    }

    @Override
    public void propagateReplicationEvents(String target, ReplicationEventQueueWorkItem queue) {
        //noop
    }

    @Override
    public void propagateStopSha256Migration(long sleepIntervalMillis) {
        //noop
    }

    @Override
    public void propagateDbProperties(boolean encrypt) {
        // Unsupported in OSS
    }

    @Override
    public void extractNuPkgInfo(FileInfo fileInfo, MutableStatusHolder statusHolder, boolean addToCache) {
        //noop
    }

    @Override
    public void addNuPkgToRepoCacheAsync(RepoPath repoPath, Properties properties) {
        //noop
    }

    /**
     * End @Async implementors
     **/

    @Override
    public void afterRepoInit(String repoKey) {
        //noop
    }

    @Override
    public void requestAsyncReindexNuPkgs(String repoKey) {
        //noop
    }

    @Override
    public void handleAddAfterCommit(FileInfo info, @Nullable String version) {
        //noop
    }

    @Override
    public void addPodAfterCommit(FileInfo info) {
        //noop
    }

    @Override
    public void cachePodProperties(FileInfo info) {
        //noop
    }

    @Override
    public void removePod(FileInfo info, Properties properties) {
        //noop
    }

    @Override
    public void indexRepos(List<String> repos) {
        //noop
    }

    @Override
    public String getXrayVersion() {
        return "Unavailable";
    }

    @Override
    public ConflictGuard getConflictGuard(String key) {
        return getConflictGuardProvider().getConflictGuard(key);
    }

    @Override
    public FsItemsVault getFsFileItemVault() {
        return getConflictGuardProvider().getFileLockingMap();
    }

    @Override
    public FsItemsVault getFsFolderItemVault() {
        return getConflictGuardProvider().getFolderLockingMap();
    }

    @Override
    public <T> ConflictsGuard<T> getConflictsGuard(String mapName) {
        return getConflictGuardProvider().getConflictsGuard(mapName);
    }

    @Override
    public boolean foundExpiredAndRemoteIsNewer(RepoResource remoteResource, RepoResource cachedResource) {
        return new CacheExpiryStrategyImpl().foundExpiredAndRemoteIsNewer(remoteResource, cachedResource);
    }

    @Override
    public void recalculateAll(String repoKey, boolean async) {
        // Unsupported in OSS
    }

    @Override
    public void exportTo(ExportSettings exportSettings) {
        // Unsupported in OSS
    }

    @Override
    public void importFrom(ImportSettings importSettings) {
        // Unsupported in OSS
    }
}
