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

import com.google.common.collect.ImmutableSetMultimap;
import org.artifactory.api.security.PasswordExpiryUser;
import org.artifactory.md.Properties;
import org.artifactory.sapi.common.Lock;
import org.artifactory.security.*;
import org.artifactory.spring.ReloadableBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Date: 8/27/12
 * Time: 2:44 PM
 *
 * @author freds
 */
public interface UserGroupStoreService extends ReloadableBean {

    /**
     * @param username The unique username
     * @return UserInfo containing user and group information if user with the input username exists, null otherwise
     */
    @Nullable
    UserInfo findUser(String username);

    @Lock
    void updateUser(MutableUserInfo user);

    @Lock
    boolean createUser(UserInfo user);

    @Lock
    boolean createUserWithProperties(UserInfo user, boolean addUserProperties);

    @Lock
    void deleteUser(String username);

    List<UserInfo> getAllUsers(boolean includeAdmins, boolean includingPasswords);

    /**
     * Deletes the group from the database including any group membership users have to this group.
     *
     * @param groupName The group name to delete
     * @return True if the group and/or membership in this group was deleted
     */
    @Lock
    boolean deleteGroup(String groupName);

    List<GroupInfo> getAllGroups();

    Map<String, GroupInfo> getAllGroupsByNames(List<String> groupNames);

    /**
     * @return A set of all the groups that should be added by default to newly created users.
     */
    List<GroupInfo> getNewUserDefaultGroups();

    /**
     * @return A list of all groups that are of an external realm
     */
    List<GroupInfo> getAllExternalGroups();

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
    @Lock
    boolean updateGroup(MutableGroupInfo groupInfo);

    @Lock
    boolean createGroup(GroupInfo groupInfo);

    /**
     * Adds a list of users to a group.
     *
     * @param groupName The group's unique name.
     * @param usernames The list of usernames.
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
    List<String>  findUsersInGroup(String groupName);

    /**
     * Find the group info object for this group name
     *
     * @param groupName The name of the group to find
     * @return the group information if group with name found, null otherwise
     */
    @Nullable
    GroupInfo findGroup(String groupName);

    @Lock
    void deleteAllGroupsAndUsers();

    boolean adminUserExists();

    boolean userExists(String username);

    /**
     * Find the user associated with the given external user datum
     *
     * @param key           The key associated with this datum
     * @param val           The value to search for
     * @param exactKeyMatch Search for {@param key} with equals or endsWith?
     * @return The user, or null if no user was found
     */
    @Nullable
    UserInfo findUserByProperty(String key, String val, boolean exactKeyMatch);


    /**
     * Find the datum associated with the given user
     *
     * @param username The user holding this datum
     * @param key      The key associated with this datum
     * @return The datum, or null if no datum was found
     */
    @Nullable
    String findUserProperty(String username, String key);

    /**
     * Add or alter an external user datum
     *
     * @param username The name of the user to alter
     * @param key      The key associated with this datum
     * @param val      The value to write
     * @return True if the write succeeded, false otherwise
     */
    @Lock
    boolean addUserProperty(String username, String key, String val);

    /**
     * Delete an external user datum
     *
     * @param username The name of the user to alter
     * @param key      The key associated with this datum
     * @return True if the delete succeeded, false otherwise
     */
    boolean deleteUserProperty(String username, String key);

    Properties findPropertiesForUser(String username);

    void deletePropertyFromAllUsers(String propertyKey);

    /**
     * Locks user upon incorrect login attempt
     *
     * @param userName
     */
    @Lock
    void lockUser(@Nonnull String userName);

    /**
     * Unlocks locked out user
     *
     * @param userName
     */
    @Lock
    void unlockUser(@Nonnull String userName);

    /**
     * Checks if the user locked
     */
    boolean isUserLocked(String userName);

    /**
     * Unlocks all locked out users
     */
    @Lock
    void unlockAllUsers();

    /**
     * Unlocks all locked out admin users
     */
    void unlockAdminUsers();

    /**
     * @return List of locked in users
     */
    Set<String> getLockedUsers();

    /**
     * Changes user password
     *
     * @param user
     * @param saltedPassword
     *
     * @param rawPassword
     * @return sucess/failure
     */
    @Lock
    void changePassword(@Nonnull UserInfo user, SaltedPassword saltedPassword, String rawPassword);

    /**
     * Makes user password expired
     *
     * @param userName
     */
    @Lock
    void expireUserPassword(String userName);

    /**
     * Makes user password not expired
     *
     * @param userName
     */
    @Lock
    void revalidatePassword(String userName);

    /**
     * Makes all users passwords expired
     *
     */
    @Lock
    void expirePasswordForAllUsers();

    /**
     * Makes all users passwords not expired
     *
     */
    @Lock
    void revalidatePasswordForAllUsers();

    /**
     * Fetches users which password is about to expire
     *
     * @param daysToNotifyBefore           days before password expiry to notify users
     * @param daysToKeepPassword           after what period password should be changed
     * @return list of users
     */
    Set<PasswordExpiryUser> getUsersWhichPasswordIsAboutToExpire(int daysToNotifyBefore, int daysToKeepPassword);

    /**
     * Checks whether user password is expired
     *
     * @param userName a user name
     * @param expireIn days for password to stay valid after creation
     *
     * @return boolean
     */
    @Deprecated
    boolean isUserPasswordExpired(String userName, int expireIn);


    /**
     * Marks user.credentialsExpired=True where password has expired
     *
     * @param daysToKeepPassword after what period password should be changed
     * @return List of users that were expired
     */
    List<String> markUsersCredentialsExpired(int daysToKeepPassword);

    /**
     * Expires a batch of user ids <b>in a single transaction</b>
     * @param userIds       user ids to expire password for
     * @throws SQLException
     */
    @Lock
    void expirePasswordForUserIds(Set<Long> userIds) throws SQLException;

    /**
     * Updates the users last login time and last login IP address
     * @param username - the username of the updated user
     * @param lastLoginTimeMillis - the last login time in millis
     * @param lastLoginIp - the IP address of the last login
     */
    void updateUserLastLogin(String username, long lastLoginTimeMillis, String lastLoginIp);

    /**
     * @param userName
     * @return the date when last password was created
     */
    Long getUserPasswordCreationTime(String userName);

    Map<String, Boolean> getAllUsersAndAdminStatus(boolean justAdmins);

    List<String> getAllAdminGroupsNames();

    ImmutableSetMultimap<String, String> getAllUsersInGroups();
}