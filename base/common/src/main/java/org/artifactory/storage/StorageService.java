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

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.Async;
import org.artifactory.api.storage.StorageQuotaInfo;
import org.jfrog.storage.binstore.ifc.model.BinaryProvidersInfo;

import java.util.Map;

/**
 * @author yoavl
 */
public interface StorageService {

    void compress(BasicStatusHolder statusHolder);

    boolean isDerbyUsed();

    void logStorageSizes();

    /**
     * Check that manual garbage collection can be run, and then activate the double GC asynchronously.
     */
    void callManualGarbageCollect(BasicStatusHolder statusHolder);

    void pruneUnreferencedFileInDataStore(BasicStatusHolder statusHolder);

    void ping();


    BinaryProvidersInfo<Map<String, String>> getBinaryProviderInfo();

    /**
     * Create and retrieve a storage quota info object which contains information about
     * the system storage total space, free space etc.
     *
     * @param fileContentLength The uploaded file content length to include in the quota calculation
     * @return The {@link org.artifactory.api.storage.StorageQuotaInfo} object, might return null if quota management doesn't exist inside
     * the central config or it is disabled.
     */
    StorageQuotaInfo getStorageQuotaInfo(long fileContentLength);

    /**
     * Creates a summary of artifacts storage, including number of items and size per repository.
     */
    StorageSummaryInfo getStorageSummaryInfo();

    /**
     * Returns a summary of given artifacts storage, including number of items and size per repository, from the cache.
     */
    StorageSummaryInfo getStorageSummaryInfoFromCache();

    /**
     * Force optimization once even if there is no need for optimization (For immigration).
     */
    void forceOptimizationOnce();

    /**
     * Creates a summary of the filestore storage used by Artifactory.
     *
     * @return a filestore usage summary
     */
    FileStoreStorageSummary getFileStoreStorageSummary();

    /**
     * Calculates a summary of given artifacts storage, including number of items and size per repository.
     */
    void calculateStorageSummary();

    /**
     * Calculates asynchronously a summary of given artifacts storage, including number of items and size per repository.
     */
    @Async
    void calculateStorageSummaryAsync();

    /**
     * Calculates asynchronously a summary of given artifacts storage, including number of items and size per repository.
     * Called on startup. INTERNAL method. Do not use.
     */
    @Async
    void calculateStorageSummaryAsyncOnStartup();

    /**
     * Update storage summary cache for HA propagation
     * @param summary - the new updated cache
     */
    void updateStorageSummaryCache(StorageSummaryInfo summary);

    /**
     * Return the total size managed by this binary store from cache, otherwise access the database.
     *
     * @return The total size or -1 if the binary does not supports fetch of the full size.
     */
    long getCachedStorageSize();
}
