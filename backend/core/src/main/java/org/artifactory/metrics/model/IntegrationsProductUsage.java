package org.artifactory.metrics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.List;



/**
 * * @author shivaramr
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationsProductUsage {
    private String productId;
    private List<FeatureUsage> features;
};


