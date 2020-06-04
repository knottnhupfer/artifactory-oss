package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.repo.RepoDetailsType;

public enum RepositoryType {

    LOCAL("Local"), REMOTE("Remote"), DISTRIBUTION("Distribution"), VIRTUAL("Virtual"),
    CACHED("Cached"), RELEASE_BUNDLES("ReleaseBundles"), SUPPORT_BUNDLES("SupportBundles");

    private final String typeName;

    RepositoryType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getTypeNameLoweCase() {
        return getTypeName().toLowerCase();
    }

    public static RepositoryType byNativeName(String nativeName) {
        for (RepositoryType repoType : values()) {
            if(repoType.getTypeNameLoweCase().equals(nativeName.trim())) {
                    return repoType;
            }
        }
        return null;
    }

    public static RepositoryType byDescriptor(RepoDescriptor descriptor) {
        if (descriptor instanceof LocalCacheRepoDescriptor) {
            return RepositoryType.CACHED;
        }
        if (descriptor instanceof DistributionRepoDescriptor) {
            return RepositoryType.DISTRIBUTION;
        }
        if (descriptor instanceof ReleaseBundlesRepoDescriptor) {
            return RepositoryType.RELEASE_BUNDLES;
        }
        if (descriptor instanceof LocalRepoDescriptor) {
            return RepositoryType.LOCAL;
        }
        if (descriptor instanceof RemoteRepoDescriptor) {
            return RepositoryType.REMOTE;
        }
        if (descriptor instanceof VirtualRepoDescriptor) {
            return RepositoryType.VIRTUAL;
        }
        return RepositoryType.LOCAL;
    }

}
