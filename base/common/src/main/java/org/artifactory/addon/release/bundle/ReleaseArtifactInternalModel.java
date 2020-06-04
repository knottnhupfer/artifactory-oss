package org.artifactory.addon.release.bundle;

import lombok.Data;

@Data
public class ReleaseArtifactInternalModel {

    String name;
    String path;
    String createdBy;
    long size;
}
