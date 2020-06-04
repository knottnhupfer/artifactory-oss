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

package org.artifactory.ui.rest.model.admin.security.user;

import org.artifactory.security.AceInfo;
import org.artifactory.security.PermissionTarget;
import org.artifactory.security.RepoPermissionTarget;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.EffectivePermission;

import java.util.List;

/**
 * Used for the various effective permissions views
 *
 * @author Chen keinan
 */
public class RepoUserPermission extends UserPermission {

    private List<String> repoKeys;
    private int numOfRepos;

    RepoUserPermission() {
    }

    public RepoUserPermission(AceInfo aceInfo, RepoPermissionTarget permissionTarget, int numOfRepos) {
        this.permissionName = permissionTarget.getName();
        this.effectivePermission = new EffectivePermission(aceInfo);
        this.repoKeys = permissionTarget.getRepoKeys();
        this.numOfRepos = numOfRepos;
    }

    public RepoUserPermission(AceInfo aceInfo, PermissionTarget permissionTarget) {
        this.permissionName = permissionTarget.getName();
        if (permissionTarget instanceof RepoPermissionTarget) {
            this.repoKeys = ((RepoPermissionTarget) permissionTarget).getRepoKeys();
        }
        this.effectivePermission = new EffectivePermission(aceInfo);
    }

    public RepoUserPermission(AceInfo aceInfo, String permissionName, int numOfRepos) {
        this.permissionName = permissionName;
        this.repoKeys = null;
        this.effectivePermission = new EffectivePermission(aceInfo);
        this.numOfRepos = numOfRepos;
    }

    public List<String> getRepoKeys() {
        return repoKeys;
    }

    public void setRepoKeys(List<String> repoKeys) {
        this.repoKeys = repoKeys;
    }

    public int getNumOfRepos() {
        return numOfRepos;
    }

    public void setNumOfRepos(int numOfRepos) {
        this.numOfRepos = numOfRepos;
    }
}
