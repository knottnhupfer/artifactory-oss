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

package org.artifactory.api.security;

import com.google.common.collect.Multimap;
import org.artifactory.sapi.common.Lock;
import org.artifactory.security.*;

import java.util.List;
import java.util.Map;

/**
 * User: freds Date: Aug 5, 2008 Time: 8:46:40 PM
 */
public interface AclService {

    /**
     * Returns a list of repo permission targets for the current logged-in user for the type of permission given.
     *
     * @param permission Type of permission to find
     * @return List of permission target info objects
     */
    List<RepoPermissionTarget> getRepoPermissionTargets(ArtifactoryPermission permission);

    /**
     * @return all the AclInfos of repo permissions for the current logged-in user for the type of permission given.
     */
    List<RepoAcl> getAllRepoAcls(ArtifactoryPermission permission);

    /**
     * @return all the AclInfos of build permissions for the current logged-in user for the type of permission given.
     */
    List<BuildAcl> getAllBuildAcls(ArtifactoryPermission permission);

    /**
     * @return all the AclInfos of release-bundle permissions for the current logged-in user for the type of permission given.
     * NOTE: if user doesn't have EntPlus/Edge license we return an empty list (even if release-bundle acls exist!)
     */
    List<ReleaseBundleAcl> getAllReleaseBundleAcls(ArtifactoryPermission permission);

    /**
     * @return all the AclInfos of repo permissions
     */
    List<RepoAcl> getAllRepoAcls();

    /**
     * @return all the AclInfos of build permissions
     */
    List<BuildAcl> getAllBuildAcls();

    /**
     * @param getByLicense if false - return all the AclInfos of release-bundle permissions, otherwise - if user
     *                     doesn't have EntPlus/Edge license return an empty list (even if release-bundle acls exist!)
     */
    List<ReleaseBundleAcl> getAllReleaseBundleAcls(boolean getByLicense);

    /**
     * @return all the AclInfos of repo and build permissions
     * NOTE: if user doesn't have EntPlus/Edge license we return an empty list (even if release-bundle acls exist!)
     */
    List<Acl<? extends PermissionTarget>> getAllAcls();

    /**
     * @param target The permission target to check.
     * @return True if the current logged in user is admin OR has manage permission on the permission target
     */
    boolean canManage(PermissionTarget target);

    /**
     * @param acl The permission target acl to check.
     * @return True if the current logged in user is admin OR has manage permission on the permission target acl.
     */
    boolean canManage(Acl<? extends PermissionTarget> acl);

    /**
     * @return True is the user or a group the user belongs to has read permissions on the target
     */
    boolean canRead(UserInfo user, PermissionTarget target);

    /**
     * @return True is the user or a group the user belongs to has annotate permissions on the target
     */
    boolean canAnnotate(UserInfo user, PermissionTarget target);

    /**
     * @return True is the user or a group the user belongs to has deploy permissions on the target
     */
    boolean canDeploy(UserInfo user, PermissionTarget target);

    /**
     * @return True is the user or a group the user belongs to has delete permissions on the target
     */
    boolean canDelete(UserInfo user, PermissionTarget target);

    /**
     * @return True is the user or a group the user belongs to has admin permissions on the target
     */
    boolean canManage(UserInfo user, PermissionTarget target);

    @Lock
    void createAcl(Acl acl);

    @Lock
    void deleteAcl(Acl acl);

    void updateAcl(Acl acl);

    RepoAcl getRepoAcl(String permTargetName);

    BuildAcl getBuildAcl(String permTargetName);

    ReleaseBundleAcl getReleaseBundleAcl(String permTargetName);

    /**
     * @return release-bundle acl for given permission target name.
     * If user doesn't have EntPlus/Edge license return null (even if the release-bundle acl exists!)
     */
    ReleaseBundleAcl getReleaseBundleAclByLicense(String permTargetName);

    /**
     * Converts cached repo keys contained in the list so that the '-cache' suffix is omitted.
     * When provided with a remote or local repository key, it will stay unchanged.
     *
     * @param repoKeys
     * @return repoKeys with all '-cache' suffixes omitted
     */
    List<String> convertCachedRepoKeysToRemote(List<String> repoKeys);

    /**
     * Converts cached repo keys contained in the acl's permission target so that the '-cache' suffix is omitted.
     * When provided with a remote or local repository key, it will stay unchanged.
     *
     * @return a new MutableAclInfo with its permission target's repo keys modified to omit the '-cache' suffix
     */
    RepoAcl convertNewAclCachedRepoKeysToRemote(MutableRepoAcl acl);

    /**
     * return map permissions according to username and ACL type (repo/build)
     *
     * @param type type of permissions to return
     */
    Map<PermissionTarget, AceInfo> getUserPermissionByPrincipal(String username, ArtifactoryResourceType type);

    /**
     * return multi map permissions according to username and its groups ACL type (repo/build)
     *
     * @param type type of permissions to return
     */
    Multimap<PermissionTarget, AceInfo> getUserPermissionAndItsGroups(String username, ArtifactoryResourceType type);

    /**
     * get groups related permissions according to ACL type (repo/build)
     *
     * @param groups - groups to get permissions for
     * @param type type of permissions to return
     * @return -map of permissions and groups access rights
     */
    Multimap<PermissionTarget, AceInfo> getGroupsPermissions(List<String> groups, ArtifactoryResourceType type);

    /**
     * get user related permissions according to ACL type (repo/build)
     *
     * @param userName - userName to get permissions for
     * @param type type of permissions to return
     * @return -map of permissions and user access rights
     */
    Map<PermissionTarget, AceInfo> getUserPermissions(String userName, ArtifactoryResourceType type);

    /**
     * get all Acls mapped by permission target first character
     * @return a map of permission target first character to list of PermissionTargetAcls wrapper.
     */
    Map<Character, List<PermissionTargetAcls>> getAllAclsMappedByPermissionTargetFirstChar(boolean reversed);

    @Lock
    void createDefaultSecurityEntities(UserInfo anonUser, GroupInfo readersGroup, String currentUsername);
}
