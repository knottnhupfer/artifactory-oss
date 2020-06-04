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
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.ArtifactoryRestResponse;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Uriah Levy
 */
@Test
public class GetProxyKeysServiceTest {

    public void testGetProxyKeys() {
        CentralConfigService centralConfigService = mock(CentralConfigService.class);
        CentralConfigDescriptorImpl configDescriptor = new CentralConfigDescriptorImpl();
        ProxyDescriptor firstProxy = new ProxyDescriptor();
        firstProxy.setKey("first-proxy");
        ProxyDescriptor secondProxy = new ProxyDescriptor();
        secondProxy.setKey("second-proxy");
        configDescriptor.setProxies(Arrays.asList(firstProxy, secondProxy));
        when(centralConfigService.getMutableDescriptor()).thenReturn(configDescriptor);
        GetProxyKeysService service = new GetProxyKeysService(centralConfigService);
        ArtifactoryRestResponse response = new ArtifactoryRestResponse();
        service.execute(mock(ArtifactoryRestRequest.class), response);
        assertEquals(response.getResponseCode(), 200);
        assertEquals(Arrays.asList("first-proxy", "second-proxy"), response.getEntity());
    }
}