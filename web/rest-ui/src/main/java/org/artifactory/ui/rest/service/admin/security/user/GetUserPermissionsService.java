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

package org.artifactory.ui.rest.service.admin.security.user;

import org.artifactory.addon.AddonsManager;
import org.artifactory.api.security.AclService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.AceInfo;
import org.artifactory.security.ArtifactoryResourceType;
import org.artifactory.security.PermissionTarget;
import org.artifactory.ui.rest.model.admin.security.user.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.artifactory.security.ArtifactoryResourceType.*;

/**
 * @author Chen Keian
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetUserPermissionsService implements RestService<User> {

    @Autowired
    UserGroupService userGroupService;

    @Autowired
    AclService aclService;

    @Autowired
    AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest<User> request, RestResponse response) {
        String name = request.getPathParamByKey("id");
        boolean userOnly = Boolean.valueOf(request.getQueryParamByKey("userOnly"));
        // get user related permissions
        UserPermissions userPermissions = getUserRelatedPermissions(name, userOnly);
        // update response model
        response.iModel(userPermissions);
    }


    /**
     * get user related permission by user name
     *
     * @param name     - user name
     * @param userOnly - if true user related permission only , else user and group related permissions
     * @return - list of user related permissions data
     */
    private UserPermissions getUserRelatedPermissions(String name, boolean userOnly) {
        List<RepoUserPermission> repoUserPermissionList = new ArrayList<>();
        List<BuildUserPermission> buildUserPermissionList = new ArrayList<>();
        List<ReleaseBundleUserPermission> releaseBundleUserPermissionList = new ArrayList<>();
        getRepoPermissions(name, userOnly, repoUserPermissionList);
        getBuildPermissions(name, userOnly, buildUserPermissionList);
        getReleaseBundlePermissions(name, userOnly, releaseBundleUserPermissionList);
        return new UserPermissions(repoUserPermissionList, buildUserPermissionList, releaseBundleUserPermissionList);
    }

    private void getRepoPermissions(String name, boolean userOnly, List<RepoUserPermission> repoUserPermissionList) {
        getPermissionTargetByType(name, userOnly, REPO).forEach((permission, ace) ->
                repoUserPermissionList.add(new RepoUserPermission(ace, permission)));
    }

    private void getBuildPermissions(String name, boolean userOnly, List<BuildUserPermission> buildUserPermissionList) {
        getPermissionTargetByType(name, userOnly, BUILD).forEach((permission, ace) ->
                buildUserPermissionList.add(new BuildUserPermission(ace, permission)));
    }

    private void getReleaseBundlePermissions(String name, boolean userOnly, List<ReleaseBundleUserPermission> releaseBundleUserPermissionList) {
        getPermissionTargetByType(name, userOnly, RELEASE_BUNDLES).forEach(
                (permission, ace) -> releaseBundleUserPermissionList
                        .add(new ReleaseBundleUserPermission(ace, permission)));
    }

    private Map<PermissionTarget, AceInfo> getPermissionTargetByType(String name, boolean userOnly,
            ArtifactoryResourceType type) {
        return userOnly ? aclService.getUserPermissions(name, type) :
                aclService.getUserPermissionByPrincipal(name, type);
    }
}
