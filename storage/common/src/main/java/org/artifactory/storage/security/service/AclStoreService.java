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

import org.artifactory.sapi.common.Lock;
import org.artifactory.security.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Date: 9/3/12
 * Time: 4:13 PM
 *
 * @author freds
 */
public interface AclStoreService {

    /**
     * @return Returns all the AclInfos
     */
    Collection<RepoAcl> getAllRepoAcls();

    Collection<BuildAcl> getAllBuildAcls();

    Collection<ReleaseBundleAcl> getAllReleaseBundleAcls();

    <T extends RepoPermissionTarget> AclCache<T> getAclCache();

    <T extends RepoPermissionTarget> AclCache<T> getBuildsAclCache();

    <T extends RepoPermissionTarget> AclCache<T> getReleaseBundlesAclCache();

    Map<Character, List<PermissionTargetAcls>> getMapPermissionTargetAcls(boolean reversed);

    @Lock
    void createAcl(Acl acl);

    @Lock
    void updateAcl(Acl acl);

    @Lock
    void deleteAcl(Acl acl);

    RepoAcl getRepoAcl(String permTargetName);

    BuildAcl getBuildAcl(String permTargetName);

    ReleaseBundleAcl getReleaseBundleAcl(String permTargetName);

    @Lock
    void removeAllUserAces(String username);

    @Lock
    void deleteAllAcls();

    void removeAllGroupAces(String groupName);

    String invalidateAclCache();

    void destroy();
}
