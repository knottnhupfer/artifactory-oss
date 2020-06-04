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

import java.util.Set;

/**
 * Date: 8/2/11
 * Time: 10:22 AM
 *
 * @author Fred Simon
 */
public interface MutableAceInfo extends AceInfo {

    void setPrincipal(String principal);

    void setGroup(boolean group);

    void setManagedXrayMeta(boolean xrayMeta);

    void setManagedXrayWatches(boolean xrayWatches);

    void setMask(int mask);

    void setManage(boolean manage);

    void setDelete(boolean delete);

    void setDeploy(boolean deploy);

    void setAnnotate(boolean annotate);

    void setRead(boolean read);

    /**
     * Used by the {@see AclMapper} and the v1 rest api - converts strings in the form of 'r','w' etc. to {@link ArtifactoryPermission}
     */
    void setPermissionsFromStrings(Set<String> permissionStrings);

    /**
     * Used by the v2 rest api - converts strings in the form of 'read','write' etc. to {@link ArtifactoryPermission}
     * @throws IllegalArgumentException in case one of the actions provided is invalid
     */
    void setPermissionsFromDisplayNames(Set<String> permissionDisplayNames) throws IllegalArgumentException;

    /**
     * Used by the UI - converts strings in the form of 'read','write' etc. to {@link ArtifactoryPermission}
     */
    void setPermissionsFromUiNames(Set<String> permissionDisplayNames);
}
