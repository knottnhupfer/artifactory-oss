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

package org.artifactory.addon.release.bundle;

import lombok.NonNull;
import org.artifactory.addon.Addon;
import org.artifactory.addon.license.EnterprisePlusAddon;
import org.artifactory.api.release.bundle.ReleaseBundleSearchFilter;
import org.artifactory.api.rest.distribution.bundle.models.*;
import org.artifactory.api.rest.release.ReleaseBundleRequest;
import org.artifactory.api.rest.release.ReleaseBundleResult;
import org.artifactory.api.rest.release.ReleaseBundlesConfigModel;
import org.artifactory.api.rest.release.SourceReleaseBundleRequest;
import org.artifactory.bundle.BundleType;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.sapi.common.Lock;
import org.artifactory.util.UnsupportedByLicenseException;
import org.jfrog.security.util.Pair;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

/**
 * @author Shay Bagants
 */
@EnterprisePlusAddon
public interface ReleaseBundleAddon extends Addon {

    String ENTERPRISE_PLUS_MSG = "is only available on Enterprise Plus licensed Artifactory instances.";
    UnsupportedByLicenseException BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION = new UnsupportedByLicenseException(
            "Release bundle " + ENTERPRISE_PLUS_MSG);

    default ReleaseBundleResult executeReleaseBundleRequest(ReleaseBundleRequest bundleRequest, boolean includeMetaData) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default void verifyReleaseBundleSignature(String signature, String keyID) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    @Lock
    default BundleTransactionResponse createBundleTransaction(String signedJwsBundle) throws IOException {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    @Lock
    default void closeBundleTransaction(String transactionId) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    @NonNull
    default CloseTransactionStatusResponse closeBundleTransactionAsync(@NonNull String transactionPath,
            Integer syncWaitTime) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    @NonNull
    default CloseTransactionStatusResponse checkCloseTransactionStatus(@NonNull String transactionPath) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default BundlesResponse getAllBundles(BundleType bundleType) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default BundlesResponse getCompletedBundlesLastVersion(ReleaseBundleSearchFilter filter) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default BundlesNamesResponse getBundlesByReposAndPatterns(List<String> repos, List<String> includePatterns,
            List<String> excludePatterns) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default BundleVersionsResponse getBundleVersions(String bundleName, BundleType bundleType) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default BundleVersionsResponse getBundleVersions(ReleaseBundleSearchFilter filter) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default String getBundleJson(String bundleName, String bundleVersion, BundleType bundleType) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default String getBundleSignedJws(String bundleName, String bundleVersion, BundleType bundleType) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    void exportTo(ExportSettings exportSettings);

    @Lock
    void importFrom(ImportSettings importSettings);

    @Lock
    default void deleteAllBundles() {
    }

    @Lock
    default void deleteReleaseBundle(String bundleName, String bundleVersion, BundleType type, boolean includeContent) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default ReleaseBundleModel getBundleModel(String bundleName, String bundleVersion, BundleType bundleType) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default ReleaseBundlesConfigModel getReleaseBundlesConfig() {
        return null;
    }

    default void setReleaseBundlesConfig(ReleaseBundlesConfigModel releaseBundlesConfig) {
    }

    /**
     * Stores a release bundle on source Artifactory for later distribution(s)
     *
     * @param sourceReleaseBundleRequest the requested bundle to store
     * @return the bundle prefix (including the release bundles repo key) and the {@link org.apache.http.HttpStatus} returned status
     */
    default Pair<String, Integer> storeBundle(SourceReleaseBundleRequest sourceReleaseBundleRequest)
            throws IOException, ParseException, SQLException {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default String getStoringRepo(String name, String version, BundleType bundleType) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default String getBundleStatus(String bundleName, String bundleVersion, BundleType bundleType) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default List<ReleaseArtifactInternalModel> getReleaseArtifactsUsingAql(String bundleName, String bundleVersion,
            BundleType bundleType) {
        throw BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }
}
