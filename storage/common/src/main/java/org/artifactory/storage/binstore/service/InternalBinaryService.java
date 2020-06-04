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

package org.artifactory.storage.binstore.service;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.storage.StorageException;
import org.jfrog.storage.binstore.exceptions.BinaryNotFoundException;
import org.jfrog.storage.binstore.ifc.BinaryProviderManager;
import org.jfrog.storage.binstore.ifc.ProviderConnectMode;
import org.jfrog.storage.binstore.ifc.UsageTracking;
import org.jfrog.storage.binstore.ifc.model.StorageInfo;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Date: 12/11/12
 * Time: 2:13 PM
 *
 * @author freds
 */
public interface InternalBinaryService extends BinaryService, UsageTracking {

    /**
     * Add the binary data line to this data store based on all params after verification
     * from binary provider delegate.
     * Verification steps depends on configuration and delegates answers.
     * Method will returns null if no binary object with specified info is found,
     * or no filestore delegate was configured.
     *
     * @param sha1   The SHA1 key value
     * @param md5    The md5 of this new entry
     * @param length The length of the binary
     * @return The full binary info object created and stored, or null if no object
     * @throws BinaryNotFoundException if verification failed
     */
    @Nullable
    BinaryInfo addBinaryRecord(String sha1, String sha2, String md5, long length) throws BinaryNotFoundException;

    /**
     * Create the entry in DB for this binary if it does not exists already.
     * Returns the actual entry created.
     * Open and close a DB TX specifically for this.
     *
     * @return the actual entry in DB matching the inputs
     * @throws StorageException
     */
    @Nonnull
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    BinaryInfo insertRecordInDb(String sha1, String sha2, String md5, long length) throws StorageException;


    /**
     * Methos to use garbage collection.
     */
    void startGarbageCollect();

    /**
     * Activate Garbage Collection for this binary store
     *
     * @return The results of the GC process
     */
    GarbageCollectorInfo runFullGarbageCollect();

    /**
     * Run all (trash + binaries) garbage collection strategies
     */
    void runAllGarbageCollectStrategies();

    void deleteBlobInfos(List<String> deletedBinaries);

    /**
     * Delete all left over folders and files in the binaries directory that are not declared
     * at all in this binary store.
     *
     * @param statusHolder A status holder of the process messages
     */
    void prune(BasicStatusHolder statusHolder);

    /**
     * The actual local directory where checksum binary files will be stored.
     *
     * @return the folder for this binary store
     * @deprecated Cannot be used anymore
     */
    @Nullable
    @Deprecated
    File getBinariesDir();

    /**
     * Checks if the required binary provider is configured in the binary provider chain currently loaded and in use.
     * @param type the type attribute of the binary provider. For example: "s3" (<provider id="s3" type="s3">)
     */
    boolean isProviderConfigured(String type);

    /**
     * Map each file provider to it's filestore folder
     *;
     * @return A map of each filestore folder mapped to it's provider
     */
    StorageInfo getStorageInfoSummary();

    BinaryProviderManager getBinaryProviderManager();

    void addGCListener(GarbageCollectorListener garbageCollectorListener);

    /**
     * Add an external checksum filestore that can be used in read only mode.
     * This will create an external filestore binary provider
     * at the end of binary provider chain. So, if no binary found it will be used
     * in read only mode to provide the data stream.
     * The root directory provided should have the Artifactory checksum filestore layout:
     * [2 first characters of the sha1]/[the full sha1]
     * <p>
     * No files will be deleted from the external filestore except if the connectMode
     * is specified to MOVE mode.
     *
     * @param externalDir The root directory of the files respecting a checksum filestore
     * @param connectMode Define the way the streams and files of the external filestore
     *                    should be handled by this binary store
     */
    void addExternalFileStore(File externalDir, ProviderConnectMode connectMode);

    /**
     * This will disconnect from the chain an existing external filestore added or configured.
     * Before being disconnected, the disconnectMode is going to be used to copy or move
     * all the files from the external filestore to this binary store.
     *
     * @param externalDir    The exact same root directory used previously to add the external filestore
     * @param disconnectMode The mode of disconnection to manage all the files
     *                       needed by this binary store and present in the external filestore
     * @param statusHolder   A collection of messages about the status of the disconnection
     */
    void disconnectExternalFilestore(File externalDir, ProviderConnectMode disconnectMode, BasicStatusHolder statusHolder);

    Collection<BinaryInfo> findAllBinaries();

    /**
     * Deletes unreferenced archive paths. Shared archive paths might not be used after a binary is deleted.
     */
    @Transactional
    int deleteUnusedArchivePaths();

    /**
     * Deletes unreferenced archive names. Shared archive names might not be used after a binary is deleted.
     */
    @Transactional
    int deleteUnusedArchiveNames();

    /**
     * Increments the active users of a certain binary to prevent deletion while still in usage.
     *
     * @param sha1 The sha1 checksum to protect
     */
    @Override
    int incrementNoDeleteLock(String sha1);

    /**
     * Decrements the active users of a certain binary. Indicates that the active usage was ended.
     *
     * @param sha1 The sha1 checksum to remove protection from
     */
    @Override
    void decrementNoDeleteLock(String sha1);

    /**
     * BEWARE - this method is meant for a very specific usage - the sha2 db conversion.
     * NEVER EVER touch the binaries table outside of normal flow!
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    boolean updateSha2ForSha1(String targetSha1, String newSha2) throws SQLException;

    /**
     * Checks whether the requested {@param sha1} is in cloud provider - via cache of sha1 string only
     * (not the actual artifact), and verifies that redirect are allowed in that cloud provider.
     * Going out to real cloud provider only in case cache doesn't hold the sha1 in it.
     */
    boolean canCloudProviderGenerateRedirection(String sha1);
}
