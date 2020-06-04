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

package org.artifactory.descriptor.repo;

import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.delegation.ContentSynchronisation;
import org.jfrog.common.config.diff.DiffElement;
import org.jfrog.common.config.diff.DiffReference;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Fred Simon
 */
@XmlType(name = "RemoteRepoBaseType", propOrder = {"url", "offline", "hardFail", "storeArtifactsLocally",
        "fetchJarsEagerly", "fetchSourcesEagerly", "externalDependencies", "retrievalCachePeriodSecs", "assumedOfflinePeriodSecs",
        "missedRetrievalCachePeriodSecs", "checksumPolicyType",
        "unusedArtifactsCleanupPeriodHours", "shareConfiguration", "synchronizeProperties", "listRemoteFolderItems",
        "remoteRepoLayout", "rejectInvalidJars", "nuget", "pypi", "bower", "cocoaPods", "p2OriginalUrl", "vcs",
        "contentSynchronisation", "blockMismatchingMimeTypes", "mismatchingMimeTypesOverrideList", "bypassHeadRequests", "composer"},
        namespace = Descriptor.NS)
@GenerateDiffFunction(defaultImpl = HttpRepoDescriptor.class)
public abstract class RemoteRepoDescriptor extends RealRepoDescriptor {

    @XmlElement(defaultValue = "true", required = false)
    protected boolean storeArtifactsLocally = true;
    @XmlElement(defaultValue = "false", required = false)
    protected boolean fetchJarsEagerly = false;
    @XmlElement(defaultValue = "false", required = false)
    protected boolean fetchSourcesEagerly = false;
    @XmlElement(required = false)
    private ExternalDependenciesConfig externalDependencies;
    @XmlElement(defaultValue = "false", required = false)
    protected boolean shareConfiguration = false;
    @XmlElement(required = true)
    private String url;
    @XmlElement(defaultValue = "false", required = false)
    private boolean hardFail;
    @XmlElement(defaultValue = "false", required = false)
    private boolean offline;
    @XmlElement(defaultValue = "600", required = false)
    private long retrievalCachePeriodSecs = 7200;//2 hr.
    @XmlElement(defaultValue = "300", required = false)
    private long assumedOfflinePeriodSecs = 300;   //5 minutes
    @XmlElement(defaultValue = "1800", required = false)
    private long missedRetrievalCachePeriodSecs = 1800;//30 min.
    @XmlElement(name = "remoteRepoChecksumPolicyType", defaultValue = "generate-if-absent", required = false)
    @DiffElement(name = "remoteRepoChecksumPolicyType")
    private ChecksumPolicyType checksumPolicyType = ChecksumPolicyType.GEN_IF_ABSENT;
    @XmlElement(defaultValue = "0", required = false)
    private int unusedArtifactsCleanupPeriodHours = 0;
    @XmlElement(defaultValue = "false", required = false)
    private boolean synchronizeProperties;
    @XmlElement(defaultValue = "true", required = false)
    private boolean listRemoteFolderItems = true;
    @XmlElement(defaultValue = "false", required = false)
    private boolean bypassHeadRequests;

    @XmlIDREF
    @XmlElement(name = "remoteRepoLayoutRef")
    @DiffReference
    private RepoLayout remoteRepoLayout;

    @XmlElement(defaultValue = "false", required = false)
    private boolean rejectInvalidJars;

    @XmlElement(required = false)
    private String p2OriginalUrl;

    private NuGetConfiguration nuget;

    private PypiConfiguration pypi;

    private VcsConfiguration vcs;

    private BowerConfiguration bower;

    private ComposerConfiguration composer;

    private CocoaPodsConfiguration cocoaPods;

    @XmlElement(name = "contentSynchronisation", required = false)
    private ContentSynchronisation contentSynchronisation;

    @XmlElement(defaultValue = "true", required = true)
    private boolean blockMismatchingMimeTypes = true;

    @XmlElement(required = false)
    private String mismatchingMimeTypesOverrideList;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isHardFail() {
        return hardFail;
    }

    public void setHardFail(boolean hardFail) {
        this.hardFail = hardFail;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public long getRetrievalCachePeriodSecs() {
        return retrievalCachePeriodSecs;
    }

    public void setRetrievalCachePeriodSecs(long retrievalCachePeriodSecs) {
        this.retrievalCachePeriodSecs = retrievalCachePeriodSecs;
    }

    public long getAssumedOfflinePeriodSecs() {
        return assumedOfflinePeriodSecs;
    }

    public void setAssumedOfflinePeriodSecs(long assumedOfflinePeriodSecs) {
        this.assumedOfflinePeriodSecs = assumedOfflinePeriodSecs;
    }

    public long getMissedRetrievalCachePeriodSecs() {
        return missedRetrievalCachePeriodSecs;
    }

    public void setMissedRetrievalCachePeriodSecs(long missedRetrievalCachePeriodSecs) {
        this.missedRetrievalCachePeriodSecs = missedRetrievalCachePeriodSecs;
    }

    public boolean isStoreArtifactsLocally() {
        return storeArtifactsLocally;
    }

    public void setStoreArtifactsLocally(boolean storeArtifactsLocally) {
        this.storeArtifactsLocally = storeArtifactsLocally;
    }

    public boolean isFetchJarsEagerly() {
        return fetchJarsEagerly;
    }

    public void setFetchJarsEagerly(boolean fetchJarsEagerly) {
        this.fetchJarsEagerly = fetchJarsEagerly;
    }

    public boolean isFetchSourcesEagerly() {
        return fetchSourcesEagerly;
    }

    public void setFetchSourcesEagerly(boolean fetchSourcesEagerly) {
        this.fetchSourcesEagerly = fetchSourcesEagerly;
    }

    public ExternalDependenciesConfig getExternalDependencies() {
        return externalDependencies;
    }

    public void setExternalDependencies(ExternalDependenciesConfig externalDependencies) {
        this.externalDependencies = externalDependencies;
    }

    public ChecksumPolicyType getChecksumPolicyType() {
        return checksumPolicyType;
    }

    public void setChecksumPolicyType(ChecksumPolicyType checksumPolicyType) {
        this.checksumPolicyType = checksumPolicyType;
    }

    public int getUnusedArtifactsCleanupPeriodHours() {
        return unusedArtifactsCleanupPeriodHours;
    }

    public void setUnusedArtifactsCleanupPeriodHours(int unusedArtifactsCleanupPeriodHours) {
        this.unusedArtifactsCleanupPeriodHours = unusedArtifactsCleanupPeriodHours;
    }

    public boolean isShareConfiguration() {
        return shareConfiguration;
    }

    public void setShareConfiguration(boolean shareConfiguration) {
        this.shareConfiguration = shareConfiguration;
    }

    public boolean isSynchronizeProperties() {
        return synchronizeProperties;
    }

    public void setSynchronizeProperties(boolean synchronizeProperties) {
        this.synchronizeProperties = synchronizeProperties;
    }

    public boolean isListRemoteFolderItems() {
        return listRemoteFolderItems;
    }

    public void setListRemoteFolderItems(boolean listRemoteFolderItems) {
        this.listRemoteFolderItems = listRemoteFolderItems;
    }

    public RepoLayout getRemoteRepoLayout() {
        return remoteRepoLayout;
    }

    public void setRemoteRepoLayout(RepoLayout remoteRepoLayout) {
        this.remoteRepoLayout = remoteRepoLayout;
    }

    public boolean isRejectInvalidJars() {
        return rejectInvalidJars;
    }

    public void setRejectInvalidJars(boolean rejectInvalidJars) {
        this.rejectInvalidJars = rejectInvalidJars;
    }

    public String getP2OriginalUrl() {
        return p2OriginalUrl;
    }

    public void setP2OriginalUrl(String p2OriginalUrl) {
        this.p2OriginalUrl = p2OriginalUrl;
    }

    public NuGetConfiguration getNuget() {
        return nuget;
    }

    public void setNuget(NuGetConfiguration nuget) {
        this.nuget = nuget;
    }

    public PypiConfiguration getPypi() {
        return pypi;
    }

    public void setPypi(PypiConfiguration pypi) {
        this.pypi = pypi;
    }

    public VcsConfiguration getVcs() {
        return vcs;
    }

    public void setVcs(VcsConfiguration vcs) {
        this.vcs = vcs;
    }

    public BowerConfiguration getBower() {
        return bower;
    }

    public void setBower(BowerConfiguration bower) {
        this.bower = bower;
    }

    public ComposerConfiguration getComposer() {
        return composer;
    }

    public void setComposer(ComposerConfiguration composer) {
        this.composer = composer;
    }

    public CocoaPodsConfiguration getCocoaPods() {
        return cocoaPods;
    }

    public void setCocoaPods(CocoaPodsConfiguration cocoaPods) {
        this.cocoaPods = cocoaPods;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isCache() {
        return false;
    }

    /**
     * Returns ContentForwarding configuration
     *
     * @return {@link ContentSynchronisation}
     */
    public ContentSynchronisation getContentSynchronisation() {
        if(contentSynchronisation == null) {
            contentSynchronisation = new ContentSynchronisation();
        }
        return contentSynchronisation;
    }

    /**
     * Sets {@link ContentSynchronisation} configuration (used for UI mapping purposes)
     */
    public void setContentSynchronisation(ContentSynchronisation contentSynchronisation) {
        this.contentSynchronisation = contentSynchronisation;
    }

    /**
     * A list of blocked mime types that overrides the defaults set in the system properties
     */
    public String getMismatchingMimeTypesOverrideList() {
        return mismatchingMimeTypesOverrideList;
    }

    public void setMismatchingMimeTypesOverrideList(String mismatchingMimeTypesOverrideList) {
        this.mismatchingMimeTypesOverrideList = mismatchingMimeTypesOverrideList;
    }

    /**
     * This repo blocks all mimetypes that are set in the system properties in cases where the requested mime type
     * varies from the one returned by the remote. i.e. the client requested application/json but remote returns
     * text/html --> the download will be blocked.
     */
    public boolean isBlockMismatchingMimeTypes() {
        return blockMismatchingMimeTypes;
    }

    public void setBlockMismatchingMimeTypes(boolean blockMismatchingMimeTypes) {
        this.blockMismatchingMimeTypes = blockMismatchingMimeTypes;
    }

    public void setBypassHeadRequests(boolean bypassHeadRequests) {
        this.bypassHeadRequests = bypassHeadRequests;
    }

    public boolean isBypassHeadRequests() {
        return bypassHeadRequests;
    }

    public String getPypiRepositorySuffix() {
        if (getPypi() == null) {
            setPypi(new PypiConfiguration());
        }
        return getPypi().getRepositorySuffix();
    }
}