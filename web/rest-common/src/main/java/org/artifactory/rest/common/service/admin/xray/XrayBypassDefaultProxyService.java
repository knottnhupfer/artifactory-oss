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

package org.artifactory.rest.common.service.admin.xray;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.XrayDescriptor;
import org.artifactory.rest.common.model.xray.XrayBypassSystemDefaultProxyModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Uriah Levy
 */
@Component
public class XrayBypassDefaultProxyService implements RestService {

    private final CentralConfigService configService;

    @Autowired
    public XrayBypassDefaultProxyService(CentralConfigService configService) {
        this.configService = configService;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        XrayBypassSystemDefaultProxyModel bypassDefaultProxy = (XrayBypassSystemDefaultProxyModel) request.getImodel();
        MutableCentralConfigDescriptor mutableDescriptor = configService.getMutableDescriptor();
        XrayDescriptor xrayConfig = mutableDescriptor.getXrayConfig();
        if (xrayConfig != null) {
            xrayConfig.setBypassDefaultProxy(bypassDefaultProxy.isBypassDefaultProxy());
            configService.saveEditedDescriptorAndReload(mutableDescriptor);
        }
    }
}
