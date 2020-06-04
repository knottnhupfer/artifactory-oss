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

package org.artifactory.addon.replicator;

import org.artifactory.addon.Addon;
import org.artifactory.addon.license.EnterprisePlusAddon;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.api.replicator.ReplicatorRegistrationRequest;
import org.artifactory.api.replicator.ReplicatorRegistrationResponse;
import org.artifactory.util.UnsupportedByLicenseException;

import java.io.IOException;

/**
 * @author Yoaz Menda
 */
@EnterprisePlusAddon
public interface ReplicatorAddon extends Addon {

    default ReplicatorRegistrationResponse register(ReplicatorRegistrationRequest registrationRequest) {
        throw new UnsupportedByLicenseException("Replicator registration " + ReleaseBundleAddon.ENTERPRISE_PLUS_MSG);
    }

    default String getExternalUrl() {
        throw new UnsupportedByLicenseException("Replicator " + ReleaseBundleAddon.ENTERPRISE_PLUS_MSG);
    }

    default void reCreateReplicatorConfigFile() throws IOException {
        // nothing to create
    }

    default ReplicatorDetails getReplicatorDetails() {
        throw new UnsupportedByLicenseException("Replicator " + ReleaseBundleAddon.ENTERPRISE_PLUS_MSG);
    }
}
