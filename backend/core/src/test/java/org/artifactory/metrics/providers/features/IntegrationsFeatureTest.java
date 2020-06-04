package org.artifactory.metrics.providers.features;

import org.artifactory.api.callhome.FeatureGroup;
import org.artifactory.metrics.exception.MaxSizeExceededException;
import org.artifactory.metrics.model.FeatureUsage;
import org.artifactory.metrics.model.IntegrationsProductUsage;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author shivaramr
 */
@Test
public class IntegrationsFeatureTest {


    IntegrationsFeature integrationsFeature = new IntegrationsFeature();

    @Test(expectedExceptions = MaxSizeExceededException.class, expectedExceptionsMessageRegExp = ".*no more data will be aggregated.*")
    public void testMaxProductSize() throws MaxSizeExceededException {
        //insert products that exceeds the limit
        for (int i = 0; i <= 100; i++) {
            IntegrationsProductUsage integrationsProductUsage = new IntegrationsProductUsage();
            integrationsProductUsage.setProductId("test" + Integer.toString(i));

            List<FeatureUsage> featureUsages = new ArrayList<>();
            featureUsages.add(new FeatureUsage("testfeature", 1, null));
            integrationsProductUsage.setFeatures(featureUsages);

            integrationsFeature.addUsageData(integrationsProductUsage);
        }

    }

    @Test(expectedExceptions = MaxSizeExceededException.class, expectedExceptionsMessageRegExp = ".*Max capacity is .*")
    public void testMaxFeaturesSize() throws MaxSizeExceededException {
        //insert features per product that exceeds the limit
        integrationsFeature.clearData();
        IntegrationsProductUsage integrationsProductUsage = new IntegrationsProductUsage();
        integrationsProductUsage.setProductId("test");
        List<FeatureUsage> featureUsages = new ArrayList<>();

        for (int i = 0; i <= 100; i++) {

            featureUsages.add(new FeatureUsage("featureId" + Integer.toString(i), 1, null));
            integrationsProductUsage.setFeatures(featureUsages);

        }
        integrationsFeature.addUsageData(integrationsProductUsage);

    }

    @Test(expectedExceptions = MaxSizeExceededException.class, expectedExceptionsMessageRegExp = ".*Total length of attributes string is capped at.*")
    public void testMaxAttributeSize() throws MaxSizeExceededException {
        //insert attributes per feature that exceed limit
        integrationsFeature.clearData();
        IntegrationsProductUsage integrationsProductUsage = new IntegrationsProductUsage();
        integrationsProductUsage.setProductId("test");
        List<FeatureUsage> featureUsages = new ArrayList<>();
        Map<String, Object> attributes = new HashMap<>();
        StringBuffer jsonBuffer = new StringBuffer().append("{");
        for (int i = 0; i < 10000; i++) {
            String fieldName = "field" + Integer.toString(i);
            String value = "value" + Integer.toString(i);
            jsonBuffer.append("\"");
            jsonBuffer.append(fieldName);
            jsonBuffer.append("\":");
            jsonBuffer.append("\"");
            jsonBuffer.append(value);
            if (i != 10000)
                jsonBuffer.append("\",");

        }
        jsonBuffer.append("}");
        attributes.put("featureData", jsonBuffer.toString());
        featureUsages.add(new FeatureUsage("featureId", 1, attributes));
        integrationsProductUsage.setFeatures(featureUsages);

        integrationsFeature.addUsageData(integrationsProductUsage);

    }

    @Test()
    public void testAddUsage() throws MaxSizeExceededException {
        integrationsFeature.clearData();

        IntegrationsProductUsage integrationsProductUsage = new IntegrationsProductUsage();
        integrationsProductUsage.setProductId("test");

        List<FeatureUsage> featureUsages = new ArrayList<>();
        Map<String, Object> attributes = new HashMap<>();
        Map<String, Object> attributeData = new HashMap<>();
        attributeData.put("field", "value");
        attributes.put("testfeatureData", attributeData);
        featureUsages.add(new FeatureUsage("testfeature", 1, attributes));
        integrationsProductUsage.setFeatures(featureUsages);


        integrationsFeature.addUsageData(integrationsProductUsage);
        assert(integrationsFeature.productUsageMap.size() == 1);
        Map<String, FeatureUsage> featureUsagesInMap = integrationsFeature.productUsageMap.get("test");
        FeatureUsage featureUsageInMap = featureUsagesInMap.get("testfeature");

        assert(featureUsageInMap.getAttributes() != null);
        assert(featureUsageInMap.getAttributes().containsKey("testfeatureData"));

        integrationsFeature.clearData();
        assert(integrationsFeature.productUsageMap.size() == 0);

    }

    public void testAggregatedCount() throws MaxSizeExceededException {
        integrationsFeature.clearData();

        IntegrationsProductUsage integrationsProductUsage1 = new IntegrationsProductUsage();
        integrationsProductUsage1.setProductId("test");
        List<FeatureUsage> featureUsages1 = new ArrayList<>();
        featureUsages1.add(new FeatureUsage("testfeature", 0, null));
        integrationsProductUsage1.setFeatures(featureUsages1);
        integrationsFeature.addUsageData(integrationsProductUsage1);

        IntegrationsProductUsage integrationsProductUsage2 = new IntegrationsProductUsage();
        integrationsProductUsage2.setProductId("test");
        Map<String, Object> attributes = new HashMap<>();
        Map<String, Object> attributeData = new HashMap<>();
        attributeData.put("field", "value");
        attributes.put("featureData", attributeData);
        List<FeatureUsage> featureUsages2 = new ArrayList<>();
        featureUsages2.add(new FeatureUsage("testfeature", 3, attributes));
        integrationsProductUsage2.setFeatures(featureUsages2);
        integrationsFeature.addUsageData(integrationsProductUsage2);

        // Number of entries should still be 1
        assert(integrationsFeature.productUsageMap.size() == 1);

        Map<String, FeatureUsage> featureUsagesInMap = integrationsFeature.productUsageMap.get("test");
        FeatureUsage featureUsageInMap = featureUsagesInMap.get("testfeature");

        // Count is summation of 3 + 1(for 0)
        assert(featureUsageInMap.getCount() == 4);

        // first time, attribue = null, second time it has featureData
        assert(featureUsageInMap.getAttributes() != null);
        assert(featureUsageInMap.getAttributes().containsKey("featureData"));

        integrationsFeature.clearData();
        assert(integrationsFeature.productUsageMap.size() == 0);

    }

}
