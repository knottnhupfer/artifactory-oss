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

package org.artifactory.metrics.providers.features;

import org.artifactory.api.callhome.FeatureGroup;
import org.artifactory.api.storage.BinariesInfo;
import org.artifactory.api.storage.StorageQuotaInfo;
import org.artifactory.storage.StorageService;
import org.artifactory.storage.StorageSummaryInfo;
import org.artifactory.util.CollectionUtils;
import org.jfrog.storage.binstore.ifc.model.BinaryTreeElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.jfrog.common.FileUtils.bytesToMB;


/**
 * This class represent the storage feature group of the CallHome feature (see RTFACT-9621)
 *
 * @author nadavy
 */
@Component
public class StorageFeature implements CallHomeFeature {

    @Autowired
    private StorageService storageService;

    @Override
    public FeatureGroup getFeature() {
        BinaryTreeElement<Map<String, String>> binaryTreeRootElement = storageService
                .getBinaryProviderInfo().rootTreeElement;
        FeatureGroup storageFeature = new FeatureGroup("storage");
        addStorageSummaryFeature(storageFeature, binaryTreeRootElement);
        addStorageThresholdFeature(storageFeature);
        addStorageMountsFeature(storageFeature, binaryTreeRootElement);
        return storageFeature;
    }

    /**
     * Add general storage summary
     */
    private void addStorageSummaryFeature(FeatureGroup storageFeature,
            BinaryTreeElement<Map<String, String>> binaryTreeRootElement) {
        FeatureGroup summaryFeature = new FeatureGroup("summary");
        String storageType = binaryTreeRootElement.getData().get("type");
        summaryFeature.addFeatureAttribute("provider", storageType);
        StorageSummaryInfo storageSummaryInfo = storageService.getStorageSummaryInfo();
        if ("sharding".equals(storageType)) {
            int mounts = binaryTreeRootElement.getSubBinaryTreeElements().size();
            summaryFeature.addFeatureAttribute("mounts", mounts);
            String redundancy = binaryTreeRootElement.getData().get("redundancy");
            summaryFeature.addFeatureAttribute("redundancy", Integer.parseInt(redundancy));
        }
        BinariesInfo binariesInfo = storageSummaryInfo.getBinariesInfo();
        summaryFeature.addFeatureAttribute("binaries_count", binariesInfo.getBinariesCount());
        summaryFeature.addFeatureAttribute("artifacts_count", storageSummaryInfo.getTotalFiles());
        summaryFeature.addFeatureAttribute("items_count", storageSummaryInfo.getTotalItems());
        summaryFeature.addFeatureAttribute("binaries_size", bytesToMB(binariesInfo.getBinariesSize()));
        summaryFeature.addFeatureAttribute("artifacts_size", bytesToMB(storageSummaryInfo.getTotalSize()));
        summaryFeature.addFeatureAttribute("optimization", storageSummaryInfo.getOptimization() * 100); // percentage
        storageFeature.addFeature(summaryFeature);
    }

    /**
     * Add limit and warning thresholds (if available)
     */
    private void addStorageThresholdFeature(FeatureGroup storageFeature) {
        StorageQuotaInfo storageQuotaInfo = storageService.getStorageQuotaInfo(0);
        if (storageQuotaInfo != null) {
            FeatureGroup thresholdsFeature = new FeatureGroup("thresholds");
            thresholdsFeature.addFeatureAttribute("warning", storageQuotaInfo.getDiskSpaceWarningPercentage());
            thresholdsFeature.addFeatureAttribute("limit", storageQuotaInfo.getDiskSpaceLimitPercentage());
            storageFeature.addFeature(thresholdsFeature);
        }
    }

    /**
     * Add storage info for each mount (only on root level), or just the root if not available
     */
    private void addStorageMountsFeature(FeatureGroup storageFeature,
            BinaryTreeElement<Map<String, String>> binaryTreeRootElement) {
        List<BinaryTreeElement<Map<String, String>>> binaryTreeElements = binaryTreeRootElement
                .getSubBinaryTreeElements();
        if (CollectionUtils.isNullOrEmpty(binaryTreeElements)) {
            FeatureGroup mountFeature = addMountFeature(binaryTreeRootElement);
            storageFeature.addFeature(mountFeature);
        } else {
            // add storage info for each mount
            binaryTreeElements.stream()
                    .map(this::addMountFeature)
                    .forEach(storageFeature::addFeature);
        }
    }

    private FeatureGroup addMountFeature(BinaryTreeElement<Map<String, String>> element) {
        Map<String, String> binaryProviderData = element.getData();
        FeatureGroup summaryFeature = new FeatureGroup("mount " + binaryProviderData.get("id"));
        summaryFeature.addFeatureAttribute("label", binaryProviderData.get("id"));
        summaryFeature.addFeatureAttribute("type", binaryProviderData.get("type"));
        summaryFeature.addFeatureAttribute("used", getAttributeSizeInMb(binaryProviderData, "usageSpace"));
        summaryFeature.addFeatureAttribute("available", getAttributeSizeInMb(binaryProviderData, "freeSpace"));
        summaryFeature.addFeatureAttribute("size", getAttributeSizeInMb(binaryProviderData, "totalSpace"));
        return summaryFeature;
    }

    private long getAttributeSizeInMb(Map<String, String> binaryProviderData, String attribute) {
        long size = Long.parseLong(binaryProviderData.get(attribute));
        return bytesToMB(size);
    }

}
