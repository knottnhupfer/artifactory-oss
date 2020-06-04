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

package org.artifactory.security;

import java.util.Arrays;

/**
 * @author Yoav Landman
 */
public enum ArtifactoryPermission {
    // Permissions ordered by most frequently accessed, for optimal time in any permission needed situations

    // Repositories
    READ(0, "r", "read", "read"), DEPLOY(1, "w", "write", "upload"), ANNOTATE(2, "n", "annotate", "annotate"),
    DELETE(3, "d", "delete", "delete"), MANAGE(4, "m", "manage", "manage"),
    MANAGED_XRAY_META(5, "mxm", "managedXrayMeta", "managedXrayMeta"),
    MANAGED_XRAY_WATCHES(6, "mxw", "managedXrayWatches", "managedXrayWatches"),
    MANAGED_POLICIES(7, "mxp", "managedXrayPolicies", "managedXrayPolicies"),
    DISTRIBUTE(8, "x", "distribute", "distribute");

    private static final String ERR_INVALID_PERMISSION = "'%s' is not a valid Artifactory permission.";

    private final int mask;
    private final String string;
    private final String displayName;
    private final String uiName;

    ArtifactoryPermission(int bitPos, String string,  String displayName, String uiName) {
        this.mask = 1 << bitPos;
        this.string = string;
        this.displayName = displayName;
        this.uiName = uiName; //Provided for ease of use by the ui
    }

    public int getMask() {
        return mask;
    }

    /**
     * This string representation of a permission action is how we save it in access
     */
    public String getString() {
        return string;
    }

    /**
     * This string representation of a permission action is displayed to the user in ui / v2 rest endpoints
     */
    public String getDisplayName() {
        return displayName;
    }

    public String getUiName() {
        return uiName;
    }

    /**
     * Get {@link ArtifactoryPermission} by display name convention
     */
    public static ArtifactoryPermission fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(permission -> permission.displayName.equalsIgnoreCase(displayName))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format(ERR_INVALID_PERMISSION, displayName)));
    }

    /**
     * Get {@link ArtifactoryPermission} by shorthand ('string') convention
     */
    public static ArtifactoryPermission fromString(String string) {
        return Arrays.stream(values())
                .filter(permission -> permission.string.equalsIgnoreCase(string))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format(ERR_INVALID_PERMISSION, string)));
    }
}
