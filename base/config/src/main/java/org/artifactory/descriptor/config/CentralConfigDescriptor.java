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

package org.artifactory.descriptor.config;

import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.addon.AddonSettings;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.descriptor.cleanup.CleanupConfigDescriptor;
import org.artifactory.descriptor.distribution.ReleaseBundlesConfig;
import org.artifactory.descriptor.download.DownloadRedirectConfigDescriptor;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.descriptor.eula.EulaDescriptor;
import org.artifactory.descriptor.gc.GcConfigDescriptor;
import org.artifactory.descriptor.index.IndexerDescriptor;
import org.artifactory.descriptor.mail.MailServerDescriptor;
import org.artifactory.descriptor.message.SystemMessageDescriptor;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.quota.QuotaConfigDescriptor;
import org.artifactory.descriptor.replication.GlobalReplicationsConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.signature.SignedUrlConfig;
import org.artifactory.descriptor.subscription.SubscriptionConfig;
import org.artifactory.descriptor.sumologic.SumoLogicConfigDescriptor;
import org.artifactory.descriptor.trashcan.TrashcanConfigDescriptor;
import org.jfrog.common.config.diff.DiffIgnore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Immutable interface for the central config.
 *
 * @author Yossi Shaul
 */
public interface CentralConfigDescriptor extends Descriptor {
    @DiffIgnore
    TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

    Map<String, LocalRepoDescriptor> getLocalRepositoriesMap();

    Map<String, RemoteRepoDescriptor> getRemoteRepositoriesMap();

    Map<String, VirtualRepoDescriptor> getVirtualRepositoriesMap();

    Map<String, DistributionRepoDescriptor> getDistributionRepositoriesMap();

    Map<String, ReleaseBundlesRepoDescriptor> getReleaseBundlesRepositoriesMap();

    List<ProxyDescriptor> getProxies();

    ProxyDescriptor getProxy(String proxyKey);

    List<ReverseProxyDescriptor> getReverseProxies();

    ReverseProxyDescriptor getReverseProxy(String key);

    ReverseProxyDescriptor getCurrentReverseProxy();

    String getDateFormat();

    int getFileUploadMaxSizeMb();

    long getRevision();

    List<BackupDescriptor> getBackups();

    IndexerDescriptor getIndexer();

    String getServerName();

    @Nonnull
    SecurityDescriptor getSecurity();

    /**
     * @return true if the global offline mode is set.
     */
    boolean isOfflineMode();

    boolean isHelpLinksEnabled();

    ProxyDescriptor getDefaultProxy();

    MailServerDescriptor getMailServer();

    XrayDescriptor getXrayConfig();

    List<PropertySet> getPropertySets();

    String getUrlBase();

    String getJFrogUrlBase();

    AddonSettings getAddons();

    String getLogo();

    SystemMessageDescriptor getSystemMessageConfig();

    FolderDownloadConfigDescriptor getFolderDownloadConfig();

    TrashcanConfigDescriptor getTrashcanConfig();

    SumoLogicConfigDescriptor getSumoLogicConfig();

    ReleaseBundlesConfig getReleaseBundlesConfig();

    SignedUrlConfig getSignedUrlConfig();

    String getFooter();

    List<RepoLayout> getRepoLayouts();

    RepoLayout getRepoLayout(String repoLayoutName);

    List<RemoteReplicationDescriptor> getRemoteReplications();

    List<LocalReplicationDescriptor> getLocalReplications();

    RemoteReplicationDescriptor getRemoteReplication(String replicatedRepoKey);

    LocalReplicationDescriptor getLocalReplication(String replicatedRepoKey, String replicateRepoUrl);

    LocalReplicationDescriptor getLocalReplication(String replicatedRepoKey);

    LocalReplicationDescriptor getEnabledLocalReplication(String replicatedRepoKey);

    int getTotalNumOfActiveLocalReplication(String replicatedRepoKey);

    Set<String> getReplicationKeysByRepoKey(String replicatedRepoKey);

    GcConfigDescriptor getGcConfig();

    /**
     * Normalizes the Artifactory's server URL set in the mail server config; falls back to the server URL set in the
     * general config if none is defined under the mail settings. For use within the contents of e-mails
     *
     * @return Artifactory server URL
     */
    String getServerUrlForEmail();

    CleanupConfigDescriptor getCleanupConfig();

    CleanupConfigDescriptor getVirtualCacheCleanupConfig();

    QuotaConfigDescriptor getQuotaConfig();

    Map<String, LocalReplicationDescriptor> getLocalReplicationsMap();

    Map<String, LocalReplicationDescriptor> getSingleReplicationPerRepoMap();

    Map<String, LocalReplicationDescriptor> getLocalReplicationsPerRepoMap(String repoName);

    Map<String, RemoteReplicationDescriptor> getRemoteReplicationsPerRepoMap(String repoName);

    List<String> getLocalReplicationsUniqueKeyForProperty(String repoName);

    BintrayConfigDescriptor getBintrayConfig();

    DownloadRedirectConfigDescriptor getDownloadRedirectConfig();

    List<LocalReplicationDescriptor> getMultiLocalReplications(String repoKey);

    GlobalReplicationsConfigDescriptor getReplicationsConfig();

    Map<String, BintrayApplicationConfig> getBintrayApplications();

    BintrayApplicationConfig getBintrayApplication(String bintrayApplicationKey);

    boolean isForceBaseUrl();

    EulaDescriptor getEulaConfig();

    SubscriptionConfig getSubscriptionConfig();
}
