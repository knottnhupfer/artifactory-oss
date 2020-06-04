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

import org.artifactory.descriptor.property.PredefinedValue;
import org.artifactory.descriptor.property.Property;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearch;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
public class PropertySearch extends BaseSearch {

    private List<PropertyKeyValues> propertyKeyValues;

    public List<PropertyKeyValues> getPropertyKeyValues() {
        return propertyKeyValues;
    }

    public void setPropertyKeyValues(
            List<PropertyKeyValues> propertyKeyValues) {
        this.propertyKeyValues = propertyKeyValues;
    }

    public void updatePropertySearchData(String key, String value){
        Property property = new Property(key);
        List<PredefinedValue> predefinedValues = new ArrayList<>();
        predefinedValues.add(new PredefinedValue(value,true));
        property.setPredefinedValues(predefinedValues);
        PropertyKeyValues propertyKeyValues = new PropertyKeyValues(null,property);
        List<PropertyKeyValues> propertyKeyValuesList = new ArrayList<>();
        propertyKeyValuesList.add(propertyKeyValues);
        this.setPropertyKeyValues(propertyKeyValuesList);
    }
}
