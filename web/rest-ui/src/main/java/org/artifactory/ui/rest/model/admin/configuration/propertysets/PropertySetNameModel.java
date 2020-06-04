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

import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Dan Feldman
 */
public class PropertySetNameModel extends BaseModel {

    String name;
    Integer propertiesCount;

    public PropertySetNameModel() {

    }

    public PropertySetNameModel(String name, int propertiesCount) {
        this.name = name;
        this.propertiesCount = propertiesCount;
    }

    public PropertySetNameModel(PropertySet propertySet) {
        this.name = propertySet.getName();
        this.propertiesCount = propertySet.getProperties().size();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPropertiesCount() {
        return propertiesCount;
    }

    public void setPropertiesCount(Integer count) {
        this.propertiesCount = count;
    }
}
