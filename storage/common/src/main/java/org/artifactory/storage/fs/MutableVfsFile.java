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

package org.artifactory.storage.fs;

import org.artifactory.checksum.ChecksumType;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.sapi.fs.VfsFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;

/**
 * A mutable interface of a virtual file.
 *
 * @author Yossi Shaul
 */
public interface MutableVfsFile extends MutableVfsItem<MutableFileInfo>, VfsFile<MutableFileInfo> {

    /**
     * Sets the client provided checksum. This value is provided by the user and might not be a valid checksum.
     *
     * @param type     The checksum type. Either sha1 or md5
     * @param checksum The user provided checksum value
     */
    void setClientChecksum(@Nonnull ChecksumType type, @Nullable String checksum);

    /**
     * Sets the client provided sha1 checksum. This value is provided by the user and might not be a valid checksum.
     * Checksum information is used to validate binaries as they are inserted into the Binarystore, therefor invalid
     * or mismatching client checksums will result in failing uploads
     *
     * @param sha1 The user provided sha1 checksum
     */
    void setClientSha1(@Nullable String sha1);

    /**
     * Sets the client provided sha256 checksum. This value is provided by the user and might not be a valid checksum.
     * Checksum information is used to validate binaries as they are inserted into the Binarystore, therefor invalid
     * or mismatching client checksums will result in failing uploads
     *
     * @param sha2 The user provided sha2 checksum
     */
    void setClientSha2(@Nullable String sha2);

    /**
     * Sets the client provided md5 checksum. This value is provided by the user and might not be a valid checksum.
     * Checksum information is used to validate binaries as they are inserted into the Binarystore, therefor invalid
     * or mismatching client checksums will result in failing uploads
     *
     * @param md5 The user provided sha1 checksum
     */
    void setClientMd5(String md5);

    /**
     * Check if this file is originally new in the current transaction (i.e. isNew was true before it was saved)
     * @return {@code true} if the file was originally new, {@code false} otherwise
     */
    boolean isOriginallyNew();

    /**
     * Used to fill actual checksum and length information
     * Use {@param info} to enforce checksum validation when the Binarystore writes this steam.
     */
    void fillBinaryData(InputStream in);

    /**
     * Sets the statistics data on this mutable file. Used only during import.
     *
     * @param statsInfo The stats info to set on this file
     */
    void setStats(StatsInfo statsInfo);

    /**
     * Fills the non-content dependent fields (real checksums) from the source info.
     *
     * @param sourceInfo The source file info to read data from
     */
    void fillInfo(FileInfo sourceInfo);

    /**
     * Automatically adds a binary record to the database if binary matching the given sha1 exists in the binary
     * provider. This method is required to support skeleton import.
     *
     * @param sha1   The binary sha1 checksum
     * @param sha2   The binary sha2 checksum
     * @param md5    The binary md5 checksum
     * @param length The length of the binary
     * @return True if the record exists or was added successfully
     */
    boolean tryUsingExistingBinary(String sha1, String sha2, String md5, long length);

    /**
     * Check whether the checksum of the content this file holds changed in the current transaction.
     * Note- For new files, the checksum has changed (comparing no checksum to the new checksum), {@code true} is returned.
     * @return {@code true} if the content changed, {@code false} otherwise
     */
    boolean isChecksumChanged();
}
