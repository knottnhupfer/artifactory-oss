package org.artifactory.ui.rest.service.distribution;

import com.google.common.collect.Maps;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.security.AclService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.exception.ForbiddenException;
import org.artifactory.security.ReleaseBundleAcl;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.PrincipalEffectivePermissions;
import org.artifactory.ui.rest.model.distribution.ReleaseBundlePermissionsModel;
import org.artifactory.ui.rest.service.common.EffectivePermissionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Used in Effective Permissions tab per release-bundle, called by the Release-Bundle browser
 *
 * @author Inbar Tal
 */
@Component
public class GetReleaseBundleEffectivePermissionService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetReleaseBundleEffectivePermissionService.class);

    private AuthorizationService authService;
    private UserGroupService userGroupService;
    private EffectivePermissionHelper effectivePermissionHelper;
    private AddonsManager addonsManager;

    @Autowired
    public GetReleaseBundleEffectivePermissionService(AuthorizationService authService,
            UserGroupService userGroupService, AddonsManager addonsManager) {
        this.authService = authService;
        this.userGroupService = userGroupService;
        this.effectivePermissionHelper = new EffectivePermissionHelper(userGroupService);
        this.addonsManager = addonsManager;
    }

    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String name = request.getPathParamByKey("name");
        String version = request.getPathParamByKey("version");
        String repoKey = request.getPathParamByKey("repoKey");

        RepoPath releaseBundlePath = RepoPathFactory.create(repoKey, name + "/" + version);
        if (!authService.canManage(releaseBundlePath)) {
            throw new ForbiddenException(
                    String.format("Forbidden UI REST call from user: '%s'", authService.currentUsername()));
        }
        try {
            response.iModel(getPermissionModel(releaseBundlePath));
        } catch (Exception e) {
                log.debug("Artifactory was unable to get users and groups effective permission for release-bundle.", e);
                response.error("Artifactory was unable to get users and groups effective permissions, " +
                        "see logs for more details.");
            }
        }

    /**
     * Find and populate release bundle permission model from acl cache
     */
    public ReleaseBundlePermissionsModel getPermissionModel(RepoPath repoPath) {
        List<ReleaseBundleAcl> releaseBundleAcls =
                (repoPath.isRoot()) ? ((AclService) userGroupService).getAllReleaseBundleAcls(true) :
                        userGroupService.getReleaseBundleAcls(repoPath);
        Map<String, PrincipalEffectivePermissions> userEffectivePermissions = Maps.newHashMap();
        Map<String, PrincipalEffectivePermissions> groupEffectivePermissions = Maps.newHashMap();
        boolean isAol = addonsManager.addonByType(CoreAddons.class).isAol();
        effectivePermissionHelper.addAdminsToMaps(userEffectivePermissions, groupEffectivePermissions, isAol);
        releaseBundleAcls.forEach(aclInfo -> effectivePermissionHelper
                .addAclInfoToAclList(aclInfo, userEffectivePermissions, groupEffectivePermissions));
        effectivePermissionHelper
                .grantGroupUsersEffectivePermissions(groupEffectivePermissions, userEffectivePermissions);
        // update response with model
        return new ReleaseBundlePermissionsModel(userEffectivePermissions.values(), groupEffectivePermissions.values(),
                releaseBundleAcls);
    }
}
