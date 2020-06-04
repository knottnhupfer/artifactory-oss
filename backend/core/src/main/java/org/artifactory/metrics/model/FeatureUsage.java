package org.artifactory.metrics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author shivaramr
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeatureUsage {

    private String featureId;
    private int count;
    private Map<String, Object> attributes;

};
