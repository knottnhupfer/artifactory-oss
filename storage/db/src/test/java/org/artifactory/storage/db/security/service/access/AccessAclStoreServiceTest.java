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

package org.artifactory.storage.db.security.service.access;

import com.google.common.collect.ImmutableList;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.security.access.AccessService;
import org.artifactory.test.ArtifactoryHomeStub;
import org.jfrog.access.client.AccessClient;
import org.jfrog.access.client.permission.PermissionsClient;
import org.jfrog.access.common.ServiceId;
import org.jfrog.access.rest.permission.PermissionActionsResponse;
import org.jfrog.access.rest.permission.PermissionResponse;
import org.jfrog.access.rest.permission.PermissionsResponse;
import org.jfrog.common.ClockUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Noam Shemesh
 */
public class AccessAclStoreServiceTest {
    private AclStoreServiceImpl aclStoreServiceImpl;

    @Mock
    private AccessService accessService;
    private PermissionsClient permissionsClient;

    @BeforeMethod
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);

        ArtifactoryHome.bind(new ArtifactoryHomeStub());
        aclStoreServiceImpl = new AclStoreServiceImpl();
        aclStoreServiceImpl.setAccessService(accessService);
        aclStoreServiceImpl.init();

        ArtifactoryHome.get().getArtifactoryProperties().setProperty(ConstantValues.aclMinimumWaitRefreshDataSeconds.getPropertyName(), "1");
        long time = ClockUtils.epochMillis();
        AccessClient accessClient = Mockito.mock(AccessClient.class);
        permissionsClient = Mockito.mock(PermissionsClient.class);
        when(accessClient.permissions()).thenReturn(permissionsClient);
        PermissionResponse permissionResponse = new PermissionResponse()
                .modified(time - ConstantValues.aclMinimumWaitRefreshDataSeconds.getLong() - 1)
                .actions(new PermissionActionsResponse())
                .resourceType("repo").serviceId("ser").name("name");
        when(permissionsClient.findPermissionsByServiceIdAndResourceType(any(), any()))
                .thenReturn(new PermissionsResponse().permissions(ImmutableList.of(permissionResponse)));
        when(permissionsClient.lastUpdatedByServiceId(any()))
                .thenReturn(time);

        when(accessService.getAccessClient()).thenReturn(accessClient);
        when(accessService.getArtifactoryServiceId()).thenReturn(ServiceId.fromFormattedName("jfrt@service"));
    }

    @Test(enabled = false, description = "flaky")
    public void testRefreshingCache() throws InterruptedException {
        aclStoreServiceImpl.getAclCache();
        aclStoreServiceImpl.getAclCache();
        Thread.sleep(1001);
        aclStoreServiceImpl.getAclCache();

        verify(permissionsClient, times(2)).lastUpdatedByServiceId(any());
    }

    @Test(enabled = false, description = "flaky")
    public void testRefreshingCacheForAclMap() throws InterruptedException {
        aclStoreServiceImpl.getAllRepoAcls();
        aclStoreServiceImpl.getAllRepoAcls();
        Thread.sleep(1001);
        aclStoreServiceImpl.getAllRepoAcls();

        verify(permissionsClient, times(2)).lastUpdatedByServiceId(any());
    }
}