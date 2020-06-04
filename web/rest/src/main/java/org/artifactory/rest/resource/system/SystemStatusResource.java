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

package org.artifactory.rest.resource.system;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.rest.search.result.StatusRestResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.service.ArtifactoryServersCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.addon.AddonsManager.NO_LICENSE_HASH;

/**
 * Resource to get information about Artifactory's current nodes' status (version, state and license)
 *
 * @author Rotem Kfir
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Path(SystemRestConstants.PATH_ROOT + "/" + SystemRestConstants.PATH_STATUS)
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
public class SystemStatusResource {

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private ArtifactoryServersCommonService serversService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArtifactoryStatus() {
        HaAddon haAddon = addonsManager.addonByType(HaAddon.class);
        boolean isHa = haAddon.isHaEnabled();

        List<ArtifactoryServer> artifactoryServers = serversService.getActiveMembers();
        List<StatusRestResult.NodeStatus> nodes = convertToNodeStatuses(artifactoryServers);

        return Response.ok()
                .entity(new StatusRestResult(isHa, nodes))
                .build();
    }

    private List<StatusRestResult.NodeStatus> convertToNodeStatuses(List<ArtifactoryServer> servers) {
        return servers.stream()
                .map(this::convertToNodeStatus)
                .collect(Collectors.toList());
    }

    private StatusRestResult.NodeStatus convertToNodeStatus(ArtifactoryServer server) {
        String licenseHash = NO_LICENSE_HASH.equals(server.getLicenseKeyHash()) ? null : server.getLicenseKeyHash();
        return new StatusRestResult.NodeStatus(server.getServerId(), server.getServerState().name(), server.getArtifactoryVersion(), licenseHash);
    }
}
