package org.artifactory.ui.rest.model.builds.permissions;

import com.google.common.collect.Sets;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.security.BuildAcl;
import org.artifactory.security.BuildPermissionTarget;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.BuildPermissionTargetInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.PrincipalEffectivePermissions;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * UI model for builds Effective Permission tab
 *
 * @author Yuval Reches
 */
public class BuildPermissionsModel extends BaseModel {

    private Collection<PrincipalEffectivePermissions> userEffectivePermissions;
    private Collection<PrincipalEffectivePermissions> groupEffectivePermissions;
    private List<BuildPermissionTargetInfo> permissionTargets;

    public BuildPermissionsModel() {

    }

    public BuildPermissionsModel(Collection<PrincipalEffectivePermissions> userEffectivePermissions,
            Collection<PrincipalEffectivePermissions> groupEffectivePermissions, List<BuildAcl> buildAcls) {
        this.userEffectivePermissions = userEffectivePermissions;
        this.groupEffectivePermissions = groupEffectivePermissions;
        this.permissionTargets = buildAcls.stream()
                .map(this::getPermissionTargetInfo)
                .collect(Collectors.toList());
    }

    public List<BuildPermissionTargetInfo> getPermissionTargets() {
        return permissionTargets;
    }

    public Collection<PrincipalEffectivePermissions> getUserEffectivePermissions() {
        return userEffectivePermissions;
    }

    public Collection<PrincipalEffectivePermissions> getGroupEffectivePermissions() {
        return groupEffectivePermissions;
    }

    private BuildPermissionTargetInfo getPermissionTargetInfo(BuildAcl buildAcl) {
        BuildPermissionTargetInfo buildPermissionTargetInfo = new BuildPermissionTargetInfo();
        BuildPermissionTarget permissionTarget = buildAcl.getPermissionTarget();
        buildPermissionTargetInfo.setPermissionName(permissionTarget.getName());
        Set<String> groups = Sets.newHashSet();
        Set<String> users = Sets.newHashSet();
        buildAcl.getMutableAces().forEach(aceInfo -> {
            if (aceInfo.isGroup()) {
                groups.add(aceInfo.getPrincipal());
            } else {
                users.add(aceInfo.getPrincipal());
            }
        });
        buildPermissionTargetInfo.setGroups(groups);
        buildPermissionTargetInfo.setUsers(users);
        buildPermissionTargetInfo.setIncludePatterns(permissionTarget.getIncludes());
        buildPermissionTargetInfo.setExcludePatterns(permissionTarget.getExcludes());
        return buildPermissionTargetInfo;
    }

}
