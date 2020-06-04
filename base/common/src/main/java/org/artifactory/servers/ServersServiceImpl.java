package org.artifactory.servers;

import org.apache.commons.collections.CollectionUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.security.access.AccessService;
import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.service.ArtifactoryServersCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static org.artifactory.servers.ServerModel.ServerStatus.*;

/**
 * @author Rotem Kfir
 */
@Service
public class ServersServiceImpl implements ServersService {

    @Autowired
    private AccessService accessService;
    @Autowired
    private ArtifactoryServersCommonService serversService;

    @Nonnull
    @Override
    public List<ServerModel> getAllArtifactoryServers() {
        String serviceId = accessService.getArtifactoryServiceId().getFormattedName();

        List<ServerModel> servers = new ArrayList<>();
        List<ArtifactoryServer> allArtifactoryServers = serversService.getAllArtifactoryServers();
        if (CollectionUtils.isNotEmpty(allArtifactoryServers)) {
            allArtifactoryServers.forEach(server -> {
                boolean hasHeartbeat = ArtifactoryServersCommonService.hasHeartbeat.test(server);
                boolean hasLicense = !server.getLicenseKeyHash().equals(AddonsManager.NO_LICENSE_HASH);
                ServerModel model = toServerModel(serviceId, server, !hasHeartbeat, hasLicense);
                servers.add(model);
            });
        }
        return servers;
    }

    private ServerModel toServerModel(String serviceId, ArtifactoryServer server, boolean isHeartbeatStale, boolean hasLicense) {
        ArtifactoryServerState serverState = isHeartbeatStale ? ArtifactoryServerState.UNAVAILABLE : server.getServerState();
        String statusDetails = serverState.getPrettyName();
        if (!hasLicense) {
            statusDetails += NO_LICENSE_MESSAGE;
        }

        return new ServerModel()
                .serviceName(ARTIFACTORY_SERVICE_NAME)
                .serviceId(serviceId)
                .nodeId(server.getServerId())
                .url(server.getContextUrl())
                .version(server.getArtifactoryVersion())
                .lastHeartbeat(server.getLastHeartbeat())
                .heartbeatStale(isHeartbeatStale)
                .startTime(server.getStartTime())
                .status(mapToStatus(serverState, hasLicense))
                .statusDetails(statusDetails);
    }

    private static ServerModel.ServerStatus mapToStatus(ArtifactoryServerState state, boolean hasLicense) {
        if (state == null) {
            return DOWN;
        }
        switch (state) {
            case RUNNING:
                return hasLicense ? ONLINE : PARTIALLY_ONLINE;
            case STARTING:
            case STOPPING:
            case CONVERTING:
                return PARTIALLY_ONLINE;
            default:
                return DOWN;
        }
    }
}
