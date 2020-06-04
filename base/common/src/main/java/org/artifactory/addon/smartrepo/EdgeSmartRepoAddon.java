package org.artifactory.addon.smartrepo;

import org.artifactory.addon.Addon;
import org.artifactory.addon.license.EdgeAddon;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;

/**
 * This Addon purpose is to make sure that all remote repositories on Edge are smart repositories.
 *
 * @author Inbar Tal
 */
@EdgeAddon
public interface EdgeSmartRepoAddon extends Addon {
    String ERR_MSG = "Only Smart Remote repositories are supported with Artifactory Edge license";

    /**
     * On Edge instances (denoted by {@link EdgeAddon}), replication is not allowed.
     */
    default void validateReplication() { }

    /**
     * Call this method in the configuration state to verify user config is correct.
     *
     * On non-edge instances (denoted by {@link EdgeAddon}) requests are never blocked.
     * On Edge instances 1 condition must hold:
     *  - The upstream url is another Artifactory instance with a valid license.
     */
    default boolean shouldBlockNonSmartRepo(HttpRepoDescriptor repoDescriptor) {
        return false;
    }

    /**
     * On non-edge instances (denoted by {@link EdgeAddon}) requests are never blocked.
     * On Edge instances 2 conditions must hold for remote repos to work:
     *  - Smart repo is configured for {@param repoKey}
     *  - The upstream url is another Artifactory instance with a valid license.
     */
    default boolean shouldBlockNonSmartRepo(String repoKey) {
        return false;
    }
}
