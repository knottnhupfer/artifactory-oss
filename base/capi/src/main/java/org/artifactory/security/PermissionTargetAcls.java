package org.artifactory.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * This class composes all three permissions target acls: repo acls, build acls and release bundle acls.
 *
 * @author Inbar Tal
 */
@Data
@AllArgsConstructor
public class PermissionTargetAcls {
    private String permissionTargetName;
    private RepoAcl repoAcl;
    private BuildAcl buildAcl;
    private ReleaseBundleAcl releaseBundleAcl;

    public PermissionTargetAcls(String permissionTargetName) {
        this.permissionTargetName = permissionTargetName;
    }

    public static int compareToIgnoreCase(PermissionTargetAcls permissionTargetAcls1, PermissionTargetAcls permissionTargetAcls2) {
        return permissionTargetAcls1.permissionTargetName.compareToIgnoreCase(permissionTargetAcls2.permissionTargetName);
    }

}
