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

import com.google.common.collect.Lists;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * @author Uriah Levy
 */
@Component
public class GetProxyKeysService implements RestService {

    private CentralConfigService centralConfigService;

    @Autowired
    public GetProxyKeysService(CentralConfigService centralConfigService) {
        this.centralConfigService = centralConfigService;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        updateResponseWithProxyKeys(response);
    }

    private void updateResponseWithProxyKeys(RestResponse response) {
            ArrayList<String> proxyKeys = Lists
                .newArrayList(centralConfigService.getMutableDescriptor().getProxies().stream()
                        .map(ProxyDescriptor::getKey)
                        .collect(Collectors.toList()));
        response.iModelList(proxyKeys);
    }
}
