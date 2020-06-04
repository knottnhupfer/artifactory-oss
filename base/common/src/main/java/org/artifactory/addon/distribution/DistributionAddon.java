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

package org.artifactory.addon.distribution;

import org.artifactory.addon.Addon;
import org.artifactory.addon.license.EnterprisePlusAddon;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.rest.distribution.bundle.models.FileSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.OutputStream;


/**
 * @author Tomer Mayost
 */
@EnterprisePlusAddon
public interface DistributionAddon extends Addon {

    default String validateFileAndGetChecksum(@Nonnull FileSpec fileSpec) {
        return null;
    }

    @Deprecated
    default BasicStatusHolder distributeArtifact(@Nonnull FileSpec fileSpec, String delegateToken, String auth) {
        throw ReleaseBundleAddon.BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default void distributeArtifactStreaming(@Nullable String fileTransactionId, @Nonnull FileSpec fileSpec,
            String delegateToken, String auth, String checksum, OutputStream outputStream) {
        throw ReleaseBundleAddon.BUNDLE_UNSUPPORTED_OPERATION_EXCEPTION;
    }
}
