package org.artifactory.ui.rest.model.admin.security.user;

import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.EffectivePermission;

/**
 * @author Yuval Reches
 */
public abstract class UserPermission {
    protected String permissionName;
    protected EffectivePermission effectivePermission;

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public EffectivePermission getEffectivePermission() {
        return effectivePermission;
    }

    public void setEffectivePermission(EffectivePermission effectivePermission) {
        this.effectivePermission = effectivePermission;
    }

}
