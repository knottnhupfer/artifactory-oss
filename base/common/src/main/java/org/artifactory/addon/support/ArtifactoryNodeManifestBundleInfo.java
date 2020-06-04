package org.artifactory.addon.support;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.jfrog.support.rest.model.manifest.NodeManifestBundleInfo;

import java.util.Date;

/**
 * @author Tamir Hadad
 */
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ArtifactoryNodeManifestBundleInfo extends NodeManifestBundleInfo {

    private long createDate;

    public ArtifactoryNodeManifestBundleInfo(NodeManifestBundleInfo nodeManifestBundleInfo) {
        this.setId(nodeManifestBundleInfo.getId());
        this.setCreated(nodeManifestBundleInfo.getCreated());
        this.setDescription(nodeManifestBundleInfo.getDescription());
        this.setName(nodeManifestBundleInfo.getName());
        this.setStatus(nodeManifestBundleInfo.getStatus().replace(' ', '_'));
        this.createDate = Date.from(nodeManifestBundleInfo.getCreated().toInstant()).getTime();
    }
}


