package org.artifactory.security.access;

import com.google.common.collect.Lists;
import org.artifactory.util.ListeningPortDetector;
import org.jfrog.access.common.ServiceId;
import org.jfrog.access.router.registration.HealthCheckEndpoint;
import org.jfrog.access.router.registration.LocalEndpoint;
import org.jfrog.access.router.registration.RoutePath;
import org.jfrog.access.router.registration.RouterRegistrationData;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Tamir Hadad
 */
@Test
public class ArtifactoryRouterRegistrationDataTest {

    public void testRouterRegistrationDataBuilder() {
        int port = 9090;
        ServiceId serviceId = new ServiceId("jf", "test");
        System.setProperty(ListeningPortDetector.SYS_ARTIFACTORY_PORT, String.valueOf(port));
        RouterRegistrationData registrationData = new ArtifactoryRouterRegistrationData(serviceId, "nodeId").build();
        assertThat(registrationData.getNodeId()).isEqualTo("nodeId");
        assertThat(registrationData.getServiceId()).isEqualTo(serviceId);
        List<LocalEndpoint> endpoints = registrationData.getEndpoints();
        assertThat(endpoints).hasSize(1);
        ArrayList<String> paths = Lists.newArrayList("/", "/artifactory/", "/v2/");
        List<RoutePath> routePaths = endpoints.get(0).getRoutePaths();
        assertThat(routePaths).hasSize(3);
        assertThat(routePaths.stream().allMatch(path -> paths.contains(path.getPath()))).isTrue();
        HealthCheckEndpoint healthCheckEndpoint = registrationData.getHealthCheckEndpoint();
        assertThat(healthCheckEndpoint.getPath()).isEqualTo("/api/system/ping");
        assertThat(healthCheckEndpoint.getPort()).isEqualTo(port);
    }
}