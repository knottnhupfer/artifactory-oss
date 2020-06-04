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

package org.artifactory.ui.rest.service.admin.configuration.proxies;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.rest.common.model.proxies.ProxiesModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.util.AolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteProxyService<T extends ProxiesModel> implements RestService<T> {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        AolUtils.assertNotAol("DeleteProxy");
        T model = request.getImodel();
        for (String proxyKey : model.getProxyKeys()) {
            // remove proxy from config descriptor
            deleteProxy(proxyKey);
            // update response feedback
        }
        if(model.getProxyKeys().size()>1){
            response.info("Successfully removed "+model.getProxyKeys().size()+" proxies");
        }else if(model.getProxyKeys().size()==1){
            response.info("Successfully removed proxy '" + model.getProxyKeys().get(0) + "'");
        }
    }

    /**
     * remove proxy from config descriptor
     *
     * @param proxyKey - proxy key
     */
    private void deleteProxy(String proxyKey) {
        MutableCentralConfigDescriptor configDescriptor = centralConfigService.getMutableDescriptor();
        configDescriptor.removeProxy(proxyKey);
        centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
    }
}
