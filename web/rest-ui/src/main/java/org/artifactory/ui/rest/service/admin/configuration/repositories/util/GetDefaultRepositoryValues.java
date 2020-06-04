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

package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.PropertySetNameModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.info.RepositoryDefaultValuesModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteNetworkRepositoryConfigModel;
import org.artifactory.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetDefaultRepositoryValues implements RestService<RepositoryDefaultValuesModel> {

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest<RepositoryDefaultValuesModel> request, RestResponse response) {
        RepositoryDefaultValuesModel defaultValues = new RepositoryDefaultValuesModel();
        //Add artifactory prop set as default
        configService.getDescriptor().getPropertySets().stream()
                .filter(propertySet -> propertySet.getName().equals(PropertySet.ARTIFACTORY_RESERVED_PROP_SET))
                .findAny().ifPresent(
                artPropSet -> {
                    ((LocalAdvancedRepositoryConfigModel) defaultValues.getDefaultModels()
                            .get("localAdvanced")).setPropertySets(Lists.newArrayList(new PropertySetNameModel(artPropSet)));
                    ((RemoteAdvancedRepositoryConfigModel) defaultValues.getDefaultModels()
                            .get("remoteAdvanced")).setPropertySets(Lists.newArrayList(new PropertySetNameModel(artPropSet)));
                });
        populateRemoteNetworkWithCertificates(defaultValues);
        response.iModel(defaultValues);
    }

    private void populateRemoteNetworkWithCertificates(RepositoryDefaultValuesModel defaultValues) {
        RemoteNetworkRepositoryConfigModel network = (RemoteNetworkRepositoryConfigModel) defaultValues
                .getDefaultModels().get("network");
        ArtifactWebstartAddon webstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
        List<String> certAliases = webstartAddon.getSslCertNames();
        if (CollectionUtils.notNullOrEmpty(certAliases)) {
            network.setInstalledCertificatesList(certAliases);
        }
    }
}
