/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.addon.debian;

import org.artifactory.addon.Addon;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.fs.RepoResource;
import org.artifactory.repo.RepoPath;

/**
 * @author Gidi Shabat
 */
public interface DebianAddon extends Addon {

    /**
     * Calculate all the indices in repo
     * the method is being invoked by the recalculate button in the UI or by REST
     * Note that the REST clients are allowed to pass password by headers
     */
    default void recalculateAll(RepoBaseDescriptor descriptor, String passphrase, boolean delayed) {
    }

    /**
     * Calculate virtual metadata after events (i.e. virtual configuration change). Should not be used only for
     * automatic calculation after specific event and not directly when a user tries to trigger the calculation, the
     * reason is because automatic calculation that is triggered by internal event can be disabled by a system property
     */
    default void calculateVirtualMetadataAfterInternalEvent(String repoKey, String distribution, String component,  boolean delayed) {
    }

    default boolean hasPrivateKey() {
        return false;
    }

    default boolean hasPublicKey() {
        return false;
    }

    boolean foundExpiredAndRemoteIsNewer(RepoResource remoteResource, RepoResource cachedResource);

    /**
     * Used to get the package metadata for the UI info tab
     *
     * @param repoPath Path to the package
     * @return DebianMetadataInfo instance - UI model for the info tab
     */
    default DebianMetadataInfo getDebianMetadataInfo(RepoPath repoPath) {
        return null;
    }

    /**
     * Finds and index all cached debian packages coordinates in the given remote cached repository.
     * This works only on cached debian packages that their Package file also been cached.
     *
     * @param repoKey the cached remote repository to work on
     */
    default void calculateCachedDebianCoordinates(String repoKey) {
    }
}
