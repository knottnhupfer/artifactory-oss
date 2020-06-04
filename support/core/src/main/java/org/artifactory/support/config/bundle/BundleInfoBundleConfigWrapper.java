package org.artifactory.support.config.bundle;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jfrog.support.rest.model.SupportBundleConfig;
import org.jfrog.support.rest.model.manifest.NodeManifest;

//TODO [by tamir]: REMOVE ME
@AllArgsConstructor
@Data
public class BundleInfoBundleConfigWrapper {

    private final SupportBundleConfig config;

    private final NodeManifest nodeManifest;

}
