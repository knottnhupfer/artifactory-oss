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

import org.jfrog.storage.binstore.ifc.model.BinaryProvidersInfo;
import org.jfrog.storage.binstore.ifc.model.BinaryTreeElement;
import org.jfrog.storage.common.StorageUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Summary of the storage used by Artifactory on the filesystem.
 * This is usually either the binaries filestore or the cache folder.
 *
 * @author Yossi Shaul
 */
public class FileStoreStorageSummary implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(FileStoreStorageSummary.class);
    private final List<File> binariesFolders;
    private long freeSpace;
    private long totalSpace;
    private long usedSpace;
    private long cacheSize;
    private String binariesStorageTemplate;

    public FileStoreStorageSummary(List<File> binariesFolders, BinaryProvidersInfo<Map<String, String>> binaryProvidersInfo) {
        this.binariesFolders = binariesFolders;
        binariesStorageTemplate = binaryProvidersInfo.template;
        if (binariesFolders != null && !binariesFolders.isEmpty()) {
            freeSpace = binariesFolders.stream().mapToLong(File::getFreeSpace).sum();
            totalSpace = binariesFolders.stream().mapToLong(File::getTotalSpace).sum();
            usedSpace = totalSpace - freeSpace;
        } else {
            freeSpace = 0;
            totalSpace = 0;
            usedSpace = 0;
        }

        if (isFullDb(binaryProvidersInfo) || isCacheFs(binaryProvidersInfo)) {
            cacheSize = getFirstCacheMaxSize(binaryProvidersInfo);
        } else {
            cacheSize = -1L;
        }
    }

    /**
     * get first maxCacheSize value
     */
    private static long getFirstCacheMaxSizeInternal(BinaryTreeElement<Map<String, String>> treeElement) {
        try {
            if (treeElement == null) {
                return -1;
            }
            Map<String, String> data = treeElement.getData();
            String type = data.get("type");
            if ("cache-fs".equals(type)) {
                String maxCacheSize = data.get("maxCacheSize");
                return StorageUnit.getStorageUnitValue(maxCacheSize);
            }
            getFirstCacheMaxSizeInternal(treeElement.getNextBinaryTreeElement());
            for (BinaryTreeElement<Map<String, String>> elements : treeElement.getSubBinaryTreeElements()) {
                getFirstCacheMaxSizeInternal(elements);
            }
        } catch (Exception e) {
            log.error("Failed to resolve max cache size from the BinaryProvidersInfo.", e);
        }
        return -1;
    }

    private long getFirstCacheMaxSize(BinaryProvidersInfo<Map<String, String>> binaryProvidersInfo) {
        return getFirstCacheMaxSizeInternal(binaryProvidersInfo.rootTreeElement);
    }

    private boolean isCacheFs(BinaryProvidersInfo<Map<String, String>> binaryProvidersInfo) {
        return "cache-fs".equals(binaryProvidersInfo.template);
    }

    private boolean isFullDb(BinaryProvidersInfo<Map<String, String>> binaryProvidersInfo) {
        return "full-db".equals(binaryProvidersInfo.template);
    }

    /**
     * @return The type of the binaries storage (filesystem, full db etc.)
     */
    public String getBinariesStorageType() {
        return binariesStorageTemplate;
    }

    /**
     * @return The location on the filesystem storing the binaries (either the filestore or the cache). Might be null
     * when configured to use full db without a cache
     */
    @Nullable
    public List<File> getBinariesFolders() {
        return binariesFolders;
    }

    /**
     * @return The total space in bytes on the device containing {@link FileStoreStorageSummary#getBinariesFolders()}
     */
    public long getTotalSpace() {
        return totalSpace;
    }

    /**
     * @return The free space, in bytes, on the device containing {@link FileStoreStorageSummary#getBinariesFolders()}
     */
    public long getUsedSpace() {
        return usedSpace;
    }

    /**
     * @return The free space, in bytes, on the device containing {@link FileStoreStorageSummary#getBinariesFolders()}
     */
    public long getFreeSpace() {
        return freeSpace;
    }

    /**
     * @return Used space fraction
     */
    public double getUsedSpaceFraction() {
        return (double) usedSpace / totalSpace;
    }

    /**
     * @return Free space fraction
     */
    public double getFreeSpaceFraction() {
        return (double) freeSpace / totalSpace;
    }

    public long getCacheSize() {
        return cacheSize;
    }

    protected void setCacheSize(long cacheSize) {
        this.cacheSize = cacheSize;
    }

}
