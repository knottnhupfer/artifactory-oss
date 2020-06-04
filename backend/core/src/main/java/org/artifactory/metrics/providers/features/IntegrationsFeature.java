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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.artifactory.api.callhome.FeatureGroup;
import org.artifactory.metrics.exception.MaxSizeExceededException;
import org.artifactory.metrics.model.FeatureUsage;
import org.artifactory.metrics.model.IntegrationsFeatureGroup;
import org.artifactory.metrics.model.IntegrationsProductUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class aggregates usage data incoming from various clients.
 *
 * @author shivaramr
 */
@Component
public class IntegrationsFeature implements CallHomeFeature {
    private static final Logger log = LoggerFactory.getLogger(IntegrationsFeature.class);

    private static final int MAX_PRODUCT_IDS_IN_MAP = 100;
    private static final int MAX_FEATURES_PER_PRODUCT = 100;
    private static final int MAX_ATTRIBUTES_SIZE_PER_PRODUCT = 10000;

    Map<String, Map<String, FeatureUsage>> productUsageMap = new ConcurrentHashMap<>();

    @Override
    public FeatureGroup getFeature() {
        IntegrationsFeatureGroup integrationsFeature = new IntegrationsFeatureGroup("Integrations");
        for (Map.Entry<String, Map<String, FeatureUsage>> entry : productUsageMap.entrySet()) {
            String productId = entry.getKey();
            Map<String, FeatureUsage> value = entry.getValue();
            FeatureGroup currentProductFeature = new FeatureGroup(productId);
            for (Map.Entry<String, FeatureUsage> usageEntry : value.entrySet()) {
                addUsageDataToCurrentFeatureGroup(integrationsFeature, currentProductFeature, usageEntry);
            }
            integrationsFeature.addFeature(currentProductFeature);

        }
        if (integrationsFeature.isEmpty()) {
            log.debug("No Integration Usage data available for call home job.");
            //Returning null so there's no empty block in the json
            integrationsFeature = null;
        }
        return integrationsFeature;
    }

    private void addUsageDataToCurrentFeatureGroup(IntegrationsFeatureGroup integrationsFeature, FeatureGroup currentProductFeature,
                                            Map.Entry<String, FeatureUsage> usageEntry) {
        String featureId = usageEntry.getKey();
        FeatureUsage productFeatureUsageCount = usageEntry.getValue();
        currentProductFeature.addFeature(
                IntegrationsFeatureGroup.builder()
                        .name(featureId)
                        .count(productFeatureUsageCount.getCount())
                        .attributes(productFeatureUsageCount.getAttributes())
                        .build()
        );
    }

    public void addUsageData(IntegrationsProductUsage productUsageData) throws MaxSizeExceededException {
        int totalAttributeLength = 0;
        String productId = productUsageData.getProductId();
        Map<String, FeatureUsage> allProductFeatures = getProductFeatures(productId);
        log.debug("Aggregating all usage data of product '{}'", productId);
        for (final FeatureUsage incomingUsage : productUsageData.getFeatures()) {
            FeatureUsage existingUsage = getFeatureUsage(productId, allProductFeatures, incomingUsage);
            totalAttributeLength += mergeUsageData(totalAttributeLength, incomingUsage, existingUsage);
        }
    }

    private Map<String, FeatureUsage> getProductFeatures(String productId) throws MaxSizeExceededException {
        Map<String, FeatureUsage> productFeatures = productUsageMap.get(productId);
        if (productFeatures == null) {
            assertProductMapSizeExceeded();
            productFeatures = new HashMap<>();
            productUsageMap.put(productId, productFeatures);
        }
        return productFeatures;
    }

    private FeatureUsage getFeatureUsage(String productId, Map<String, FeatureUsage> allProductFeatures, FeatureUsage incomingUsage) throws MaxSizeExceededException {
        String featureId = incomingUsage.getFeatureId();
        FeatureUsage featureUsage = allProductFeatures.get(featureId);
        if (featureUsage == null) {
            assertFeaturesMapSizeExceeded(allProductFeatures.size(), productId);
            featureUsage = new FeatureUsage();
            allProductFeatures.put(featureId, featureUsage);
        }
        return featureUsage;
    }

    private int mergeUsageData(int totalAttributeLength, FeatureUsage incomingUsageData, FeatureUsage existingUsageData) throws MaxSizeExceededException {
        String featureId = incomingUsageData.getFeatureId();
        log.debug("Adding usage data of featureId '{}' into map", featureId);
        mergeUsageCount(incomingUsageData, existingUsageData);
        Map<String, Object> incomingAttributes = incomingUsageData.getAttributes();
        if (incomingAttributes != null) {
            totalAttributeLength += getAttributeLength(incomingAttributes);
            assertTotalAttributeSizeExceeded(totalAttributeLength, featureId);
            existingUsageData.setAttributes(incomingAttributes);
        } else {
            Map<String, Object> existingAttributes = existingUsageData.getAttributes();
            if (existingAttributes != null) {
                totalAttributeLength += getAttributeLength(existingAttributes);
                assertTotalAttributeSizeExceeded(totalAttributeLength, featureId);
            }
        }

        return totalAttributeLength;
    }

    private int getAttributeLength(Map<String, Object> attribute) {
        //TODO think of a better way
        int length = 0;
        try {
            String jsonString = new ObjectMapper().writeValueAsString(attribute);
            length = jsonString.length();
        } catch (JsonProcessingException e) {
            log.debug("Failed to write feature usage as string: ", e);
        }
        return length;
    }

    private void mergeUsageCount(FeatureUsage incomingUsageData, FeatureUsage existingUsageData) {
        int count = incomingUsageData.getCount();
        if (count == 0) {
            count = 1;
        }
        existingUsageData.setCount(existingUsageData.getCount() + count);
    }

    private void assertTotalAttributeSizeExceeded(int totalAttributeLength, String featureId) throws MaxSizeExceededException {
        if (totalAttributeLength >= MAX_ATTRIBUTES_SIZE_PER_PRODUCT) {
            log.debug("Total attribute length for feature '{}' exceeds max allowed size of '{}' - canceling aggregation operation.",
                    featureId, MAX_ATTRIBUTES_SIZE_PER_PRODUCT);
            throw new MaxSizeExceededException("Total length of attributes string is capped at " + MAX_ATTRIBUTES_SIZE_PER_PRODUCT);
        }
    }

    private void assertProductMapSizeExceeded() throws MaxSizeExceededException {
        if (productUsageMap.size() >= MAX_PRODUCT_IDS_IN_MAP) {
            log.debug("Usage data map exceeded allowed size '{}' no new data will be aggregated until the next job run.",
                    MAX_PRODUCT_IDS_IN_MAP);
            throw new MaxSizeExceededException("Too much usage data is currently pending transmission, no more data will be aggregated until next job is run.");
        }
    }

    private void assertFeaturesMapSizeExceeded(int allProductFeaturesSize, String productId) throws MaxSizeExceededException {
        if (allProductFeaturesSize >= MAX_FEATURES_PER_PRODUCT) {
            log.debug("Usage data map for product '{}' exceeded allowed size '{}' no new data will be aggregated " +
                    "until the next job run.", productId, MAX_FEATURES_PER_PRODUCT);
            throw new MaxSizeExceededException("product '" + productId + "' is already aggregating too much feature " +
                    "data (Max capacity is " + MAX_FEATURES_PER_PRODUCT + "'. no new data will be aggregated until next job run.");
        }
    }

    @Override
    public void clearData() {
        productUsageMap.clear();
    }
}
