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

package org.artifactory.storage;

/**
 * @author gidis
 */
public enum BinaryProviderType {
    filesystem, // binaries are stored in the filesystem
    fullDb,     // binaries are stored as blobs in the db, filesystem is used for caching unless cache size is 0
    cachedFS,   // binaries are stored in the filesystem, but a front cache (faster access) is added
    S3,         // binaries are stored in S3 JClouds API
    S3Old,        // binaries are stored in S3 Jets3t API
    goog        // binaries are stored in S3 Jets3t API
}
