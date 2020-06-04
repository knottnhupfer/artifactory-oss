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
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.descriptor.repo.XrayDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.ArtifactoryRestResponse;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Uriah Levy
 */
@Test
public class UpdateXrayProxyServiceTest {

    public void testUpdateProxy() {
        // arrange
        CentralConfigService centralConfigService = mock(CentralConfigService.class);
        CentralConfigDescriptorImpl configDescriptor = new CentralConfigDescriptorImpl();
        configDescriptor.setXrayConfig(new XrayDescriptor());
        when(centralConfigService.getMutableDescriptor()).thenReturn(configDescriptor);
        ArtifactoryRestResponse response = new ArtifactoryRestResponse();
        ArtifactoryRestRequest request = mock(ArtifactoryRestRequest.class);
        when(request.getImodel()).thenReturn("charles-proxy");

        // do
        UpdateXrayProxyService service = new UpdateXrayProxyService(centralConfigService);
        service.execute(request, response);

        // assert
        String xrayProxy = configDescriptor.getXrayConfig().getProxy();
        assertNotNull(xrayProxy);
        // The service should update the conf descriptor
        assertEquals(xrayProxy, "charles-proxy");
    }
}