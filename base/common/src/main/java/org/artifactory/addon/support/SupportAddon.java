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

package org.artifactory.addon.support;

import org.artifactory.addon.Addon;
import org.artifactory.api.download.FolderDownloadResult;
import org.jfrog.support.rest.model.manifest.NodeManifest;

import java.io.IOException;

/**
 * @author Michael Pasternak
 */
public interface SupportAddon extends Addon {

    default boolean isSupportAddonEnabled() {
        return false;
    }

    /**
     * Generates support bundle/s
     *
     * @param bundleConfiguration config to be used
     *
     * @return name/s of generated bundles
     */
    default String generate(Object bundleConfiguration, String artUrl) {
        return "";
    }

    /**
     * @param nodeManifest - to search the bundle by
     * @param baseUrl      - request base url
     * @return serialized service manifest
     */
    default String getBundleInfo(NodeManifest nodeManifest, String baseUrl) {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes artifatory bundles of {@param bundleId}
     */
    default boolean deleteBundle(String bundleId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes specific Archive by name
     * @param bundleName - archive name to delete
     *
     */
    default boolean deleteBundleByName(String bundleName) {return true;}

    /**
     *
     * @return artifactory bundles list
     */
    default String listBundles() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @return artifactory bundles list for the ui
     */
    default String uiListBundles() {return "";}

    /**
     *
     * @param bundleId - bundle id to download
     * @return stream of the bundle by Id
     */
    default FolderDownloadResult downloadBundleArchive(String bundleId) {return null;}

    /**
     *
     * @param bundleName - specific Archive by name
     * @return stream of the entire bundle folder to download
     */
    default FolderDownloadResult downloadByBundleName(String bundleName){return null;}

    /**
     * Generates Support bundle and manages the service manifest.
     * Propagates to other cluster nodes (mandated by {@param currentNodeOnly}
     *
     * @param artifactoryBaseUrl is required for setting a download url on the generated service manifest.
     *
     * @return String since Artifactory's Jersey implementation has to be 2.x combined with Jackson 2.x, which it is not
     * so until we do and in order to deal with the common model we have to do it like this.
     */
    default String generateSupportBundle(ArtifactorySupportBundleConfig bundleInfo, String artifactoryBaseUrl) throws IOException {
        throw new UnsupportedOperationException("Generating support bundle requires Artifactory Pro.");
    }

    /**
     * Generates Support bundle as part of a larger cluster request, will not change the service manifest and will not propagate
     *
     * @return String since Artifactory's Jersey implementation has to be 2.x combined with Jackson 2.x, which it is not
     *  so until we do and in order to deal with the common model we have to do it like this.
     */
    default String generateClusterNodeSupportBundle(ArtifactorySupportBundleConfig bundleInfo) throws IOException {
        return null;
    }

    /**
     *
     * @param repoKey - repo name
     * @return true if the repoKey is support bundle repo
     */
    default boolean isSupportBundlesRepo(String repoKey) {
        return false;
    }


    /**
     * Cleanup old bundles
     */
    default void cleanUp() {
        return;
    }

    /**
     * Delete all repo content
     */
    default void deleteAll() {
        return;
    }
}
