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

package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.descriptor.repo.*;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.rest.common.util.JsonUtil;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.artifactory.repo.config.RepoConfigDefaultValues.*;

/**
 * @author Dan Feldman
 */
public class MavenTypeSpecificConfigModel implements TypeSpecificConfigModel {

    //local
    protected Integer maxUniqueSnapshots = DEFAULT_MAX_UNIQUE_SNAPSHOTS;
    protected Boolean handleReleases = DEFAULT_HANDLE_RELEASES;
    protected Boolean handleSnapshots = DEFAULT_HANDLE_SNAPSHOTS;
    protected Boolean suppressPomConsistencyChecks = DEFAULT_SUPPRESS_POM_CHECKS_MAVEN;
    protected SnapshotVersionBehavior snapshotVersionBehavior = DEFAULT_SNAPSHOT_BEHAVIOR;
    protected LocalRepoChecksumPolicyType localChecksumPolicy = DEFAULT_CHECKSUM_POLICY;

    //remote
    protected Boolean eagerlyFetchJars = DEFAULT_EAGERLY_FETCH_JARS;
    protected Boolean eagerlyFetchSources = DEFAULT_EAGERLY_FETCH_SOURCES;
    protected ChecksumPolicyType remoteChecksumPolicy = DEFAULT_REMOTE_CHECKSUM_POLICY;
    protected Boolean listRemoteFolderItems = DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE;
    protected Boolean rejectInvalidJars = DEFAULT_REJECT_INVALID_JARS;

    //virtual
    protected PomCleanupPolicy pomCleanupPolicy = DEFAULT_POM_CLEANUP_POLICY;
    protected String keyPair;
    protected Boolean forceMavenAuthentication = DEFAULT_FORCE_MAVEN_AUTH;

    public Integer getMaxUniqueSnapshots() {
        return maxUniqueSnapshots;
    }

    public void setMaxUniqueSnapshots(Integer maxUniqueSnapshots) {
        this.maxUniqueSnapshots = maxUniqueSnapshots;
    }

    public Boolean getHandleReleases() {
        return handleReleases;
    }

    public void setHandleReleases(Boolean handleReleases) {
        this.handleReleases = handleReleases;
    }

    public Boolean getHandleSnapshots() {
        return handleSnapshots;
    }

    public void setHandleSnapshots(Boolean handleSnapshots) {
        this.handleSnapshots = handleSnapshots;
    }

    public Boolean getSuppressPomConsistencyChecks() {
        return suppressPomConsistencyChecks;
    }

    public void setSuppressPomConsistencyChecks(Boolean suppressPomConsistencyChecks) {
        this.suppressPomConsistencyChecks = suppressPomConsistencyChecks;
    }

    public SnapshotVersionBehavior getSnapshotVersionBehavior() {
        return snapshotVersionBehavior;
    }

    public void setSnapshotVersionBehavior(SnapshotVersionBehavior snapshotVersionBehavior) {
        this.snapshotVersionBehavior = snapshotVersionBehavior;
    }

    public LocalRepoChecksumPolicyType getLocalChecksumPolicy() {
        return localChecksumPolicy;
    }

    public void setLocalChecksumPolicy(LocalRepoChecksumPolicyType localChecksumPolicy) {
        this.localChecksumPolicy = localChecksumPolicy;
    }

    public Boolean getEagerlyFetchJars() {
        return eagerlyFetchJars;
    }

    public void setEagerlyFetchJars(Boolean eagerlyFetchJars) {
        this.eagerlyFetchJars = eagerlyFetchJars;
    }

    public Boolean getEagerlyFetchSources() {
        return eagerlyFetchSources;
    }

    public void setEagerlyFetchSources(Boolean eagerlyFetchSources) {
        this.eagerlyFetchSources = eagerlyFetchSources;
    }

    public ChecksumPolicyType getRemoteChecksumPolicy() {
        return remoteChecksumPolicy;
    }

    public void setRemoteChecksumPolicy(ChecksumPolicyType remoteChecksumPolicy) {
        this.remoteChecksumPolicy = remoteChecksumPolicy;
    }

    public Boolean isListRemoteFolderItems() {
        return listRemoteFolderItems;
    }

    public void setListRemoteFolderItems(Boolean listRemoteFolderItems) {
        this.listRemoteFolderItems = listRemoteFolderItems;
    }

    public Boolean getRejectInvalidJars() {
        return rejectInvalidJars;
    }

    public void setRejectInvalidJars(Boolean rejectInvalidJars) {
        this.rejectInvalidJars = rejectInvalidJars;
    }

    public void setPomCleanupPolicy(PomCleanupPolicy pomCleanupPolicy) {
        this.pomCleanupPolicy = pomCleanupPolicy;
    }

    public PomCleanupPolicy getPomCleanupPolicy() {
        return pomCleanupPolicy;
    }

    public String getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(String keyPair) {
        this.keyPair = keyPair;
    }

    public Boolean isForceMavenAuthentication() {
        return forceMavenAuthentication;
    }

    public void setForceMavenAuthentication(Boolean forceMavenAuthentication) {
        this.forceMavenAuthentication = forceMavenAuthentication;
    }

    public void validateSharedTypeSpecific() {
        if (getRepoType() != RepoType.Maven) {
            //Maven types suppress pom checks by default, maven does not
            setSuppressPomConsistencyChecks(
                    Optional.ofNullable(getSuppressPomConsistencyChecks()).orElse(DEFAULT_SUPPRESS_POM_CHECKS));
        }
        setMaxUniqueSnapshots(Optional.ofNullable(getMaxUniqueSnapshots()).orElse(DEFAULT_MAX_UNIQUE_SNAPSHOTS));
        setHandleReleases(Optional.ofNullable(getHandleReleases()).orElse(DEFAULT_HANDLE_RELEASES));
        setHandleSnapshots(Optional.ofNullable(getHandleSnapshots()).orElse(DEFAULT_HANDLE_SNAPSHOTS));
        setSuppressPomConsistencyChecks(Optional.ofNullable(getSuppressPomConsistencyChecks())
                .orElse(DEFAULT_SUPPRESS_POM_CHECKS));
    }

    @Override
    public void validateRemoteTypeSpecific() {
        setEagerlyFetchJars(ofNullable(getEagerlyFetchJars()).orElse(DEFAULT_EAGERLY_FETCH_JARS));
        setEagerlyFetchSources(ofNullable(getEagerlyFetchSources()).orElse(DEFAULT_EAGERLY_FETCH_SOURCES));
        setListRemoteFolderItems(ofNullable(isListRemoteFolderItems())
                .orElse(DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE));
    }

    @Override
    public void validateVirtualTypeSpecific(AddonsManager addonsManager) throws RepoConfigException {
        if (getRepoType() == RepoType.Maven) {
            // just for maven, not for inheritors
            setForceMavenAuthentication(
                    ofNullable(isForceMavenAuthentication()).orElse(DEFAULT_FORCE_MAVEN_AUTH));
        }
        setPomCleanupPolicy(ofNullable(getPomCleanupPolicy()).orElse(DEFAULT_POM_CLEANUP_POLICY));
        if (getKeyPair() != null && !addonsManager.addonByType(ArtifactWebstartAddon.class)
                .getKeyPairNames().contains(getKeyPair())) {
            throw new RepoConfigException("Keypair '" + getKeyPair() + "' doesn't exist", SC_NOT_FOUND);
        }
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.Maven;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
