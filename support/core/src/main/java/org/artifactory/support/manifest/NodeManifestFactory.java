package org.artifactory.support.manifest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ha.HaNodeProperties;
import org.jfrog.access.common.ServiceId;
import org.jfrog.support.rest.model.SupportBundleConfig;
import org.jfrog.support.rest.model.manifest.NodeManifest;
import org.jfrog.support.rest.model.manifest.NodeManifestBundleInfo;

import javax.annotation.Nullable;
import java.util.Objects;

import static org.artifactory.addon.support.ArtifactorySupportBundleConfig.fromSupportBundleConfig;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeManifestFactory {

    private static final String SERVICE_TYPE = "jfrt";
    private static final String MICRO_SERVICE_NAME = "artifactory";
    private static final String DEFAULT_NODE_ID = "sa";

    public static NodeManifest newNodeManifest(SupportBundleConfig bundleConfig, @Nullable ServiceId artifactoryServiceId) {
        return newNodeManifest(fromSupportBundleConfig(bundleConfig), artifactoryServiceId);
    }

    public static NodeManifest newNodeManifest(NodeManifestBundleInfo bundleInfo, @Nullable ServiceId artifactoryServiceId) {
        ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
        String version = artifactoryHome.getRunningArtifactoryVersion().getVersionName();

        String nodeId  = DEFAULT_NODE_ID;
        HaNodeProperties haNodeProperties = artifactoryHome.getHaNodeProperties();
        if (!Objects.isNull(haNodeProperties)) {
            nodeId = haNodeProperties.getServerId();
        }
        String serviceId = "";
        if (!Objects.isNull(artifactoryServiceId)) {
            serviceId = artifactoryServiceId.getFormattedName();
        }
        return new NodeManifest(SERVICE_TYPE, MICRO_SERVICE_NAME, version, serviceId, nodeId, bundleInfo);
    }
}
