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

import lombok.Getter;
import org.artifactory.security.AceInfo;
import org.artifactory.security.BuildPermissionTarget;
import org.artifactory.security.PermissionTarget;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.EffectivePermission;

import java.util.List;

/**
 * Used for the various effective permissions views
 *
 * @author Yuval Reches
 */
@Getter
public class BuildUserPermission extends UserPermission {

    private List<String> includePatterns;
    private List<String> excludePatterns;

    BuildUserPermission() {
    }

    public BuildUserPermission(AceInfo aceInfo, PermissionTarget permissionTarget) {
        this.permissionName = permissionTarget.getName();
        this.effectivePermission = new EffectivePermission(aceInfo);
        if (permissionTarget instanceof BuildPermissionTarget) {
            BuildPermissionTarget buildPermissionTarget = (BuildPermissionTarget) permissionTarget;
            this.includePatterns = buildPermissionTarget.getIncludes();
            this.excludePatterns = buildPermissionTarget.getExcludes();
        }
    }
}
