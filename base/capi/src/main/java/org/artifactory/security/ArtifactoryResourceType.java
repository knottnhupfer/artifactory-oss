package org.artifactory.security;

import java.util.Arrays;

/**
 * This enum represents types of permissions used by Artifactory that are saved in Access
 *
 * @author maximy
 */
public enum ArtifactoryResourceType {
    BUILD("build"),
    REPO("repo"),
    RELEASE_BUNDLES("release_bundle");

    private final String name;

    ArtifactoryResourceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ArtifactoryResourceType fromString(String string) {
        return Arrays.stream(ArtifactoryResourceType.values())
                .filter(type -> type.getName().equals(string))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("No ArtifactoryResourceType matching string: " + string));
    }
}
