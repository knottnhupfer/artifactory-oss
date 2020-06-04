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

package org.artifactory.storage.db.servers.model;

import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.state.ArtifactoryServerState;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * Artifactory server information model
 *
 * @author Shay Bagants
 */
public class ArtifactoryServerInfo {

    private ArtifactoryServer artifactoryServer;
    private int staleHeartbeatSeconds;

    public ArtifactoryServerInfo(ArtifactoryServer artifactoryServer, int staleHeartbeatSeconds) {
        this.artifactoryServer = artifactoryServer;
        this.staleHeartbeatSeconds = staleHeartbeatSeconds;
    }

    public boolean isRunning() {
        return artifactoryServer.getServerState() == ArtifactoryServerState.RUNNING;
    }

    public boolean isStarting() {
        return artifactoryServer.getServerState() == ArtifactoryServerState.STARTING;
    }

    public boolean isStopping() {
        return artifactoryServer.getServerState() == ArtifactoryServerState.STOPPING;
    }

    public boolean isConverting() {
        return artifactoryServer.getServerState() == ArtifactoryServerState.CONVERTING;
    }

    public boolean isActive(int customStaleHeartbeatSecs){
        return !isStaleHeartbeat(customStaleHeartbeatSecs) && (isRunning() || isStopping() || isConverting() || isStarting());
    }

    public boolean isActive() {
        return isActive(staleHeartbeatSeconds);
    }

    public boolean isPrimary() {
        return artifactoryServer.getServerRole() == ArtifactoryServerRole.PRIMARY;
    }

    public boolean isOther(String serverId) {
        return !artifactoryServer.getServerId().trim().equals(serverId);
    }

    boolean isStaleHeartbeat(int staleTimeSec) {
        return TimeUnit.MILLISECONDS.toSeconds(
                System.currentTimeMillis() - artifactoryServer.getLastHeartbeat()) > staleTimeSec;
    }

    //delegated methods
    @Nonnull
    public String getServerId() {
        return artifactoryServer.getServerId();
    }

    public long getStartTime() {
        return artifactoryServer.getStartTime();
    }

    public String getContextUrl() {
        return artifactoryServer.getContextUrl();
    }

    public int getMembershipPort() {
        return artifactoryServer.getMembershipPort();
    }

    public ArtifactoryServerState getServerState() {
        return artifactoryServer.getServerState();
    }

    public ArtifactoryServerRole getServerRole() {
        return artifactoryServer.getServerRole();
    }

    public long getLastHeartbeat() {
        return artifactoryServer.getLastHeartbeat();
    }

    public String getArtifactoryVersion() {
        return artifactoryServer.getArtifactoryVersion();
    }

    public long getArtifactoryRevision() {
        return artifactoryServer.getArtifactoryRevision();
    }

    public long getArtifactoryRelease() {
        return artifactoryServer.getArtifactoryRelease();
    }

    public String getLicenseKeyHash() {
        return artifactoryServer.getLicenseKeyHash();
    }

    public ArtifactoryRunningMode getArtifactoryRunningMode() {
        return artifactoryServer.getArtifactoryRunningMode();
    }
}
