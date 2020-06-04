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

package org.artifactory.ui.rest.service.utils.groups;

import org.artifactory.addon.AddonsManager;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AclService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.RepoPermissionTarget;
import org.artifactory.ui.rest.model.admin.security.group.Group;
import org.artifactory.ui.rest.model.admin.security.user.BuildUserPermission;
import org.artifactory.ui.rest.model.admin.security.user.ReleaseBundleUserPermission;
import org.artifactory.ui.rest.model.admin.security.user.RepoUserPermission;
import org.artifactory.ui.rest.model.admin.security.user.UserPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.security.ArtifactoryResourceType.*;
import static org.artifactory.security.PermissionTarget.*;

/**
 * @author Yuval Reches
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetGroupPermissionsService implements RestService<Group> {

    @Autowired
    private AclService aclService;

    @Autowired
    private RepositoryService repoService;

    @Autowired
    AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest<Group> request, RestResponse response) {
        Group group = request.getImodel();
        UserPermissions userPermissions = getUserRelatedPermissions(group.getGroups());
        response.iModel(userPermissions);
    }

    private UserPermissions getUserRelatedPermissions(List<String> groups) {
        List<RepoUserPermission> repoUserPermissionList = aclService.getGroupsPermissions(groups, REPO)
                .entries()
                .parallelStream()
                .map(entry -> new RepoUserPermission(entry.getValue(),
                        (RepoPermissionTarget)entry.getKey(),
                        getRepoKeysSize((RepoPermissionTarget)entry.getKey())))
                .collect(Collectors.toList());

        List<BuildUserPermission> buildUserPermissionList = aclService.getGroupsPermissions(groups, BUILD)
                .entries()
                .parallelStream()
                .map(entry -> new BuildUserPermission(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());

        List<ReleaseBundleUserPermission> releaseBundleUserPermissionList = aclService.getGroupsPermissions(groups, RELEASE_BUNDLES)
                .entries()
                .parallelStream()
                .map(entry -> new ReleaseBundleUserPermission(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());

        return new UserPermissions(repoUserPermissionList, buildUserPermissionList, releaseBundleUserPermissionList);
    }

    private int getRepoKeysSize(RepoPermissionTarget permission) {
        if (permission.getRepoKeys().contains(ANY_REPO)) {
            return repoService.getAllRepoKeys().size();
        } else if (permission.getRepoKeys().contains(ANY_LOCAL_REPO)) {
            return repoService.getLocalRepoDescriptors().size();
        } else if (permission.getRepoKeys().contains(ANY_REMOTE_REPO)) {
            return repoService.getCachedRepoDescriptors().size();
        } else if (permission.getRepoKeys().contains(ANY_DISTRIBUTION_REPO)) {
            return repoService.getDistributionRepoDescriptors().size();
        }
        return 0;
    }
}
