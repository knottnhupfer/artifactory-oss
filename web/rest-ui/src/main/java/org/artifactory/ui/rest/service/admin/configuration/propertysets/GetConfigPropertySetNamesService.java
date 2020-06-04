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

package org.artifactory.ui.rest.service.admin.configuration.propertysets;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.PropertySetNameModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetConfigPropertySetNamesService implements RestService {

    private static final String INCOMING_FROM_REPO_FORM = "isRepoForm";

    @Autowired
    private CentralConfigService configService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        response.iModelList(configService.getDescriptor().getPropertySets().stream()
                //UI doesn't show the artifactory. prop set and non-visible sets
                .filter(filterPropSetsByCallingScreen(request.getQueryParamByKey(INCOMING_FROM_REPO_FORM)))
                .map(PropertySetNameModel::new)
                .collect(Collectors.toList()));
    }

    /**
     * The Property Set config screen(in admin) does not show the 'artifactory' property set.
     * The repo wizard does show it as a valid selection but not all other invisible prop sets.
     */
    private Predicate<PropertySet> filterPropSetsByCallingScreen(String isRepoFormParam) {
        if(Boolean.valueOf(isRepoFormParam)) {
            return propertySet -> propertySet.isVisible() || propertySet.getName().equals(PropertySet.ARTIFACTORY_RESERVED_PROP_SET);
        }
        return propertySet -> !propertySet.getName().equals(PropertySet.ARTIFACTORY_RESERVED_PROP_SET);
    }
}
