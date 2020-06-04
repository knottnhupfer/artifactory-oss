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

import com.google.common.collect.ImmutableSetMultimap;
import org.artifactory.bundle.BundleNameAndRepo;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.common.Lock;
import org.artifactory.security.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Fred Simon
 */
public interface UserGroupService {
    String UI_VIEW_BLOCKED_USER_PROP = "blockUiView";

    UserInfo currentUser();

    /**
     * Returns the user details for the given username.
     *
     * @param username The unique username
     * @return UserInfo if user with the input username exists
     */
    @Nonnull
    UserInfo findUser(String username);

    /**
     * Returns the user details including groups and wanted properties for the given username.
     *
     * @param username             The unique username
     * @param errorOnAbsence       throw error if user is not found
     * @return UserInfo including groups and wanted properties if user with the input username exists
     */
    @Nullable
    UserInfo findUser(String username, boolean errorOnAbsence);

    void updateUser(MutableUserInfo user, boolean activateListeners);

    void updateUserLastLogin(String username, long lastLoginTimeMillis, String lastLoginIp);

    @Lock
    boolean createUser(MutableUserInfo user);

    @Lock
    boolean createUserWithNoUIAccess(MutableUserInfo user);

    @Lock
    void deleteUser(String username);

    List<UserInfo> getAllUsers(boolean includeAdmins);

    List<UserInfo> getAllUsers(boolean includeAdmins, boolean includingPasswords);

    Map<String, Boolean> getAllUsersAndAdminStatus(boolean justAdmins);

    /**
     * Get all ACLs that has permissions on given repo path
     */
    List<RepoAcl> getRepoPathAcls(RepoPath repoPath);

    /**
     * Get all ACLs that has permissions on given build name
     */
    List<BuildAcl> getBuildPathAcls(String buildName, String buildNumber, String buildStarted);

    /**
     * Get all ACLs that has permissions on given build name. repo path is expected to be encoded by UI
     * or by {@see BuildServiceUtils#getBuildJsonPathInRepo}
     */
    List<BuildAcl> getBuildPathAcls(RepoPath repoPath);

    /**
     * Get all release bundle ACLs that has permission on given path
     * NOTE: if user doesn't have EntPlus/Edge license we return an empty list (even if release-bundle acls exist!)
     */
    List<ReleaseBundleAcl> getReleaseBundleAcls(RepoPath repoPath);

    boolean isBuildInPermissions(List<String> includePatterns, List<String> excludePatterns, String buildName);

    boolean isReleaseBundleInPermission(List<String> includePatterns, List<String> excludePatterns, BundleNameAndRepo bundle);

    /**
     * Deletes the group from the database including any group membership users have to this group.
     *
     * @param groupName The group name to delete
     */
    @Lock
    void deleteGroup(String groupName);

    List<GroupInfo> getAllGroups();

    List<String> getAllAdminGroupsNames();

    /**
     * @return A set of all the groups that should be added by default to newly created users.
     */
    List<GroupInfo> getNewUserDefaultGroups();

    /**
     * @return All the external realm groups
     */
    Map<String, GroupInfo> getAllExternalGroups();

    Map<String, GroupInfo> getAllGroupsByGroupNames(List<String> groupNames);

    /**
     * @return A list of <b>internal</b> groups only
     */
    List<GroupInfo> getInternalGroups();

    /**
     * @return A set of all the groups names that should be added by default to newly created users.
     */
    Set<String> getNewUserDefaultGroupsNames();

    /**
     * Updates a users group. Group name update is not allowed.
     *
     * @param groupInfo The updated group info
     */
    void updateGroup(MutableGroupInfo groupInfo);

    @Lock
    boolean createGroup(MutableGroupInfo groupInfo);

    /**
     * remove users group before update and add users group after update
     *
     * @param groupInfo    - update group
     * @param usersInGroup - users after group update
     */
    @Lock
    void updateGroupUsers(MutableGroupInfo groupInfo, List<String> usersInGroup);

    /**
     * Adds a list of users to a group.
     *
     * @param groupName The group's unique name.
     * @param usernames The list of users names.
     */
    @Lock
    void addUsersToGroup(String groupName, List<String> usernames);

    /**
     * Deletes the user's membership of a group.
     *
     * @param groupName The group name
     * @param usernames The list of usernames
     */
    @Lock
    void removeUsersFromGroup(String groupName, List<String> usernames);

    /**
     * Locates the users who are members of a group
     *
     * @param groupName the group whose members are required
     * @return the usernames of the group members
     */
    List<String> findUsersInGroup(String groupName);

    String resetPassword(String userName, String remoteAddress, String resetPageUrl);

    /**
     * For use with external authentication methods only (CAS/LDAP/SSO/Crowd) tries to locate a user with the given
     * name. When can't be found a new user will be created. The user will have no defined email, will not be an admin,
     * and will not have an updatable profile.
     *
     * @param username      The username to find/create
     * @param transientUser If true a transient user will be created and will cease to exists when the session ends. If
     *                      the user already exist in Artifactory users, this flag has no meaning.
     * @return Found\created user
     */
    UserInfo findOrCreateExternalAuthUser(String username, boolean transientUser);

    /**
     * For use with external authentication methods only (SSO , SAML and OAUTH) tries to locate a user with the given
     * name. When can't be found a new user will be created. The user will have no defined email, will not be an admin,
     * and will not have an updatable profile.
     *
     * @param username      The username to find/create
     * @param transientUser If true a transient user will be created and will cease to exists when the session ends. If
     *                      the user already exist in Artifactory users, this flag has no meaning.
     * @param updateProfile - if true , user will be able to update it own profile
     * @return Found\created user
     */
    UserInfo findOrCreateExternalAuthUser(String username, boolean transientUser, boolean updateProfile);

    /**
     * Returns the group details for the group name provided.
     *
     * @param groupName The name of the group to look for
     * @return The group details, null if no group with this name found
     */
    @Nullable
    GroupInfo findGroup(String groupName);

    String createEncryptedPasswordIfNeeded(UserInfo user, String password);

    Properties findPropertiesForUser(String username);

    boolean addUserProperty(String username, String key, String value);

    String getUserProperty(String username, String key);

    void deleteUserProperty(String userName, String propertyKey);

    boolean updateUserProperty(String username, String key, String value);


    void deletePropertyFromAllUsers(String propertyKey);

    String getPropsToken(String userName, String propsKey);

    @Lock
    boolean revokePropsToken(String userName, String propsKey);

    @Lock
    boolean createPropsToken(String userName, String key, String value);

    @Lock
    void revokeAllPropsTokens(String propsKey);

    @Lock
    boolean updatePropsToken(String user, String propKey, String propValue);

    /**
     * Locks user upon incorrect login attempt
     */
    void lockUser(@Nonnull String userName);

    /**
     * Unlocks locked in user
     */
    void unlockUser(@Nonnull String userName);

    /**
     * Unlocks all locked in users
     */
    void unlockAllUsers();

    /**
     * Registers incorrect login attempt
     */
    void registerIncorrectLoginAttempt(@Nonnull String userName);

    /**
     * Resets logon failures
     */
    void resetIncorrectLoginAttempts(@Nonnull String userName);

    /**
     * @return List of locked in users
     */
    Set<String> getLockedUsers();

    /**
     * Map of group id to multiple users ids
     */
    ImmutableSetMultimap<String, String> getAllUsersInGroups();

    boolean adminUserExists();
}
