/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission;

import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * This guy represents a single 'effective permission' for a single principal (user or group)
 *
 * @author Chen Keinan
 */
public class PrincipalEffectivePermissions extends BaseArtifactInfo {

    /**
     * Governs the max permission targets to show in the main view (inside each column), above this max user must press
     * a button to trigger a second backend call that provides all permission targets for view.
     */
    @JsonIgnore
    private static final int PERMISSION_TARGET_CAP = 5;

    private String principal;
    private boolean admin;
    private List<String> permissionTargets;
    private EffectivePermission permission;
    private int permissionTargetsCount;
    private boolean permissionTargetsCap; // flag will cause the ui to show a tooltip

    public PrincipalEffectivePermissions() {
        this.permissionTargets = new ArrayList<>(PERMISSION_TARGET_CAP);
        this.permission = new EffectivePermission();
        permissionTargetsCount = 0;
    }

    public PrincipalEffectivePermissions(String username) {
        this();
        this.principal = username;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public List<String> getPermissionTargets() {
        return permissionTargets;
    }

    public void setPermissionTargets(List<String> permissionTargets) {
        this.permissionTargets = permissionTargets;
    }

    public EffectivePermission getPermission() {
        return permission;
    }

    public void setPermission(EffectivePermission permission) {
        this.permission = permission;
    }

    public boolean isPermissionTargetsCap() {
        return permissionTargets.size() == PERMISSION_TARGET_CAP;
    }

    public void setPermissionTargetsCap(boolean permissionTargetsCap) {
        this.permissionTargetsCap = permissionTargetsCap;
    }

    public static int getPermissionTargetCap() {
        return PERMISSION_TARGET_CAP;
    }

    public int getPermissionTargetsCount() {
        return permissionTargetsCount;
    }

    public void setPermissionTargetsCount(int permissionTargetsCount) {
        this.permissionTargetsCount = permissionTargetsCount;
    }

    public void advancePermissionTargetsCount() {
        permissionTargetsCount++;
    }
}
