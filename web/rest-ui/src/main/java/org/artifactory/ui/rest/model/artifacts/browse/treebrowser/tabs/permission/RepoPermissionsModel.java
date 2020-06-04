package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission;

import com.google.common.collect.Sets;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.security.RepoAcl;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * UI model for repo Effective Permission tab
 *
 * @author nadavy
 */
public class RepoPermissionsModel extends BaseModel {

    private Collection<PrincipalEffectivePermissions> userEffectivePermissions;
    private Collection<PrincipalEffectivePermissions> groupEffectivePermissions;
    private List<RepoPermissionTargetInfo> permissionTargets;

    public RepoPermissionsModel() {

    }

    public RepoPermissionsModel(Collection<PrincipalEffectivePermissions> userEffectivePermissions,
            Collection<PrincipalEffectivePermissions> groupEffectivePermissions, List<RepoAcl> repoAcls) {
        this.userEffectivePermissions = userEffectivePermissions;
        this.groupEffectivePermissions = groupEffectivePermissions;
        this.permissionTargets = repoAcls.stream()
                .map(this::getPermissionTargetInfo)
                .collect(Collectors.toList());
    }

    public List<RepoPermissionTargetInfo> getPermissionTargets() {
        return permissionTargets;
    }

    public Collection<PrincipalEffectivePermissions> getUserEffectivePermissions() {
        return userEffectivePermissions;
    }

    public Collection<PrincipalEffectivePermissions> getGroupEffectivePermissions() {
        return groupEffectivePermissions;
    }

    private RepoPermissionTargetInfo getPermissionTargetInfo(RepoAcl repoAcl) {
        RepoPermissionTargetInfo repoPermissionTargetInfo = new RepoPermissionTargetInfo();
        repoPermissionTargetInfo.setPermissionName(repoAcl.getPermissionTarget().getName());
        Set<String> groups = Sets.newHashSet();
        Set<String> users = Sets.newHashSet();
        repoAcl.getMutableAces().forEach(aceInfo -> {
            if (aceInfo.isGroup()) {
                groups.add(aceInfo.getPrincipal());
            } else {
                users.add(aceInfo.getPrincipal());
            }
        });
        repoPermissionTargetInfo.setGroups(groups);
        repoPermissionTargetInfo.setUsers(users);
        repoPermissionTargetInfo.setRepoKeys(repoAcl.getPermissionTarget().getRepoKeys());
        return repoPermissionTargetInfo;
    }
}
