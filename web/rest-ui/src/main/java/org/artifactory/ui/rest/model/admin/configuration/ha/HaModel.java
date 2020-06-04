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

package org.artifactory.ui.rest.model.admin.configuration.ha;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;

/**
 * @author Chen Keinan
 */
public class HaModel {

    private String id;
    private String startTime;
    private String url;
    private int memberShipPort;
    private String state;
    private String role;
    private String lastHeartbeat;
    private String version;
    private long revision;
    private String releaseDate;
    private boolean isHeartbeatStale;
    private boolean hasLicense;

    public HaModel(ArtifactoryServer server, boolean isHeartbeatStale, boolean hasLicense) {
        id = server.getServerId();
        CentralConfigService centralConfig = ContextHelper.get().getCentralConfig();
        startTime = centralConfig.getDateFormatter().print(server.getStartTime());
        url = server.getContextUrl();
        memberShipPort = server.getMembershipPort();
        role = server.getServerRole().getPrettyName();
        lastHeartbeat = centralConfig.getDateFormatter().print(server.getLastHeartbeat());
        version = server.getArtifactoryVersion();
        revision = server.getArtifactoryRevision();
        releaseDate = centralConfig.getDateFormatter().print(server.getArtifactoryRelease());
        this.isHeartbeatStale = isHeartbeatStale;
        this.hasLicense = hasLicense;
        ArtifactoryServerState serverState = server.getServerState();
        if (isHeartbeatStale) {
            serverState = ArtifactoryServerState.UNAVAILABLE;
        }
        state = serverState.getPrettyName();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getMemberShipPort() {

        return memberShipPort;
    }

    public void setMemberShipPort(int memberShipPort) {
        this.memberShipPort = memberShipPort;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(String lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isHeartbeatStale() {
        return isHeartbeatStale;
    }

    public void setIsHeartbeatStale(boolean isHeartbeatStale) {
        this.isHeartbeatStale = isHeartbeatStale;
    }

    public boolean isHasLicense() {
        return hasLicense;
    }

    public void setHasLicense(boolean hasLicense) {
        this.hasLicense = hasLicense;
    }
}
