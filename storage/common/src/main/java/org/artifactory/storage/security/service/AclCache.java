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

package org.artifactory.storage.security.service;

import org.artifactory.security.PrincipalPermission;
import org.artifactory.security.RepoPermissionTarget;

import java.util.Map;
import java.util.Set;

/**
 * @author nadavy
 */
public class AclCache<T extends RepoPermissionTarget> {

    // Map of user/group name to a map of repoPath/buildName to aclInfo
    private Map<String, Map<String, Set<PrincipalPermission<T>>>> groupResultMap;
    private Map<String, Map<String, Set<PrincipalPermission<T>>>> userResultMap;

    public AclCache(Map<String, Map<String, Set<PrincipalPermission<T>>>> groupResultMap,
            Map<String, Map<String, Set<PrincipalPermission<T>>>> userResultMap) {
        this.groupResultMap = groupResultMap;
        this.userResultMap = userResultMap;
    }

    public Map<String, Map<String, Set<PrincipalPermission<T>>>> getGroupResultMap() {
        return groupResultMap;
    }

    public Map<String, Map<String, Set<PrincipalPermission<T>>>> getUserResultMap() {
        return userResultMap;
    }

}
