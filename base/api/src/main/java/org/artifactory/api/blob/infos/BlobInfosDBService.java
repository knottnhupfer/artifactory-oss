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

package org.artifactory.api.blob.infos;

import org.artifactory.api.rest.blob.BlobInfo;
import org.jfrog.storage.StorageException;

import java.util.List;

/**
 * @author Inbar Tal
 */
public interface BlobInfosDBService {

    /**
     * Insert a given blobInfo to DB. If already exist - override it.
     * @param checksum - the {@link BlobInfo} id
     * @param blobInfoJson - the {@link BlobInfo in JSON format} to cache
     * @return true if cached successfully, otherwise return false
     * @throws StorageException - In case a storage error occurred during the operation
     */
    boolean putBlobInfo(String checksum, String blobInfoJson) throws StorageException;

    /**
     * Delete a blobInfo by it's checksum.
     * @param checksum - the checksum of the {@link BlobInfo} to delete
     * @return 1 if delete successfully, otherwise return 0
     * @throws StorageException - In case a storage error occurred during the operation
     */
    int deleteBlobInfo(String checksum) throws StorageException;

    /**
     * Delete all given blobInfos by their checksums.
     * @param checksums - the checksum of the blobInfo to delete
     * @return number of {@link BlobInfo}s deleted
     * @throws StorageException - In case a storage error occurred during the operation
     */
    int deleteBlobInfos(List<String> checksums) throws StorageException;

    /**
     * Find blobInfo by it's checksum.
     * @param checksum - the checksum of the requested blobInfo
     * @return - the {@link BlobInfo in JSON Format} if found, otherwise return null.
     * @throws StorageException - In case a storage error occurred during the operation
     */
    String getBlobInfo(String checksum) throws StorageException;

}
