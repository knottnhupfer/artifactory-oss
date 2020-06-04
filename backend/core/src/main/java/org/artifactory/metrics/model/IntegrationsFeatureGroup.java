package org.artifactory.metrics.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.api.callhome.FeatureGroup;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * @author Dan Feldman
 */
@NoArgsConstructor
@Data
public class IntegrationsFeatureGroup extends FeatureGroup {

    private int count;

    public IntegrationsFeatureGroup(String name) {
        super(name);
    }

    @Builder
    public IntegrationsFeatureGroup(String name, Map<String, Object> attributes, List<FeatureGroup> features, int count) {
        this.name = name;
        this.attributes = attributes;
        this.features = features;
        this.count = count;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return (attributes == null || attributes.isEmpty())
                && (features == null || features.isEmpty())
                && count == 0
                && isBlank(name);
    }
}
