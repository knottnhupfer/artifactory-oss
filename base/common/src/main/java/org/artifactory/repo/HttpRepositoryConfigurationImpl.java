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

package org.artifactory.repo;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.delegation.ContentSynchronisation;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.vcs.VcsGitProvider;
import org.artifactory.descriptor.repo.vcs.VcsType;
import org.artifactory.util.RepoLayoutUtils;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.artifactory.descriptor.repo.RepoType.*;
import static org.artifactory.repo.RepoDetailsType.REMOTE;

/**
 * Remote Repository configuration
 *
 * @author Tomer Cohen
 * @see org.artifactory.descriptor.repo.HttpRepoDescriptor
 */
@SuppressWarnings("unused") // setters for jackson
@JsonFilter("typeSpecificFilter")
public class HttpRepositoryConfigurationImpl extends RepositoryConfigurationBase
        implements HttpRepositoryConfiguration {

    @IncludeTypeSpecific(repoType = REMOTE)
    private String url;
    @IncludeTypeSpecific(repoType = REMOTE)
    private String username = "";
    @IncludeTypeSpecific(repoType = REMOTE)
    private String password = "";
    @IncludeTypeSpecific(repoType = REMOTE)
    private String proxy;
    @IncludeTypeSpecific(packageType = {Maven, Gradle, Ivy, SBT}, repoType = REMOTE)
    private boolean handleReleases = true;
    @IncludeTypeSpecific(packageType = {Maven, Gradle, Ivy, SBT}, repoType = REMOTE)
    private boolean handleSnapshots = true;
    @IncludeTypeSpecific(packageType = {Maven, Gradle, Ivy, SBT}, repoType = REMOTE)
    private boolean suppressPomConsistencyChecks = false;
    @IncludeTypeSpecific(packageType = {Maven, Gradle, Ivy, SBT}, repoType = REMOTE)
    private String remoteRepoChecksumPolicyType = "";
    @IncludeTypeSpecific(repoType = REMOTE)
    private boolean hardFail = false;
    @IncludeTypeSpecific(repoType = REMOTE)
    private boolean offline = false;
    @IncludeTypeSpecific(repoType = REMOTE)
    private boolean blackedOut = false;
    @IncludeTypeSpecific(repoType = REMOTE)
    private boolean storeArtifactsLocally = true;
    @IncludeTypeSpecific(repoType = REMOTE)
    private int socketTimeoutMillis = 15000;
    @IncludeTypeSpecific(repoType = REMOTE)
    private String localAddress = "";
    @IncludeTypeSpecific(repoType = REMOTE)
    private long retrievalCachePeriodSecs = 7200L;
    @IncludeTypeSpecific(repoType = REMOTE)
    private long assumedOfflinePeriodSecs = 300L;
    @IncludeTypeSpecific(repoType = REMOTE)
    private long missedRetrievalCachePeriodSecs = 1800L;
    @IncludeTypeSpecific(repoType = REMOTE)
    private int unusedArtifactsCleanupPeriodHours = 0;
    @IncludeTypeSpecific(packageType = {Maven, Gradle, Ivy, SBT}, repoType = REMOTE)
    private boolean fetchJarsEagerly = false;
    @IncludeTypeSpecific(packageType = {Maven, Gradle, Ivy, SBT}, repoType = REMOTE)
    private boolean fetchSourcesEagerly = false;
    @IncludeTypeSpecific(repoType = REMOTE)
    private boolean shareConfiguration = false;
    @IncludeTypeSpecific(repoType = REMOTE)
    private boolean synchronizeProperties = false;
    @IncludeTypeSpecific(packageType = {Maven, VCS}, repoType = REMOTE)
    private int maxUniqueSnapshots = 0;
    private int maxUniqueTags = 0;
    @IncludeTypeSpecific(repoType = REMOTE)
    private List<String> propertySets;
    @IncludeTypeSpecific(repoType = REMOTE)
    private String remoteRepoLayoutRef;
    @IncludeTypeSpecific(repoType = REMOTE)
    private boolean archiveBrowsingEnabled = false;
    @IncludeTypeSpecific(repoType = REMOTE)
    private boolean listRemoteFolderItems = true;
    @IncludeTypeSpecific(packageType = {Maven, Gradle, Ivy, SBT}, repoType = REMOTE)
    private boolean rejectInvalidJars = false;
    @IncludeTypeSpecific(repoType = REMOTE)
    private boolean allowAnyHostAuth = false;
    @IncludeTypeSpecific(repoType = REMOTE)
    private boolean enableCookieManagement = false;
    @IncludeTypeSpecific(packageType = Docker, repoType = REMOTE)
    private boolean enableTokenAuthentication = false;
    @IncludeTypeSpecific(repoType = REMOTE)
    private String queryParams;
    private boolean propagateQueryParams = false;
    @IncludeTypeSpecific(repoType = REMOTE)
    private boolean blockMismatchingMimeTypes = false;
    @IncludeTypeSpecific(repoType = REMOTE)
    private String mismatchingMimeTypesOverrideList;
    @IncludeTypeSpecific(repoType = REMOTE)
    private String clientTlsCertificate;
    @IncludeTypeSpecific(repoType = REMOTE)
    private boolean bypassHeadRequests = false;
    private BowerConfiguration bowerConfiguration = new BowerConfiguration();
    private PypiConfiguration pyPIConfiguration = new PypiConfiguration();
    private CocoaPodsConfiguration podsConfiguration = new CocoaPodsConfiguration();
    private ComposerConfiguration composerConfiguration = new ComposerConfiguration();
    private VcsConfiguration vcsConfiguration = new VcsConfiguration();
    @IncludeTypeSpecific(repoType = REMOTE)
    private ContentSynchronisation contentSynchronisation;
    private NuGetConfiguration nuGetConfiguration = new NuGetConfiguration();
    @IncludeTypeSpecific(packageType = {Bower, Docker}, repoType = REMOTE)
    private boolean externalDependenciesEnabled = false;
    @IncludeTypeSpecific(packageType = Docker, repoType = REMOTE)
    private List<String> externalDependenciesPatterns;

    public HttpRepositoryConfigurationImpl() {
        setRepoLayoutRef(RepoLayoutUtils.MAVEN_2_DEFAULT_NAME);
    }

    public HttpRepositoryConfigurationImpl(HttpRepoDescriptor repoDescriptor) {
        super(repoDescriptor, TYPE);
        try {
            new URL(repoDescriptor.getUrl());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Remote URL '" + repoDescriptor.getUrl() + "' is not valid", e);
        }
        this.url = repoDescriptor.getUrl();
        String username = repoDescriptor.getUsername();
        if (StringUtils.isNotBlank(username)) {
            setUsername(username);
        }
        String password = repoDescriptor.getPassword();
        if (StringUtils.isNotBlank(password)) {
            setPassword(password);
        }
        setAllowAnyHostAuth(repoDescriptor.isAllowAnyHostAuth());
        setEnableTokenAuthentication(repoDescriptor.isEnableTokenAuthentication());
        ProxyDescriptor proxy = repoDescriptor.getProxy();
        if (proxy != null) {
            setProxy(proxy.getKey());
        }

        setRemoteRepoChecksumPolicyType(repoDescriptor.getChecksumPolicyType().getMessage());
        setHardFail(repoDescriptor.isHardFail());
        setOffline(repoDescriptor.isOffline());
        setStoreArtifactsLocally(repoDescriptor.isStoreArtifactsLocally());
        setSocketTimeoutMillis(repoDescriptor.getSocketTimeoutMillis());
        String localAddress = repoDescriptor.getLocalAddress();
        if (StringUtils.isNotBlank(localAddress)) {
            setLocalAddress(localAddress);
        }
        setEnableCookieManagement(repoDescriptor.isEnableCookieManagement());
        setBlackedOut(repoDescriptor.isBlackedOut());
        setHandleReleases(repoDescriptor.isHandleReleases());
        setHandleSnapshots(repoDescriptor.isHandleSnapshots());
        setSuppressPomConsistencyChecks(repoDescriptor.isSuppressPomConsistencyChecks());
        setRetrievalCachePeriodSecs(repoDescriptor.getRetrievalCachePeriodSecs());
        setAssumedOfflinePeriodSecs(repoDescriptor.getAssumedOfflinePeriodSecs());
        setMissedRetrievalCachePeriodSecs(repoDescriptor.getMissedRetrievalCachePeriodSecs());
        setUnusedArtifactsCleanupPeriodHours(repoDescriptor.getUnusedArtifactsCleanupPeriodHours());
        setFetchJarsEagerly(repoDescriptor.isFetchJarsEagerly());
        setFetchSourcesEagerly(repoDescriptor.isFetchSourcesEagerly());
        setShareConfiguration(repoDescriptor.isShareConfiguration());
        setMaxUniqueSnapshots(repoDescriptor.getMaxUniqueSnapshots());
        setMaxUniqueTags(repoDescriptor.getMaxUniqueTags());
        setSynchronizeProperties(repoDescriptor.isSynchronizeProperties());
        setContentSynchronisation(repoDescriptor.getContentSynchronisation());
        setPropagateQueryParams(repoDescriptor.isPropagateQueryParams());
        setClientTlsCertificate(repoDescriptor.getClientTlsCertificate());
        List<PropertySet> propertySets = repoDescriptor.getPropertySets();
        if (propertySets != null && !propertySets.isEmpty()) {
            setPropertySets(Lists.transform(propertySets, new Function<PropertySet, String>() {
                @Override
                public String apply(@Nonnull PropertySet input) {
                    return input.getName();
                }
            }));
        } else {
            setPropertySets(Lists.<String>newArrayList());
        }
        RepoLayout remoteRepoLayout = repoDescriptor.getRemoteRepoLayout();
        if (remoteRepoLayout != null) {
            setRemoteRepoLayoutRef(remoteRepoLayout.getName());
        }
        setArchiveBrowsingEnabled(repoDescriptor.isArchiveBrowsingEnabled());
        this.xrayRepoConfig = repoDescriptor.getXrayConfig();
        this.downloadRedirectConfig = repoDescriptor.getDownloadRedirectConfig();
        setListRemoteFolderItems(repoDescriptor.isListRemoteFolderItems());
        setRejectInvalidJars(repoDescriptor.isRejectInvalidJars());
        this.bowerConfiguration = repoDescriptor.getBower();
        this.podsConfiguration = repoDescriptor.getCocoaPods();
        this.pyPIConfiguration = repoDescriptor.getPypi();
        this.composerConfiguration = repoDescriptor.getComposer();
        this.vcsConfiguration = repoDescriptor.getVcs();
        this.nuGetConfiguration = repoDescriptor.getNuget();
        setBlockMismatchingMimeTypes(repoDescriptor.isBlockMismatchingMimeTypes());
        setMismatchingMimeTypesOverrideList(repoDescriptor.getMismatchingMimeTypesOverrideList());
        setBypassHeadRequests(repoDescriptor.isBypassHeadRequests());
        setQueryParams(repoDescriptor.getQueryParams());
        if (repoDescriptor.getExternalDependencies() != null) {
            ExternalDependenciesConfig externalDependencies = repoDescriptor.getExternalDependencies();
            setExternalDependenciesEnabled(externalDependencies.isEnabled());
            setExternalDependenciesPatterns(externalDependencies.getPatterns());
        }
    }

    @Override
    public int getMaxUniqueSnapshots() {
        return maxUniqueSnapshots;
    }

    public void setMaxUniqueSnapshots(int maxUniqueSnapshots) {
        this.maxUniqueSnapshots = maxUniqueSnapshots;
    }

    @Override
    public int getMaxUniqueTags() {
        return maxUniqueTags;
    }

    public void setMaxUniqueTags(int maxUniqueTags) {
        this.maxUniqueTags = maxUniqueTags;
    }

    @Override
    public boolean isSuppressPomConsistencyChecks() {
        return suppressPomConsistencyChecks;
    }

    public void setSuppressPomConsistencyChecks(boolean suppressPomConsistencyChecks) {
        this.suppressPomConsistencyChecks = suppressPomConsistencyChecks;
    }

    @Override
    public boolean isHandleReleases() {
        return handleReleases;
    }

    public void setHandleReleases(boolean handleReleases) {
        this.handleReleases = handleReleases;
    }

    @Override
    public boolean isHandleSnapshots() {
        return handleSnapshots;
    }

    public void setHandleSnapshots(boolean handleSnapshots) {
        this.handleSnapshots = handleSnapshots;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean isBlackedOut() {
        return blackedOut;
    }

    public void setBlackedOut(boolean blackedOut) {
        this.blackedOut = blackedOut;
    }

    @Override
    public long getAssumedOfflinePeriodSecs() {
        return assumedOfflinePeriodSecs;
    }

    public void setAssumedOfflinePeriodSecs(long assumedOfflinePeriodSecs) {
        this.assumedOfflinePeriodSecs = assumedOfflinePeriodSecs;
    }

    @Override
    public boolean isFetchJarsEagerly() {
        return fetchJarsEagerly;
    }

    public void setFetchJarsEagerly(boolean fetchJarsEagerly) {
        this.fetchJarsEagerly = fetchJarsEagerly;
    }

    @Override
    public boolean isFetchSourcesEagerly() {
        return fetchSourcesEagerly;
    }

    public void setFetchSourcesEagerly(boolean fetchSourcesEagerly) {
        this.fetchSourcesEagerly = fetchSourcesEagerly;
    }

    @Override
    public boolean isHardFail() {
        return hardFail;
    }

    public void setHardFail(boolean hardFail) {
        this.hardFail = hardFail;
    }

    @Override
    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    @Override
    public long getMissedRetrievalCachePeriodSecs() {
        return missedRetrievalCachePeriodSecs;
    }

    public void setMissedRetrievalCachePeriodSecs(long missedRetrievalCachePeriodSecs) {
        this.missedRetrievalCachePeriodSecs = missedRetrievalCachePeriodSecs;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public List<String> getPropertySets() {
        return propertySets;
    }

    public void setPropertySets(List<String> propertySets) {
        this.propertySets = propertySets;
    }

    @Override
    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    @Override
    public String getRemoteRepoChecksumPolicyType() {
        return remoteRepoChecksumPolicyType;
    }

    public void setRemoteRepoChecksumPolicyType(String remoteRepoChecksumPolicyType) {
        this.remoteRepoChecksumPolicyType = remoteRepoChecksumPolicyType;
    }

    @Override
    public long getRetrievalCachePeriodSecs() {
        return retrievalCachePeriodSecs;
    }

    public void setRetrievalCachePeriodSecs(long retrievalCachePeriodSecs) {
        this.retrievalCachePeriodSecs = retrievalCachePeriodSecs;
    }

    @Override
    public boolean isShareConfiguration() {
        return shareConfiguration;
    }

    public void setShareConfiguration(boolean shareConfiguration) {
        this.shareConfiguration = shareConfiguration;
    }

    @Override
    public int getSocketTimeoutMillis() {
        return socketTimeoutMillis;
    }

    public void setSocketTimeoutMillis(int socketTimeoutMillis) {
        this.socketTimeoutMillis = socketTimeoutMillis;
    }

    @Override
    public boolean isStoreArtifactsLocally() {
        return storeArtifactsLocally;
    }

    public void setStoreArtifactsLocally(boolean storeArtifactsLocally) {
        this.storeArtifactsLocally = storeArtifactsLocally;
    }

    @Override
    public boolean isSynchronizeProperties() {
        return synchronizeProperties;
    }

    public void setSynchronizeProperties(boolean synchronizeProperties) {
        this.synchronizeProperties = synchronizeProperties;
    }

    @Override
    public int getUnusedArtifactsCleanupPeriodHours() {
        return unusedArtifactsCleanupPeriodHours;
    }

    public void setUnusedArtifactsCleanupPeriodHours(int unusedArtifactsCleanupPeriodHours) {
        this.unusedArtifactsCleanupPeriodHours = unusedArtifactsCleanupPeriodHours;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getRemoteRepoLayoutRef() {
        return remoteRepoLayoutRef;
    }

    public void setRemoteRepoLayoutRef(String remoteRepoLayoutRef) {
        this.remoteRepoLayoutRef = remoteRepoLayoutRef;
    }

    @Override
    public boolean isArchiveBrowsingEnabled() {
        return archiveBrowsingEnabled;
    }

    public void setArchiveBrowsingEnabled(boolean archiveBrowsingEnabled) {
        this.archiveBrowsingEnabled = archiveBrowsingEnabled;
    }

    @IncludeTypeSpecific(repoType = REMOTE)
    @Override
    public boolean isXrayIndex() {
        return xrayRepoConfig != null && xrayRepoConfig.isEnabled();
    }

    public void setXrayIndex(boolean xrayIndex) {
        getLazyXrayConfig().setEnabled(xrayIndex);
    }

    @IncludeTypeSpecific(repoType = REMOTE)
    @Override
    public boolean isDownloadRedirect() {
        return downloadRedirectConfig != null && downloadRedirectConfig.isEnabled();
    }

    public void setDownloadRedirect(boolean enableDownloadRedirect) {
        getLazyDownloadRedirectConfig().setEnabled(enableDownloadRedirect);
    }

    @Override
    public boolean isListRemoteFolderItems() {
        return listRemoteFolderItems;
    }

    public void setListRemoteFolderItems(boolean listRemoteFolderItems) {
        this.listRemoteFolderItems = listRemoteFolderItems;
    }

    @Override
    public boolean isRejectInvalidJars() {
        return rejectInvalidJars;
    }

    public void setRejectInvalidJars(boolean rejectInvalidJars) {
        this.rejectInvalidJars = rejectInvalidJars;
    }

    @Override
    public boolean isAllowAnyHostAuth() {
        return allowAnyHostAuth;
    }

    public void setAllowAnyHostAuth(boolean allowAnyHostAuth) {
        this.allowAnyHostAuth = allowAnyHostAuth;
    }

    @Override
    public boolean isEnableTokenAuthentication() {
        return enableTokenAuthentication;
    }

    public void setEnableTokenAuthentication(boolean enableTokenAuthentication) {
        this.enableTokenAuthentication = enableTokenAuthentication;
    }

    @Override
    public String getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    @Override
    public boolean isEnableCookieManagement() {
        return enableCookieManagement;
    }

    public void setEnableCookieManagement(boolean enableCookieManagement) {
        this.enableCookieManagement = enableCookieManagement;
    }

    @IncludeTypeSpecific(packageType = Bower, repoType = REMOTE)
    @Override
    public String getBowerRegistryUrl() {
        return this.bowerConfiguration != null ? this.bowerConfiguration.getBowerRegistryUrl() : null;
    }

    @IncludeTypeSpecific(packageType = NuGet, repoType = REMOTE)
    @Override
    public String getDownloadContextPath(){
        return this.nuGetConfiguration != null ? this.nuGetConfiguration.getDownloadContextPath() : null;
    }

    @IncludeTypeSpecific(packageType = NuGet, repoType = REMOTE)
    @Override
    public String getFeedContextPath(){
        return this.nuGetConfiguration != null ? this.nuGetConfiguration.getFeedContextPath() : null;
    }

    @IncludeTypeSpecific(packageType = NuGet, repoType = REMOTE)
    @Override
    public String getV3FeedUrl(){
        return this.nuGetConfiguration != null ? this.nuGetConfiguration.getV3FeedUrl() : null;
    }

    @IncludeTypeSpecific(packageType = Composer, repoType = REMOTE)
    @Override
    public String getComposerRegistryUrl() {
        return this.composerConfiguration != null ? this.composerConfiguration.getComposerRegistryUrl() : null;
    }

    @IncludeTypeSpecific(packageType = CocoaPods, repoType = REMOTE)
    @Override
    public String getPodsSpecsRepoUrl() {
        return this.podsConfiguration != null ? this.podsConfiguration.getCocoaPodsSpecsRepoUrl() : null;
    }

    @IncludeTypeSpecific(packageType = Pypi, repoType = REMOTE)
    @Override
    public String getPyPIRegistryUrl() {
        return this.pyPIConfiguration != null ? this.pyPIConfiguration.getPyPIRegistryUrl() : null;
    }

    @IncludeTypeSpecific(packageType = Pypi, repoType = REMOTE)
    @Override
    public String getPyPIRepositorySuffix() {
        return this.pyPIConfiguration != null ? this.pyPIConfiguration.getRepositorySuffix() : null;
    }

    @IncludeTypeSpecific(packageType = {VCS, Bower, Go, CocoaPods, Composer}, repoType = REMOTE)
    @Override
    public String getVcsType() {
        return this.vcsConfiguration != null ? this.vcsConfiguration.getType().name() : null;
    }

    @IncludeTypeSpecific(packageType = {VCS, Bower, Go, CocoaPods, Composer}, repoType = REMOTE)
    @Override
    public String getVcsGitProvider() {
        return this.vcsConfiguration != null ? this.vcsConfiguration.getGit().getProvider().name() : null;
    }

    @IncludeTypeSpecific(packageType = {VCS, Bower, Go, CocoaPods, Composer}, repoType = REMOTE)
    @Override
    public String getVcsGitDownloadUrl() {
        return this.vcsConfiguration != null ? this.vcsConfiguration.getGit().getDownloadUrl() : null;
    }

    // Keep these setters for jackson
    public void setBowerRegistryUrl(String bowerRegistryUrl) {
        this.bowerConfiguration.setBowerRegistryUrl(bowerRegistryUrl);
    }

    public void setPyPIRegistryUrl(String pyPIRegistryUrl) {
        this.pyPIConfiguration.setPyPIRegistryUrl(pyPIRegistryUrl);
    }

    public void setPyPIRepositorySuffix(String repositorySuffix) {
        this.pyPIConfiguration.setRepositorySuffix(repositorySuffix);
    }

    public void setComposerRegistryUrl(String composerRegistryUrl) {
        this.composerConfiguration.setComposerRegistryUrl(composerRegistryUrl);
    }

    public void setPodsSpecsRepoUrl(String podsSpecsRepoUrl) {
        this.podsConfiguration.setCocoaPodsSpecsRepoUrl(podsSpecsRepoUrl);
    }

    public void setVcsType(String vcsType) {
        this.vcsConfiguration.setType(VcsType.valueOf(vcsType));
    }

    public void setVcsGitProvider(String gitProvider) {
        this.vcsConfiguration.getGit().setProvider(VcsGitProvider.valueOf(gitProvider));
    }

    public void setVcsGitDownloadUrl(String vcsGitDownloadUrl) {
        this.vcsConfiguration.getGit().setDownloadUrl(vcsGitDownloadUrl);
    }

    public void setDownloadContextPath(String downloadContextPath) {
        this.nuGetConfiguration.setDownloadContextPath(downloadContextPath);
    }

    public void setFeedContextPath(String feedContextPath) {
        this.nuGetConfiguration.setFeedContextPath(feedContextPath);
    }

    public void setV3FeedUrl(String v3FeedUrl) {
        this.nuGetConfiguration.setV3FeedUrl(v3FeedUrl);
    }

    public ContentSynchronisation getContentSynchronisation() {
        return contentSynchronisation;
    }

    public void setContentSynchronisation(ContentSynchronisation contentSynchronisation) {
        this.contentSynchronisation = contentSynchronisation;
    }

    public boolean isPropagateQueryParams() {
        return propagateQueryParams;
    }

    public void setPropagateQueryParams(boolean propagateQueryParams) {
        this.propagateQueryParams = propagateQueryParams;
    }

    @Override
    public boolean isBlockMismatchingMimeTypes() {
        return blockMismatchingMimeTypes;
    }

    public void setBlockMismatchingMimeTypes(boolean blockMismatchingMimeTypes) {
        this.blockMismatchingMimeTypes = blockMismatchingMimeTypes;
    }

    public void setBypassHeadRequests(boolean bypassHeadRequests) {
        this.bypassHeadRequests = bypassHeadRequests;
    }

    public boolean getBypassHeadRequests() {
        return this.bypassHeadRequests;
    }

    @Override
    public String getMismatchingMimeTypesOverrideList() {
        return mismatchingMimeTypesOverrideList;
    }

    @Override
    public String getClientTlsCertificate() {
        return this.clientTlsCertificate;
    }

    public void setClientTlsCertificate(String clientTlsCertificate) {
        this.clientTlsCertificate = clientTlsCertificate;
    }

    public void setMismatchingMimeTypesOverrideList(String mismatchingMimeTypesOverrideList) {
        this.mismatchingMimeTypesOverrideList = mismatchingMimeTypesOverrideList;
    }

    @Override
    public boolean isExternalDependenciesEnabled() {
        return externalDependenciesEnabled;
    }

    public void setExternalDependenciesEnabled(boolean externalDependenciesEnabled) {
        this.externalDependenciesEnabled = externalDependenciesEnabled;
    }

    @Override
    public List<String> getExternalDependenciesPatterns() {
        return externalDependenciesPatterns;
    }

    public void setExternalDependenciesPatterns(List<String> externalDependenciesPatterns) {
        this.externalDependenciesPatterns = externalDependenciesPatterns;
    }
}
