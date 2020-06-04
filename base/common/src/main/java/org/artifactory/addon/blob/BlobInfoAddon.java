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

package org.artifactory.addon.blob;

import org.artifactory.addon.Addon;
import org.artifactory.addon.license.EnterprisePlusAddon;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.api.rest.blob.ClosestBlobInfoRequest;
import org.artifactory.util.UnsupportedByLicenseException;

/**
 * @author Rotem Kfir
 */
@EnterprisePlusAddon
public interface BlobInfoAddon extends Addon {

    default String getClosestBlobInfo(ClosestBlobInfoRequest request, String auth) {
        throw new UnsupportedByLicenseException(
                "Get closest blob info " + ReleaseBundleAddon.ENTERPRISE_PLUS_MSG);
    }
}
