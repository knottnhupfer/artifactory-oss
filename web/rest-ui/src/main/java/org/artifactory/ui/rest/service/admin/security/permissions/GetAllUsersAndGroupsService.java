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

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.ui.rest.model.admin.security.permissions.AllUsersAndGroupsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.artifactory.addon.CoreAddons.SUPER_USER_NAME;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAllUsersAndGroupsService implements RestService {
    private UserGroupService userGroupService;
    private AuthorizationService authService;
    private AddonsManager addonsManager;

    @Autowired
    public GetAllUsersAndGroupsService(UserGroupService userGroupService, AuthorizationService authService, AddonsManager addonsManager) {
        this.userGroupService = userGroupService;
        this.authService = authService;
        this.addonsManager = addonsManager;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        if (!authService.hasPermission(ArtifactoryPermission.MANAGE)) {
            int responseCode = authService.isAnonymous() ?
                    Response.Status.UNAUTHORIZED.getStatusCode() : Response.Status.FORBIDDEN.getStatusCode();
            response.responseCode(responseCode);
            response.error("MANAGE permission is required");
            return;
        }
        Map<String, Boolean> allUsers = userGroupService.getAllUsersAndAdminStatus(false);
        if (addonsManager.addonByType(CoreAddons.class).isAol()) {
            allUsers.remove(SUPER_USER_NAME);
        }
        List<AllUsersAndGroupsModel.PrincipalInfo> allUsersInfo = allUsers.entrySet().stream()
                .map(entry -> new AllUsersAndGroupsModel.PrincipalInfo(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        List<AllUsersAndGroupsModel.PrincipalInfo> allGroups = userGroupService.getAllGroups()
                .stream()
                .map(groupInfo -> new AllUsersAndGroupsModel.PrincipalInfo(groupInfo.getGroupName(), groupInfo.isAdminPrivileges()))
                .collect(Collectors.toList());

        AllUsersAndGroupsModel allUsersAndGroups = new AllUsersAndGroupsModel();
        allUsersAndGroups.setAllUsers(allUsersInfo);
        allUsersAndGroups.setAllGroups(allGroups);
        response.iModel(allUsersAndGroups);
    }
}