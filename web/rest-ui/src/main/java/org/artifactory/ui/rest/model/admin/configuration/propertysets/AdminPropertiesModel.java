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

package org.artifactory.ui.rest.model.admin.configuration.propertysets;

import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertyType;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jfrog.common.config.diff.DiffIgnore;

/**
 * @author Dan Feldman
 */
@JsonIgnoreProperties({"closedPredefinedValues", "multipleChoice", "getFormattedValues", "valueCount"})
public class AdminPropertiesModel extends Property implements RestModel {

    @DiffIgnore
    public PropertyType propertyType; //Will be null - gets populated during serialization chain

    public AdminPropertiesModel() {
    }

    public AdminPropertiesModel(Property that) {
        this.setName(that.getName());
        this.setPredefinedValues(that.getPredefinedValues());
        this.setPropertyType(that.getPropertyType().toString());
    }

    public Property toProperty() {
        return this;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
