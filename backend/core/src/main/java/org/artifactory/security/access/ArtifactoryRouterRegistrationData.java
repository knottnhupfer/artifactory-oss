package org.artifactory.security.access;

import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.compress.utils.Sets;
import org.artifactory.util.ListeningPortDetector;
import org.jfrog.access.common.ServiceId;
import org.jfrog.access.router.registration.*;

import java.util.List;
import java.util.Set;

/**
 * @author Tamir Hadad
 */
@Data
public class ArtifactoryRouterRegistrationData {
    public static final String ARTIFACTORY_HEALTH_CHECK_ENDPOINT = "/api/system/ping";
    private ServiceId serviceId;
    private String nodeId;
    private int artPort;

    public ArtifactoryRouterRegistrationData(ServiceId serviceId, String nodeId) {
        this.serviceId = serviceId;
        this.nodeId = nodeId;
        this.artPort = ListeningPortDetector.detect();
    }

    public RouterRegistrationData build() {
        LocalEndpoint endpoint = new LocalEndpoint(artPort, EndpointProtocol.HTTP1, getRoutePaths());
        HealthCheckEndpoint healthCheckEndpoint = new HealthCheckEndpoint(artPort, ARTIFACTORY_HEALTH_CHECK_ENDPOINT);
        return new RouterRegistrationData(serviceId, nodeId, Lists.newArrayList(endpoint), healthCheckEndpoint);
    }

    private List<RoutePath> getRoutePaths() {
        Set <String> artifactoryRoutingPaths = Sets.newHashSet("/", "/artifactory/", "/v2/");
        List<RoutePath> paths = Lists.newArrayList();
        artifactoryRoutingPaths.forEach(path -> paths.add(new RoutePath(path, false)));
        return paths;
    }
}
