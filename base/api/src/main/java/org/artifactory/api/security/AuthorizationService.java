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

import org.artifactory.repo.RepoPath;
import org.artifactory.security.*;
import org.jfrog.security.common.AuthorizationRoles;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * These are the usage of security data and logged in user methods.
 *
 * @author Fred Simon
 */
public interface AuthorizationService {
    String ROLE_USER = AuthorizationRoles.ROLE_USER;
    String ROLE_ADMIN = AuthorizationRoles.ROLE_ADMIN;

    /**
     * @return True if the current user can update her profile.
     */
    boolean isUpdatableProfile();

    /**
     * @return True if the current user is transient
     */
    boolean isTransientUser();

    /**
     * @return True if anonymous access is allowed.
     */
    boolean isAnonAccessEnabled();

    /**
     * @return True if the current user can read the specified path.
     */
    boolean canRead(RepoPath path);

    /**
     * @return True if the current user can annotate the specified path.
     */
    boolean canAnnotate(RepoPath repoPath);

    /**
     * @return True if the current user can delete the specified path.
     */
    boolean canDelete(RepoPath path);

    /**
     * @return True if the current user can deploy to the specified path.
     */
    boolean canDeploy(RepoPath path);

    /**
     * @return True if the user can deploy to at least one non-cache repositories.
     */
    boolean canDeployToLocalRepository();

    /**
     * @return True if the current user has admin permissions on a target info that includes this path..
     */
    boolean canManage(RepoPath path);

    /**
     * Indicates if the current user has the given repo permission, no matter the target
     *
     * @param artifactoryPermission Permission to check
     * @return True if the current user has such permission. False if not
     */
    boolean hasRepoPermission(ArtifactoryPermission artifactoryPermission);

    /**
     * Indicates if the current user has the given build permission, no matter the target
     *
     * @param artifactoryPermission Permission to check
     * @return True if the current user has such permission. False if not
     */
    boolean hasBuildPermission(ArtifactoryPermission artifactoryPermission);

    /**
     * Indicates if the current user has the given release bundle permission, no matter the target
     * NOTE: if the user doesn't have Edge/EntPlus license the release-bundle permission is hidden and therefore
     * return false.
     *
     * @param artifactoryPermission Permission to check
     * @return True if the current user has such permission. False if not
     */
    boolean hasReleaseBundlePermission(ArtifactoryPermission artifactoryPermission);

    /**
     * Indicates if the current user has the given permission on at least one of repo/build/release-bundles,
     * no matter the target
     *
     * @param artifactoryPermission Permission to check
     * @return True if the current user has such permission. False if not
     */
    boolean hasPermission(ArtifactoryPermission artifactoryPermission);

    boolean hasPermissionOnAcl(Acl<? extends PermissionTarget> acl, ArtifactoryPermission permission);

    /**
     * Indicates if the current user has build read permission on at least one build.
     * This is true if the user is admin or "Build Basic Read" flag is on, or he has an acl that grant him read permission on a build.
     *
     * @return True if the current user has such permission. False if not
     */
    boolean hasBuildBasicReadPermission();

    /**
     * Indicates if the current user has build read permission on ALL builds.
     * This is true if the user is admin or "Build Basic Read" flag is on, or he has an acl that grant him read permission on all builds.
     *
     * @return True if the current user has such permission. False if not
     */
    boolean hasBasicReadPermissionForAllBuilds();

    /**
     * Search the repo ACL list for the required permission, no matter the repoKey.
     *
     * @return True if the user has {@param permission} in any of the repo ACLs.
     */
    boolean hasAnyPermission(GroupInfo group, ArtifactoryPermission permission);

    /**
     * Returns a list of repo permission targets for the user for the type of permission given.
     *
     * @param user to search the permission for
     * @param permission Type of permission to find
     * @return List of permission target info objects
     */
    List<RepoPermissionTarget> getRepoPermissionTargets(UserInfo user, ArtifactoryPermission permission);

    /**
     * @return True if the user can read the specified path.
     */
    boolean canRead(UserInfo user, RepoPath path);

    /**
     * @return True if the user can annotate the specified path.
     */
    boolean canAnnotate(UserInfo user, RepoPath path);

    /**
     * @return True if the user can delete the specified path.
     */
    boolean canDelete(UserInfo user, RepoPath path);

    /**
     * @return True if the user can deploy to the specified path.
     */
    boolean canDeploy(UserInfo user, RepoPath path);

    /**
     * @return True if the user can administer the specified path.
     */
    boolean canManage(UserInfo user, RepoPath path);

    /**
     * @return True if users in the group can read the specified path.
     */
    boolean canRead(GroupInfo group, RepoPath path);

    /**
     * @return True if users in the group can annotate the specified path.
     */
    boolean canAnnotate(GroupInfo group, RepoPath path);

    /**
     * @return True if users in the group can delete the specified path.
     */
    boolean canDelete(GroupInfo group, RepoPath path);

    /**
     * @return True if users in the group can deploy to the specified path.
     */
    boolean canDeploy(GroupInfo group, RepoPath path);

    /**
     * @return True if users in the group can administer the specified path.
     */
    boolean canManage(GroupInfo group, RepoPath path);

    /**
     * @return True if the current is a system administrator.
     */
    boolean isAdmin();

    /**
     * @return True if the current user is a anonymous.
     */
    boolean isAnonymous();

    /**
     *
     * @return true if should require profile page unlock with password
     */
    boolean requireProfileUnlock();

    /**
     *
     * @return true if require profile unlock
     */
    boolean requireProfilePassword();

    /**
     * @return The current logged in username. {@link org.artifactory.api.security.SecurityService#USER_SYSTEM} is
     * returned if no login information is found.
     */
    @Nonnull
    String currentUsername();

    boolean isAuthenticated();

    /**
     * Indicates if the given user has any permissions at all, on the root repository. If it is a virtual repository
     * then if the user has a permission in any one of the real repositories associated with it will return {@code
     * true}
     *
     * @param repoKey The repository key of the repository to check the user's permissions.
     * @return Whether the user has any permissions on a repository root
     */
    boolean userHasPermissionsOnRepositoryRoot(String repoKey);

    boolean isDisableInternalPassword();

    /**
     * @return The current authentication user encrypted password if exists.
     */
    String currentUserEncryptedPassword();

    /**
     * @return The current authentication user encrypted password for internal users or else API key if exists.
     */
    String currentUserEncryptedPasswordOrApiKey();

    /**
     *
     */
    boolean isApiKeyAuthentication();

    /**
     * Getting a score per repo in the PermissionHeuristicScore range:
     * Rating is by the readability of the repo based on the current user permissions.
     */
    PermissionHeuristicScore getStrongestReadPermissionTarget(String repoKey);

    /**
     * @return True if the current user has basic read permission for specific root folder of a build.
     */
    boolean isBuildBasicRead(String buildName);

    /**
     * @return True if the current user has basic read permission for specific build.
     */
    boolean isBuildBasicRead(String buildName, String buildNumber, String buildStarted);

    /**
     * @return True if the current user has read permission for specific build.
     */
    boolean canReadBuild(String buildName);

    /**
     * @return True if the current user has read permission for specific build.
     */
    boolean canReadBuild(String buildName, String buildNumber);

    /**
     * @return True if the current user has read permission for specific build.
     */
    boolean canReadBuild(String buildName, String buildNumber, String buildStarted);

    /**
     * @return True if the current user can upload a specified build.
     */
    boolean canUploadBuild(String buildName, String buildNumber);

    /**
     * @return True if the current user can upload a specified build.
     */
    boolean canUploadBuild(String buildName, String buildNumber, String buildStarted);

    /**
     * @return True if the current user can delete a specified build.
     */
    boolean canDeleteBuild(String buildName);

    /**
     * @return True if the current user can delete a specified build.
     */
    boolean canDeleteBuild(String buildName, String buildNumber, String buildStarted);

    /**
     * @return True if the current user can manage a specified build.
     */
    boolean canManageBuild(String buildName, String buildNumber, String buildStarted);
}
