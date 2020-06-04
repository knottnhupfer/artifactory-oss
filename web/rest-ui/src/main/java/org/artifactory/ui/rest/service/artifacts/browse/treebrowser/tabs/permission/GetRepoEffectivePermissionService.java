/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2016 JFrog Ltd.
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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.permission;

import com.google.common.collect.Maps;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.RepoAcl;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.PrincipalEffectivePermissions;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.RepoPermissionsModel;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.permissions.GetBuildEffectivePermissionService;
import org.artifactory.ui.rest.service.common.EffectivePermissionHelper;
import org.artifactory.ui.rest.service.distribution.GetReleaseBundleEffectivePermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Used in Effective Permissions tab per artifact for repositories (including build-info repositories)
 *
 * @author Chen Keinan
 */
@Component
public class GetRepoEffectivePermissionService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetRepoEffectivePermissionService.class);

    private AuthorizationService authService;

    private RepositoryService repoService;
    private GetBuildEffectivePermissionService buildEffectivePermissionService;
    private GetReleaseBundleEffectivePermissionService releaseBundleEffectivePermissionService;

    private UserGroupService userGroupService;
    private EffectivePermissionHelper effectivePermissionHelper;
    private AddonsManager addonsManager;

    @Autowired
    public GetRepoEffectivePermissionService(AuthorizationService authService,
            RepositoryService repoService, UserGroupService userGroupService, AddonsManager addonsManager,
            GetBuildEffectivePermissionService buildEffectivePermissionService,
            GetReleaseBundleEffectivePermissionService releaseBundleEffectivePermissionService) {
        this.authService = authService;
        this.repoService = repoService;
        this.userGroupService = userGroupService;
        this.effectivePermissionHelper = new EffectivePermissionHelper(userGroupService);
        this.addonsManager = addonsManager;
        this.buildEffectivePermissionService = buildEffectivePermissionService;
        this.releaseBundleEffectivePermissionService = releaseBundleEffectivePermissionService;
    }

    public void execute(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse) {
        String path = artifactoryRequest.getQueryParamByKey("path");
        String repoKey = artifactoryRequest.getQueryParamByKey("repoKey");
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);

        ReleaseBundlesRepoDescriptor repoDescriptor = repoService.releaseBundlesRepoDescriptorByKey(repoKey);
        if (repoDescriptor != null) {
            handleReleaseBundleEffectivePermissions(artifactoryResponse, repoPath);
            return;
        }
        LocalRepoDescriptor localRepoDescriptor = repoService.localRepoDescriptorByKey(repoPath.getRepoKey());
        if (localRepoDescriptor != null && RepoType.BuildInfo.equals(localRepoDescriptor.getType())) {
            handleBuildEffectivePermissions(artifactoryResponse, repoPath);
        } else {
            handleRepoEffectivePermissions(artifactoryResponse, repoPath);
        }
    }

    private void handleRepoEffectivePermissions(RestResponse artifactoryResponse, RepoPath repoPath) {
        if (repoService.isVirtualRepoExist(repoPath.getRepoKey())) {
            repoPath = repoService.getVirtualItemInfo(repoPath).getRepoPath();
        }
        boolean canManage = authService.canManage(repoPath);
        if (canManage) {
            try {
                artifactoryResponse.iModel(getRepoPermissionModel(repoPath));
            } catch (Exception e) {
                log.debug("Artifactory was unable to get users and groups effective permission.", e);
                artifactoryResponse
                        .error("Artifactory was unable to get users and groups effective permissions, see logs for more details.");
            }
        }
    }

    /**
     * Handle build-info repository effective permissions. get already-encoded repo path.
     */
    private void handleBuildEffectivePermissions(RestResponse response, RepoPath repoPath) {
        boolean canManageBuild = authService.canManage(repoPath);
        if (canManageBuild) {
            try {
                response.iModel(buildEffectivePermissionService.getPermissionModel(repoPath));
            } catch (Exception e) {
                log.debug("Artifactory was unable to get users and groups effective permission for build.", e);
                response.error("Artifactory was unable to get users and groups effective permissions, " +
                        "see logs for more details.");
            }
        }
    }

    /**
     * Handle release bundle repository effective permissions.
     */
    private void handleReleaseBundleEffectivePermissions(RestResponse response, RepoPath repoPath) {
        boolean canManageReleaseBundle = authService.canManage(repoPath);
        if (canManageReleaseBundle) {
            try {
                response.iModel(releaseBundleEffectivePermissionService.getPermissionModel(repoPath));
            } catch (Exception e) {
                log.debug("Artifactory was unable to get users and groups effective permission for release bundle.", e);
                response.error("Artifactory was unable to get users and groups effective permissions, " +
                        "see logs for more details.");
            }
        }
    }

    /**
     * Find and populate repo permission model from acl cache
     */
    private RepoPermissionsModel getRepoPermissionModel(RepoPath repoPath) {
        List<RepoAcl> repoAcls = userGroupService.getRepoPathAcls(repoPath);
        Map<String, PrincipalEffectivePermissions> userEffectivePermissions = Maps.newHashMap();
        Map<String, PrincipalEffectivePermissions> groupEffectivePermissions = Maps.newHashMap();
        addAdminsToMaps(userEffectivePermissions, groupEffectivePermissions);
        repoAcls.forEach(aclInfo -> effectivePermissionHelper.addAclInfoToAclList(aclInfo, userEffectivePermissions, groupEffectivePermissions));
        effectivePermissionHelper.grantGroupUsersEffectivePermissions(groupEffectivePermissions, userEffectivePermissions);
        // update response with model
        return new RepoPermissionsModel(userEffectivePermissions.values(), groupEffectivePermissions.values(), repoAcls);
    }

    private void addAdminsToMaps(Map<String, PrincipalEffectivePermissions> userEffectivePermissions,
            Map<String, PrincipalEffectivePermissions> groupEffectivePermissions) {
        boolean isAol = addonsManager.addonByType(CoreAddons.class).isAol();
        effectivePermissionHelper.addAdminsToMaps(userEffectivePermissions, groupEffectivePermissions, isAol);
    }
}
