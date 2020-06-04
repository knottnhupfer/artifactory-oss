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

package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.permissions;

import com.google.common.collect.Maps;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.security.AclService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.BuildAcl;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.PrincipalEffectivePermissions;
import org.artifactory.ui.rest.model.builds.permissions.BuildPermissionsModel;
import org.artifactory.ui.rest.service.common.EffectivePermissionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static org.artifactory.build.BuildInfoUtils.formatBuildTime;

/**
 * Used in Effective Permissions tab per build, called by the Build browser
 *
 * @author Yuval Reches
 */
@Component
public class GetBuildEffectivePermissionService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetBuildEffectivePermissionService.class);

    private AuthorizationService authService;
    private UserGroupService userGroupService;
    private EffectivePermissionHelper effectivePermissionHelper;
    private AddonsManager addonsManager;

    @Autowired
    public GetBuildEffectivePermissionService(AuthorizationService authService,
            UserGroupService userGroupService, AddonsManager addonsManager) {
        this.authService = authService;
        this.userGroupService = userGroupService;
        this.effectivePermissionHelper = new EffectivePermissionHelper(userGroupService);
        this.addonsManager = addonsManager;
    }

    public void execute(ArtifactoryRestRequest artifactoryRequest, RestResponse response) {
        String buildName = artifactoryRequest.getPathParamByKey("name");
        String buildNumber = artifactoryRequest.getPathParamByKey("number");
        String buildStarted = formatBuildTime(artifactoryRequest.getPathParamByKey("date"));
        boolean canManageBuild = authService.canManageBuild(buildName, buildNumber, buildStarted);
        if (canManageBuild) {
            try {
                response.iModel(getPermissionModel(buildName, buildNumber, buildStarted));
            } catch (Exception e) {
                log.debug("Artifactory was unable to get users and groups effective permission for build.", e);
                response.error("Artifactory was unable to get users and groups effective permissions, " +
                                "see logs for more details.");
            }
        } else {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            log.error(String.format("Forbidden UI REST call from user: '%s'", authService.currentUsername()));
        }
    }

    /**
     * Find and populate permission model from acl cache
     */
    public BuildPermissionsModel getPermissionModel(RepoPath repoPath) {
        List<BuildAcl> buildAcls = (repoPath.isRoot()) ? ((AclService) userGroupService).getAllBuildAcls() :
                userGroupService.getBuildPathAcls(repoPath);
        return getBuildPermissionsModel(buildAcls);
    }

    private BuildPermissionsModel getPermissionModel(String buildName, String buildNumber, String buildStarted) {
        List<BuildAcl> buildAcls = userGroupService.getBuildPathAcls(buildName, buildNumber, buildStarted);
        return getBuildPermissionsModel(buildAcls);
    }

    private BuildPermissionsModel getBuildPermissionsModel(List<BuildAcl> buildAcls) {
        Map<String, PrincipalEffectivePermissions> userEffectivePermissions = Maps.newHashMap();
        Map<String, PrincipalEffectivePermissions> groupEffectivePermissions = Maps.newHashMap();
        boolean isAol = addonsManager.addonByType(CoreAddons.class).isAol();
        effectivePermissionHelper.addAdminsToMaps(userEffectivePermissions, groupEffectivePermissions, isAol);
        buildAcls.forEach(aclInfo -> effectivePermissionHelper
                .addAclInfoToAclList(aclInfo, userEffectivePermissions, groupEffectivePermissions));
        effectivePermissionHelper
                .grantGroupUsersEffectivePermissions(groupEffectivePermissions, userEffectivePermissions);
        // update response with model
        return new BuildPermissionsModel(userEffectivePermissions.values(), groupEffectivePermissions.values(),
                buildAcls);
    }

}
