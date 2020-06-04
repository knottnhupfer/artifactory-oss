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

package org.artifactory.security.access.emigrate.conveter;

import com.google.common.collect.ImmutableList;
import org.artifactory.security.ArtifactoryResourceType;
import org.artifactory.security.access.AccessService;
import org.jfrog.access.client.AccessClient;
import org.jfrog.access.client.permission.PermissionsClient;
import org.jfrog.access.common.ResourceType;
import org.jfrog.access.common.ServiceId;
import org.jfrog.access.common.ServiceType;
import org.jfrog.access.rest.permission.Permission;
import org.jfrog.access.rest.permission.PermissionResponse;
import org.jfrog.access.rest.permission.Permissions;
import org.jfrog.access.rest.permission.UpdatePermissionRequest;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Dan Feldman
 */
@Test
public class V5100ConvertResourceTypeToRepoTest {

    @Mock
    private AccessService accessService;
    @Mock
    private PermissionsClient permissionsClient;
    @Mock
    private AccessClient accessClient;
    @Mock
    private Permissions permissions;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
    }

    public void testConversion() {
        ServiceId serviceId = ServiceId.generateUniqueId(ServiceType.ARTIFACTORY);
        when(accessService.getArtifactoryServiceId()).thenReturn(serviceId);
        when(accessService.getAccessClient()).thenReturn(accessClient);
        when(accessClient.permissions()).thenReturn(permissionsClient);
        when(permissionsClient.findPermissionsByServiceId(serviceId)).thenReturn(permissions);
        when(this.permissions.getPermissions()).thenReturn(buildPermissionsList());
        ArgumentCaptor<UpdatePermissionRequest> arg = ArgumentCaptor.forClass(UpdatePermissionRequest.class);

        V5100ConvertResourceTypeToRepo converter = new V5100ConvertResourceTypeToRepo(accessService);
        converter.convert();

        verify(permissionsClient).updatePermission(arg.capture());
        assertEquals(arg.getAllValues().size(), 1);
        assertEquals(arg.getValue().getResourceType(), ArtifactoryResourceType.REPO.getName());
    }

    private List<Permission> buildPermissionsList() {
        PermissionResponse repoPermission = new PermissionResponse()
                .resourceType(ArtifactoryResourceType.REPO.getName())
                .name("repoPermission");
        PermissionResponse servicePermission = new PermissionResponse()
                .resourceType(ResourceType.SERVICE)
                .name("servicePermission");
        return ImmutableList.of(repoPermission, servicePermission);
    }
}
