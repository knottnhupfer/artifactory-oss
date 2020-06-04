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

import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.model.ArtifactoryServerRole;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A service to interact with the artifactory servers table.
 *
 * @author Yossi Shaul
 */
public interface ArtifactoryServersService {
    List<ArtifactoryServer> getAllArtifactoryServers();

    /**
     * Get the {@link ArtifactoryServer} instance from storage with the given {@code serverId}
     *
     * @param nodeId The unique server id
     * @return {@link ArtifactoryServer} if exists, otherwise null
     */
    @Nullable
    ArtifactoryServer getArtifactoryServer(String nodeId);

    @Transactional
    int createArtifactoryServer(ArtifactoryServer artifactoryServer);

    @Transactional
    int updateArtifactoryServer(ArtifactoryServer artifactoryServer);

    @Transactional
    int updateArtifactoryServerState(String serverId, ArtifactoryServerState newState);

    @Transactional
    int updateArtifactoryServerRole(String serverId, ArtifactoryServerRole newRole);

    @Transactional
    int updateArtifactoryJoinPort(String serverId, int port);

    @Transactional
    boolean removeServer(String serverId);

    @Transactional
    int updateArtifactoryServerHeartbeat(String serverId, long heartbeat, String licenseKeyHash);
}
