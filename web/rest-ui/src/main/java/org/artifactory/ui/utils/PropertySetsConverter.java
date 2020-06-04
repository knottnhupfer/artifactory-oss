package org.artifactory.ui.utils;

import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.PropertiesArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.RepoProperty;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.RepoPropertySet;
import org.jfrog.common.StreamSupportUtils;

import java.util.List;
import java.util.stream.Collectors;

public class PropertySetsConverter {

    public static List<PropertiesArtifactInfo> toPropertiesArtifactInfoList(List<PropertySet> propertySets) {
        return StreamSupportUtils.stream(propertySets)
                .flatMap(propertySet -> StreamSupportUtils.stream(propertySet.getProperties())
                        .map(property -> toPropertiesArtifactInfo(property, propertySet)))
                .collect(Collectors.toList());
    }

    public static List<PropertiesArtifactInfo> toPropertiesArtifactInfo(PropertySet propertySet) {
        return StreamSupportUtils.stream(propertySet.getProperties())
                        .map(property -> toPropertiesArtifactInfo(property, propertySet))
                .collect(Collectors.toList());
    }

    public static PropertiesArtifactInfo toPropertiesArtifactInfo(Property property, PropertySet propertySet){
        PropertySet repoPropertySet = new RepoPropertySet();
        repoPropertySet.setName(propertySet.getName());
        Property repoProperty = new RepoProperty();
        repoProperty.setName(property.getName());
        PropertiesArtifactInfo item = new PropertiesArtifactInfo(repoPropertySet, repoProperty);
        item.setPropertyType(property.getPropertyType().name());
        return item;
    }

}


