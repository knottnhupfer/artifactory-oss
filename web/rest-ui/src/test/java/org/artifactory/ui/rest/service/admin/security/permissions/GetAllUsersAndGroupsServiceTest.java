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

package org.artifactory.ui.rest.service.admin.security.permissions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestResponse;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.ui.rest.model.admin.security.group.Group;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.artifactory.addon.CoreAddons.SUPER_USER_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Maxim Yurkovsky
 */
public class GetAllUsersAndGroupsServiceTest {
    @Test
    public void testExecuteForUserWithoutManagePermission() {
        AuthorizationService authService = mock(AuthorizationService.class);
        when(authService.hasPermission(ArtifactoryPermission.MANAGE)).thenReturn(false);
        when(authService.isAnonymous()).thenReturn(false);
        GetAllUsersAndGroupsService getAllUsersAndGroupsService = new GetAllUsersAndGroupsService(null,
                authService, createAddonsManagerMock(false));
        ArtifactoryRestResponse response = new ArtifactoryRestResponse();

        getAllUsersAndGroupsService.execute(null, response);

        assertEquals(response.getResponseCode(), Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void testExecuteForAnonymous() {
        AuthorizationService authService = mock(AuthorizationService.class);
        when(authService.hasPermission(ArtifactoryPermission.MANAGE)).thenReturn(false);
        when(authService.isAnonymous()).thenReturn(true);
        GetAllUsersAndGroupsService getAllUsersAndGroupsService = new GetAllUsersAndGroupsService(null,
                authService, createAddonsManagerMock(true));
        ArtifactoryRestResponse response = new ArtifactoryRestResponse();

        getAllUsersAndGroupsService.execute(null, response);

        assertEquals(response.getResponseCode(), Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void testExecuteForUserWithManagePermission() throws Exception {
        AuthorizationService authService = mock(AuthorizationService.class);
        when(authService.hasPermission(ArtifactoryPermission.MANAGE)).thenReturn(true);
        UserGroupService userGroupService = mock(UserGroupService.class);
        when(userGroupService.getAllUsersAndAdminStatus(false)).thenReturn(ImmutableMap.of("a", false));
        Group group = new Group();
        group.setGroupName("b");
        group.setAdminPrivileges(true);
        when(userGroupService.getAllGroups()).thenReturn(ImmutableList.of(group));
        GetAllUsersAndGroupsService getAllUsersAndGroupsService = new GetAllUsersAndGroupsService(userGroupService,
                authService, createAddonsManagerMock(false));
        ArtifactoryRestResponse response = new ArtifactoryRestResponse();

        getAllUsersAndGroupsService.execute(null, response);

        assertEquals(response.getResponseCode(), Response.Status.OK.getStatusCode());
        assertEquals(response.getEntity().toString(),
                "{\"allGroups\":[{\"principal\":\"b\",\"admin\":true}],\"allUsers\":[{\"principal\":\"a\",\"admin\":false}]}");
    }

    @Test
    public void testExecuteDoesntReturnSuperUserOnAol() throws Exception {
        AuthorizationService authService = mock(AuthorizationService.class);
        when(authService.hasPermission(ArtifactoryPermission.MANAGE)).thenReturn(true);
        UserGroupService userGroupService = mock(UserGroupService.class);
        Map<String, Boolean> users = new HashMap<>();
        users.put("a", false);
        users.put(SUPER_USER_NAME, true);
        when(userGroupService.getAllUsersAndAdminStatus(false)).thenReturn(users);
        when(userGroupService.getAllGroups()).thenReturn(ImmutableList.of());
        GetAllUsersAndGroupsService getAllUsersAndGroupsService = new GetAllUsersAndGroupsService(userGroupService,
                authService, createAddonsManagerMock(true));
        ArtifactoryRestResponse response = new ArtifactoryRestResponse();

        getAllUsersAndGroupsService.execute(null, response);

        assertEquals(response.getResponseCode(), Response.Status.OK.getStatusCode());
        assertEquals(response.getEntity().toString(),
                "{\"allGroups\":[],\"allUsers\":[{\"principal\":\"a\",\"admin\":false}]}");
    }

    private AddonsManager createAddonsManagerMock(boolean isAol) {
        CoreAddons coreAddons = mock(CoreAddons.class);
        when(coreAddons.isAol()).thenReturn(isAol);
        AddonsManager addonsManager = mock(AddonsManager.class);
        when(addonsManager.addonByType(CoreAddons.class)).thenReturn(coreAddons);
        return addonsManager;
    }
}