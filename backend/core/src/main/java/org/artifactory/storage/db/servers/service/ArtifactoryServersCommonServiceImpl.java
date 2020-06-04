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

package org.artifactory.storage.db.servers.service;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.model.ArtifactoryServerInfo;
import org.artifactory.storage.db.servers.model.ArtifactoryServerRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author gidis
 */
@Service
public class ArtifactoryServersCommonServiceImpl implements ArtifactoryServersCommonService {

    private static final Logger log = LoggerFactory.getLogger(ArtifactoryServersCommonServiceImpl.class);

    @Autowired
    private ArtifactoryServersService serversService;

    @Nullable
    @Override
    public ArtifactoryServer getRunningHaPrimary() {
        List<ArtifactoryServer> servers = serversService.getAllArtifactoryServers().stream()
                .filter(isPrimary)
                .filter(isRunning)
                .collect(Collectors.toList());
        if (servers.size() > 1) {
            throw new IllegalStateException(
                    "Found " + servers.size() + " running primary nodes where only 1 is allowed.");
        }
        return !servers.isEmpty() ? servers.get(0) : null;
    }

    //TODO [by shayb]: the getRunningHaPrimary is doing it wrong! we must delete it. This one is duplicate, but with additional filter of "is active"
    @Override
    public ArtifactoryServer getActiveRunningHaPrimary() {
        List<ArtifactoryServer> servers = serversService.getAllArtifactoryServers().stream()
                .filter(hasHeartbeat)
                .filter(isPrimary)
                .filter(isRunning)
                .collect(Collectors.toList());
        if (servers.size() > 1) {
            throw new IllegalStateException(
                    "Found " + servers.size() + " running primary nodes where only 1 is allowed.");
        }
        return !servers.isEmpty() ? servers.get(0) : null;
    }

    @Override
    public List<ArtifactoryServer> getOtherRunningHaMembers() {
        return serversService.getAllArtifactoryServers().stream()
                .filter(isOther)
                .filter(isRunning)
                .filter(hasHeartbeat)
                .collect(Collectors.toList());
    }

    @Override
    public ArtifactoryServer getArtifactoryServer(String serverId) {
        return serversService.getArtifactoryServer(serverId);
    }

    @Override
    public void updateArtifactoryServerRole(String serverId, ArtifactoryServerRole newRole) {
        serversService.updateArtifactoryServerRole(serverId, newRole);
    }

    @Override
    public void updateArtifactoryJoinPort(String serverId, int joinPort) {
        serversService.updateArtifactoryJoinPort(serverId, joinPort);
    }

    @Override
    public void updateArtifactoryServerState(ArtifactoryServer server, ArtifactoryServerState newState) {
        serversService.updateArtifactoryServerState(server.getServerId(), newState);
    }

    @Override
    public List<ArtifactoryServer> getAllArtifactoryServers() {
        return serversService.getAllArtifactoryServers();
    }

    @Override
    public List<ArtifactoryServerInfo> getAllArtifactoryServersInfo() {
        return serversService.getAllArtifactoryServers().stream()
                .map(server -> new ArtifactoryServerInfo(server, ConstantValues.haHeartbeatStaleIntervalSecs.getInt()))
                .collect(Collectors.toList());
    }

    @Override
    public void createArtifactoryServer(ArtifactoryServer artifactoryServer) {
        serversService.createArtifactoryServer(artifactoryServer);
    }

    @Override
    public void updateArtifactoryServer(ArtifactoryServer artifactoryServer) {
        serversService.updateArtifactoryServer(artifactoryServer);
    }

    @Override
    public boolean removeServer(String serverId) {
        return serversService.removeServer(serverId);
    }

    @Override
    public void updateArtifactoryServerHeartbeat(String serverId, long heartBeat, String licenseKeyHash) {
        serversService.updateArtifactoryServerHeartbeat(serverId, heartBeat, licenseKeyHash);
    }

    @Override
    public boolean updateArtifactoryLicenseHash(String serverId, long lastHeartbeat, String licenseKeyHash) {
        List<ArtifactoryServer> allArtifactoryServers = getAllArtifactoryServers();
        Optional<ArtifactoryServer> oldMe = allArtifactoryServers.stream()
                .filter(server -> server.getServerId().equals(serverId))
                .findFirst();
        //TODO [shayb] should return with false if not exist?
        String oldLicenseHash = oldMe.isPresent() ? oldMe.get().getLicenseKeyHash() : "";
        List<ArtifactoryServer> otherActiveMembers = allArtifactoryServers.stream().filter(hasHeartbeat)
                .filter(isRunning.or(isStarting).or(isStopping).or(isConverting))
                .filter(isOther)
                .collect(Collectors.toList());

        // If there is a active (heartbeat of less than now - 30 seconds) server with this license, we shouldn't update the DB that we own the license.
        boolean skipUpdate = otherActiveMembers.stream()
                .anyMatch((server) -> server.getLicenseKeyHash().equals(licenseKeyHash));
        log.debug("{} should skip update: {}", serverId, skipUpdate);
        if (!skipUpdate) {
            int affectedRows = serversService.updateArtifactoryServerHeartbeat(serverId, lastHeartbeat, licenseKeyHash);
            if (affectedRows > 0) {
                otherActiveMembers = getOtherActiveMembers();
                boolean duplicateLicenseFound = otherActiveMembers.stream()
                        .anyMatch((server) -> server.getLicenseKeyHash().equals(licenseKeyHash));
                log.debug("{} found duplicate license: {}", serverId, skipUpdate);
                if (duplicateLicenseFound) {
                    updateArtifactoryServerHeartbeat(serverId, lastHeartbeat, oldLicenseHash);
                } else {
                    log.debug("{} returning true, license was activated", serverId);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<ArtifactoryServer> getOtherActiveMembers() {
        return getActiveMembers().stream()
                .filter(isOther)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArtifactoryServer> getActiveMembers() {
        return serversService.getAllArtifactoryServers().stream()
                .filter(hasHeartbeat)
                .filter(isRunning
                        .or(isStarting)
                        .or(isStopping)
                        .or(isConverting))
                .collect(Collectors.toList());
    }

    @Override
    public List<ArtifactoryServer> getConvertingMembers() {
        return serversService.getAllArtifactoryServers().stream()
                .filter(hasHeartbeat)
                .filter(isConverting)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isConversionRunning() {
        return !getConvertingMembers().isEmpty();
    }

    @Override
    public ArtifactoryServer getCurrentMember() {
        String serverId = ContextHelper.get().getServerId();
        return serversService.getArtifactoryServer(serverId);
    }
}
