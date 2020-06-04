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

package org.artifactory.ui.rest.model.artifacts.search.propertysearch;

import org.artifactory.descriptor.property.Property;
import org.artifactory.rest.common.model.BaseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
public class PropertyKeyValues extends BaseModel {
    private String key;
    private List<String> values = new ArrayList<>();
    private String propertyType;

    PropertyKeyValues() {
    }

    public PropertyKeyValues(String propertySetName, Property property) {
        updateProps(propertySetName, property);

    }

    /**
     * update props data
     *
     * @param propertySetName - property Set Name
     * @param property        - property instance
     */
    private void updateProps(String propertySetName, Property property) {
        if (propertySetName != null) {
            this.key = propertySetName + "." + property.getName();
        }else{
            key = property.getName();
        }
        property.getPredefinedValues().forEach(preValue -> values.add(preValue.getValue()));
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }
}
