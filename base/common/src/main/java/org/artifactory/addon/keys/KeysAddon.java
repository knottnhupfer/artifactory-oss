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

package org.artifactory.addon.keys;

import org.artifactory.addon.Addon;
import org.artifactory.addon.license.EnterprisePlusAddon;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.keys.TrustedKey;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.util.UnsupportedByLicenseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * @author Rotem Kfir
 */
@EnterprisePlusAddon
public interface KeysAddon extends Addon {
    UnsupportedByLicenseException TRUSTED_KEYS_UNSUPPORTED_OPERATION_EXCEPTION = new UnsupportedByLicenseException(
            "Multiple Trusted Keys " + ReleaseBundleAddon.ENTERPRISE_PLUS_MSG);

    @Nonnull
    default List<TrustedKey> findAllTrustedKeys() {
        throw TRUSTED_KEYS_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default Optional<TrustedKey> findTrustedKeyById(String kid) {
        throw TRUSTED_KEYS_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default Optional<TrustedKey> findTrustedKeyByAlias(String alias) {
        throw TRUSTED_KEYS_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default TrustedKey createTrustedKey(String key, @Nullable String alias) {
        throw TRUSTED_KEYS_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    default void deleteTrustedKey(String kid) {
        throw TRUSTED_KEYS_UNSUPPORTED_OPERATION_EXCEPTION;
    }

    void exportTo(ExportSettings settings);

    void importFrom(ImportSettings settings);
}
