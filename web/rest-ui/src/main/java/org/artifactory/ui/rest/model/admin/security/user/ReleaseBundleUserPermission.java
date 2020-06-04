package org.artifactory.ui.rest.model.admin.security.user;

import lombok.Getter;
import org.artifactory.security.AceInfo;
import org.artifactory.security.PermissionTarget;
import org.artifactory.security.ReleaseBundlePermissionTarget;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.EffectivePermission;

import java.util.List;

/**
 * Used for the various effective permissions views
 *
 * @author Inbar Tal
 */
@Getter
public class ReleaseBundleUserPermission extends UserPermission {
    private List<String> repoKeys;
    private List<String> includePatterns;
    private List<String> excludePatterns;

    public ReleaseBundleUserPermission() {

    }

    public ReleaseBundleUserPermission(AceInfo aceInfo, PermissionTarget permissionTarget) {
        this.permissionName = permissionTarget.getName();
        this.effectivePermission = new EffectivePermission(aceInfo);
        if (permissionTarget instanceof ReleaseBundlePermissionTarget) {
            ReleaseBundlePermissionTarget releaseBundlePermissionTarget = ((ReleaseBundlePermissionTarget) permissionTarget);
            this.repoKeys = releaseBundlePermissionTarget.getRepoKeys();
            this.includePatterns = releaseBundlePermissionTarget.getIncludes();
            this.excludePatterns = releaseBundlePermissionTarget.getExcludes();
        }
    }
}
