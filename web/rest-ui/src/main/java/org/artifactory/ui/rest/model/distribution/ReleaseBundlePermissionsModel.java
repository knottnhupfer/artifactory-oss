package org.artifactory.ui.rest.model.distribution;

import com.google.common.collect.Sets;
import lombok.NoArgsConstructor;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.security.ReleaseBundleAcl;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.PrincipalEffectivePermissions;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.ReleaseBundlePermissionTargetInfo;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Inbar Tal
 */
@NoArgsConstructor
public class ReleaseBundlePermissionsModel extends BaseModel {

    private Collection<PrincipalEffectivePermissions> userEffectivePermissions;
    private Collection<PrincipalEffectivePermissions> groupEffectivePermissions;
    private List<ReleaseBundlePermissionTargetInfo> permissionTargets;

    public ReleaseBundlePermissionsModel(Collection<PrincipalEffectivePermissions> userEffectivePermissions,
            Collection<PrincipalEffectivePermissions> groupEffectivePermissions, List<ReleaseBundleAcl> releaseBundleAcls) {
        this.userEffectivePermissions = userEffectivePermissions;
        this.groupEffectivePermissions = groupEffectivePermissions;
        this.permissionTargets = releaseBundleAcls.stream()
                .map(this :: getPermissionTargetInfo)
                .collect(Collectors.toList());
    }

    public List<ReleaseBundlePermissionTargetInfo> getPermissionTargets() {
        return permissionTargets;
    }

    public Collection<PrincipalEffectivePermissions> getUserEffectivePermissions() {
        return userEffectivePermissions;
    }

    public Collection<PrincipalEffectivePermissions> getGroupEffectivePermissions() {
        return groupEffectivePermissions;
    }

    private ReleaseBundlePermissionTargetInfo getPermissionTargetInfo(ReleaseBundleAcl releaseBundleAcl) {
        ReleaseBundlePermissionTargetInfo releaseBundlePermissionTargetInfo = new ReleaseBundlePermissionTargetInfo();
        releaseBundlePermissionTargetInfo.setPermissionName(releaseBundleAcl.getPermissionTarget().getName());
        Set<String> groups = Sets.newHashSet();
        Set<String> users = Sets.newHashSet();
        releaseBundleAcl.getMutableAces().forEach(aceInfo -> {
            if (aceInfo.isGroup()) {
                groups.add(aceInfo.getPrincipal());
            } else {
                users.add(aceInfo.getPrincipal());
            }
        });
        releaseBundlePermissionTargetInfo.setGroups(groups);
        releaseBundlePermissionTargetInfo.setUsers(users);
        releaseBundlePermissionTargetInfo.setRepoKeys(releaseBundleAcl.getPermissionTarget().getRepoKeys());
        return releaseBundlePermissionTargetInfo;
    }
}
