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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties;

import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.model.RestSpecialFields;
import org.artifactory.rest.common.service.IgnoreSpecialFields;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;
import org.codehaus.jackson.map.annotate.JsonFilter;

import java.util.List;
import java.util.Set;

/**
 * @author Chen Keinan
 */
@JsonFilter("exclude fields")
@IgnoreSpecialFields(value = {"multiValue"})
public class PropertiesArtifactInfo extends BaseArtifactInfo implements RestModel, RestSpecialFields {

    public PropertiesArtifactInfo(){}

    private PropertySet parent;
    private Property property;
    private List<String> names;
    private Set<String> selectedValues;
    private List<String> predefineValues;
    private List<ArtifactProperty> artifactProperties;
    private String propertyType;

    public PropertiesArtifactInfo(PropertySet propertySet, Property property) {
        this.parent = propertySet;
        this.property = property;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public List<ArtifactProperty> getArtifactProperties() {
        return artifactProperties;
    }

    public void setArtifactProperties(List<ArtifactProperty> artifactProperties) {
        this.artifactProperties = artifactProperties;
    }

    public List<String> getPredefineValues() {
        return predefineValues;
    }

    public void setPredefineValues(List<String> predefineValues) {
        this.predefineValues = predefineValues;
    }

    public String[] getSelectedValues()
    {
        if (selectedValues != null) {
            return selectedValues.toArray(new String[selectedValues.size()]);
        }
        else{
            return null;
        }
    }

    public void setSelectedValues(Set<String> selectedValues) {
        this.selectedValues = selectedValues;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public PropertySet getParent() {
        return parent;
    }

    public void setParent(PropertySet parent) {
        this.parent = parent;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToStringIgnoreSpecialFields(this);
    }

    @Override
    public boolean ignoreSpecialFields() {
        return artifactProperties!=null;
    }
}
