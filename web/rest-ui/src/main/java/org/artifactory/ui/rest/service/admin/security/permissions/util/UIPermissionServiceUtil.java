package org.artifactory.ui.rest.service.admin.security.permissions.util;

import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.security.PrincipalConfiguration;
import org.artifactory.security.permissions.PermissionTargetModel;
import org.artifactory.security.permissions.RepoPermissionTargetModel;
import org.artifactory.ui.rest.model.admin.security.permissions.CombinedPermissionTargetUIModel;
import org.artifactory.ui.rest.model.admin.security.permissions.PermissionTargetUIModel;
import org.artifactory.ui.rest.model.admin.security.permissions.PrincipalPermissionActions;
import org.artifactory.ui.rest.model.admin.security.permissions.build.BuildPermissionTargetUIModel;
import org.artifactory.ui.rest.model.admin.security.permissions.repo.RepoPermissionTargetUIModel;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dan Feldman
 */
public class UIPermissionServiceUtil {

    private UIPermissionServiceUtil() {

    }

    public static PermissionTargetModel backendModelFromUIModel(ArtifactoryRestRequest<CombinedPermissionTargetUIModel> request) {
        CombinedPermissionTargetUIModel permissionTargetUIModel = request.getImodel();

        RepoPermissionTargetUIModel uiRepoPermission = permissionTargetUIModel.getRepoPermission();
        RepoPermissionTargetModel repoPermissionTargetModel = getRepoPermissionTarget(uiRepoPermission);
        BuildPermissionTargetUIModel uiBuildPermission = permissionTargetUIModel.getBuildPermission();
        RepoPermissionTargetModel buildPermissionTargetModel = getBuildPermissionTarget(uiBuildPermission);

        String permissionName = permissionTargetUIModel.getName();
        PermissionTargetModel backendModel = new PermissionTargetModel();
        backendModel.setName(permissionName);
        backendModel.setBuild(buildPermissionTargetModel);
        backendModel.setRepo(repoPermissionTargetModel);
        return backendModel;
    }

    private static RepoPermissionTargetModel getRepoPermissionTarget(RepoPermissionTargetUIModel uiRepoPermission) {
        RepoPermissionTargetModel repoPermissionTargetModel = null;
        if (uiRepoPermission != null) {
            PrincipalConfiguration repoPrincipalConfig = getPrincipalConfiguration(uiRepoPermission);
            repoPermissionTargetModel = RepoPermissionTargetModel.builder()
                    .repositories(uiRepoPermission.getRepoKeys())
                    .includePatterns(uiRepoPermission.getIncludePatterns())
                    .excludePatterns(uiRepoPermission.getExcludePatterns())
                    .actions(repoPrincipalConfig)
                    .build();
        }
        return repoPermissionTargetModel;
    }

    private static RepoPermissionTargetModel getBuildPermissionTarget(BuildPermissionTargetUIModel uiBuildPermission) {
        RepoPermissionTargetModel buildPermissionTargetModel = null;
        if (uiBuildPermission != null) {
            PrincipalConfiguration buildPrincipalConfig = getPrincipalConfiguration(uiBuildPermission);
            buildPermissionTargetModel = RepoPermissionTargetModel.builder()
                    .repositories(uiBuildPermission.getRepoKeys())
                    .includePatterns(uiBuildPermission.getIncludePatterns())
                    .excludePatterns(uiBuildPermission.getExcludePatterns())
                    .actions(buildPrincipalConfig)
                    .build();
        }
        return buildPermissionTargetModel;
    }

    private static PrincipalConfiguration getPrincipalConfiguration(PermissionTargetUIModel uiPermission) {
        PrincipalConfiguration repoPrincipalConfig = new PrincipalConfiguration();
        Map<String, Set<String>> userActions = getPrincipalActions(uiPermission.getUserPermissionActions());
        Map<String, Set<String>> groupActions = getPrincipalActions(uiPermission.getGroupPermissionActions());
        repoPrincipalConfig.setUsers(userActions);
        repoPrincipalConfig.setGroups(groupActions);
        return repoPrincipalConfig;
    }

    private static Map<String, Set<String>> getPrincipalActions(List<PrincipalPermissionActions> actions) {
        Map<String, Set<String>> result = null;
        if (actions != null) {
            result = actions.stream()
                    .collect(Collectors
                            .toMap(PrincipalPermissionActions::getPrincipal, PrincipalPermissionActions::getActions));
        }
        return result;
    }
}
