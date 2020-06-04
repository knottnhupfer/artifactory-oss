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

package org.artifactory.api.storage;

/**
 * This is an adapter class to support mission control user plugin. We need this because we moved the original
 * {@link org.jfrog.storage.common.StorageUnit} to another location, and mission control plugin should support both
 * Artifactory 4.x and 5.x, without having different plugins per version with different imports on each plugin.
 *
 * Do not use this class directly, use {@link org.jfrog.storage.common.StorageUnit} instead.
 *
 * @author Shay Bagants
 */
@Deprecated
public class StorageUnit {

    /**
     * Convert the number of bytes to a human readable size, if the size is more than 1024 megabytes display the correct
     * number of gigabytes.
     *
     * @param size The size in bytes.
     * @return The size in human readable format.
     *
     * @deprecated as of Artifactory 5.0, replaces by {@link org.jfrog.storage.common.StorageUnit#toReadableString(long)}
     */
    @Deprecated
    public static String StorageUnit(long size) {
        return org.jfrog.storage.common.StorageUnit.toReadableString(size);
    }
}

