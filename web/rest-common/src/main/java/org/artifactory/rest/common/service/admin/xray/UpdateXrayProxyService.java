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

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.XrayDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Uriah Levy
 */
@Component
public class UpdateXrayProxyService implements RestService {

    private CentralConfigService centralConfigService;

    @Autowired
    public UpdateXrayProxyService(CentralConfigService centralConfigService) {
        this.centralConfigService = centralConfigService;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        updateXrayProxy(request, response);
    }

    private void updateXrayProxy(ArtifactoryRestRequest request, RestResponse response) {
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        XrayDescriptor xrayConfig = mutableDescriptor.getXrayConfig();
        String proxy = request.getImodel().toString();
        if (proxy != null) {
            if (StringUtils.isEmpty(proxy)) {
                xrayConfig.setProxy(null);
            } else {
                xrayConfig.setProxy(proxy);
            }
            centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
        }
    }
}
