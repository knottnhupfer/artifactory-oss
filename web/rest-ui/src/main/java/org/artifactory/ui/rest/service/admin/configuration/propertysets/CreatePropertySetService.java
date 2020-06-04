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

import org.apache.http.HttpStatus;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.AdminPropertySetModel;
import org.jfrog.common.BiOptional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreatePropertySetService implements RestService<AdminPropertySetModel> {
    private static final Logger log = LoggerFactory.getLogger(CreatePropertySetService.class);

    @Autowired
    private CentralConfigService configService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AdminPropertySetModel toAdd = (AdminPropertySetModel) request.getImodel();
        MutableCentralConfigDescriptor descriptor = configService.getMutableDescriptor();
        BiOptional.of(descriptor.getPropertySets().stream()
                .filter(prop -> prop.getName().equals(toAdd.getName()))
                .findFirst())
                .ifNotPresent(() -> addPropertySet(descriptor, toAdd, response))
                .ifPresent(() -> error(response, toAdd.getName()));
    }

    private void addPropertySet(MutableCentralConfigDescriptor descriptor, AdminPropertySetModel propertySet,
            RestResponse artifactoryResponse) {
        descriptor.addPropertySet(propertySet.getPropertySetFromModel());
        configService.saveEditedDescriptorAndReload(descriptor);
        String msg = "Successfully add property Set '" + propertySet.getName() + "'";
        log.info(msg);
        artifactoryResponse.info(msg).responseCode(HttpStatus.SC_CREATED);
    }

    private void error(RestResponse response, String propName) {
        String msg = "Property set '" + propName + "' already exists";
        log.debug(msg + " canceling create operation");
        response.responseCode(HttpStatus.SC_BAD_REQUEST).error(msg);
    }
}
