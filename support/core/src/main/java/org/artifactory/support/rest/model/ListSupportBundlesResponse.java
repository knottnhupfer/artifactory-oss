package org.artifactory.support.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jfrog.support.rest.model.manifest.NodeManifestBundleInfo;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListSupportBundlesResponse {

    private List<NodeManifestBundleInfo> bundles;
    private int count;

}
