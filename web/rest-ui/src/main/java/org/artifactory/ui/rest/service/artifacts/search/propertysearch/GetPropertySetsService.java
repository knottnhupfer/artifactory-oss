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

package org.artifactory.ui.rest.service.artifacts.search.propertysearch;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.propertysearch.PropertyKeyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetPropertySetsService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        // fetch properties key values
        List<PropertyKeyValues> propertyKeyValues = fetchPropertyKeyValues();
        // update response
        response.iModelList(propertyKeyValues);
    }

    /**
     * get properties ket values
     *
     * @return list of property Key values
     */
    private List<PropertyKeyValues> fetchPropertyKeyValues() {
        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
        List<PropertySet> availablePropertySets = descriptor.getPropertySets();
        List<PropertyKeyValues> propertyKeyValues = new ArrayList<>();
        availablePropertySets.forEach(propertySet ->
                        propertySet.getProperties().forEach(property -> {
                                    PropertyKeyValues keyValues = new PropertyKeyValues(
                                            propertySet.getName(), property);
                                    keyValues.setPropertyType(property.getPropertyType().name());
                                    propertyKeyValues.add(
                                            keyValues);
                                }
                        )
        );
        return propertyKeyValues;
    }
}
