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

package org.artifactory.storage.db.binstore.visitors;

import org.jfrog.storage.binstore.ifc.BinaryProviderInfo;
import org.jfrog.storage.binstore.ifc.model.BinaryTreeElement;
import org.jfrog.storage.binstore.ifc.model.StorageInfo;
import org.jfrog.storage.binstore.providers.base.CacheStorageInfoImpl;

import java.util.Map;

/**
 * @author gidis
 */
public class EssentialBinaryTreeElementHandler implements BinaryTreeElementHandler<BinaryProviderInfo, Map<String, String>> {
    public static boolean isDisplayable(BinaryProviderInfo data) {
        String type = data.getProperties().get("type");
        switch (type) {
            case "empty": {
                return false;
            }
            case "eventual": {
                return false;
            }
            case "retry": {
                return false;
            }
            case "tracking": {
                return false;
            }
            case "external-wrapper": {
                return false;
            }
            default: {
                return true;
            }
        }
    }

    @Override
    public Map<String, String> visit(BinaryTreeElement<BinaryProviderInfo> binaryTreeElement) {
        BinaryProviderInfo data = binaryTreeElement.getData();
        boolean displayable = isDisplayable(data);
        if (displayable) {
            StorageInfo storageInfo = data.getStorageInfo();
            data.addProperty("freeSpace", toString(storageInfo.getFreeSpace()));
            data.addProperty("usageSpace", "" + toString(storageInfo.getUsedSpace()));
            data.addProperty("totalSpace", "" + toString(storageInfo.getTotalSpace()));
            data.addProperty("usageSpaceInPercent", "" + toString(storageInfo.getUsageSpaceInPercent()));
            data.addProperty("freeSpaceInPercent", "" + toString(storageInfo.getFreeSpaceInPercent()));
            if (storageInfo instanceof CacheStorageInfoImpl) {
                CacheStorageInfoImpl cacheStorageInfo = (CacheStorageInfoImpl) storageInfo;
                data.addProperty("freeCacheSpace", toString(cacheStorageInfo.getFreeCacheSpace()));
                data.addProperty("usageCacheSpace", toString(cacheStorageInfo.getUsedCacheSpace()));
                data.addProperty("oldestBinaryAge", toString(cacheStorageInfo.getOldestBinaryAge()));
            }
            return data.getProperties();
        }
        return null;
    }

    private String toString(Long number) {
        return Long.MAX_VALUE == number ? "infinite" : -1 == number ? "unsupported" : "" + number;
    }
}
