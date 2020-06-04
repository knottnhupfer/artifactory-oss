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

package org.artifactory.storage.db.security.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.*;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.model.xstream.security.UserProperty;
import org.artifactory.security.*;
import org.artifactory.security.exceptions.PasswordChangeException;
import org.artifactory.security.props.auth.PropsTokenManager;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.security.dao.UserGroupsDao;
import org.artifactory.storage.db.security.dao.UserPropertiesDao;
import org.artifactory.storage.db.security.entity.Group;
import org.artifactory.storage.db.security.entity.User;
import org.artifactory.storage.db.security.entity.UserGroup;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.config.diff.DataDiff;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Date: 8/27/12
 * Time: 2:47 PM
 *
 * @deprecated use {@link org.artifactory.storage.db.security.service.access.AccessUserGroupStoreService}
 * @author freds
 */
@Deprecated
public abstract class UserGroupStoreServiceImpl implements UserGroupStoreService {
    public static final int MAX_USERS_TO_TRACK = 10000; // max locked users to keep in cache
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = LoggerFactory.getLogger(UserGroupStoreServiceImpl.class);

    // max delay for user to be suspended (5 seconds)
    private static final int MAX_LOGIN_DELAY = 5000;
    private static final int OK_INCORRECT_LOGINS = 2; // delay will start after OK_INCORRECT_LOGINS+1 attempts
    private static final int LOGIN_DELAY_MULTIPLIER = getLoginDelayMultiplier();
    private static final boolean CACHE_BLOCKED_USERS = ConstantValues.useFrontCacheForBlockedUsers.getBoolean();
    private final long MILLIS_IN_DAY = /* secs in day */ 86400L * 1000L /* millis in sec */;

    // cache meaning  <userName, lock-time>
    private final Cache<String, Long> lockedUsersCache = CacheBuilder.newBuilder().maximumSize(MAX_USERS_TO_TRACK).
            expireAfterWrite(24, TimeUnit.HOURS).build();
    private final Cache<String, Long> userAccessUsersCache = CacheBuilder.newBuilder().maximumSize(MAX_USERS_TO_TRACK).
            expireAfterWrite(24, TimeUnit.HOURS).build();
    private final Map<String, AtomicInteger> incorrectLoginAttemptsCache = Maps.newConcurrentMap();
    private SecurityService securityService;
    @Autowired
    private DbService dbService;

    @Autowired
    private UserGroupsDao userGroupsDao;

    @Autowired
    private UserPropertiesDao userPropertiesDao;

    /**
     * Calculates user login delay multiplier,
     * the value (security.loginBlockDelay) is
     * taken from system properties file,
     * <p/>
     * delay may not exceed {@link UserGroupStoreServiceImpl#MAX_LOGIN_DELAY}
     *
     * @return user login delay multiplier
     */
    private static int getLoginDelayMultiplier() {
        int userDefinedDelayMultiplier = ConstantValues.loginBlockDelay.getInt();
        if (userDefinedDelayMultiplier <= MAX_LOGIN_DELAY) {
            return userDefinedDelayMultiplier;
        }
        log.warn(
                String.format(
                        "loginBlockDelay '%d' has exceeded maximum allowed delay '%d', " +
                                "which will be used instead", userDefinedDelayMultiplier, MAX_LOGIN_DELAY
                )
        );
        return MAX_LOGIN_DELAY;
    }

    @Override
    public void deleteAllGroupsAndUsers() {
        try {
            userGroupsDao.deleteAllGroupsAndUsers();
        } catch (SQLException e) {
            throw new StorageException("Could not delete all users and groups", e);
        }
    }

    @Override
    public boolean adminUserExists() {
        try {
            return userGroupsDao.adminUserExists();
        } catch (SQLException e) {
            throw new StorageException("Could not determine if admin users exists due to: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean userExists(String username) {
        try {
            return userGroupsDao.findUserIdByUsername(username) > 0L;
        } catch (SQLException e) {
            throw new StorageException("Could not execute exists query for username='" + username + "'", e);
        }
    }

    @Override
    public UserInfo findUser(String username) {
        try {
            User user = userGroupsDao.findUserByName(username);
            if (user == null) {
                return null;
            }
                Set<UserPropertyInfo> allUserProperties = new HashSet<>(
                        userPropertiesDao.getPropertiesForUser(username));
                return userToUserInfoWithProperties(user, allUserProperties);
        } catch (SQLException e) {
            throw new StorageException("Could not execute search query for username='" + username + "'", e);
        }
    }

    /**
     * Finds user in DB and returns it as is
     * without attaching any extra metadata such
     * as groups for instance
     *
     * @return {@link User}
     */
    private User getUser(String userName) {
        try {
            return userGroupsDao.findUserByName(userName);
        } catch (SQLException e) {
            throw new StorageException("Could not execute search query for username='" + userName + "'", e);
        }
    }

    @Override
    public void updateUser(MutableUserInfo userInfo) {
        try {
            User originalUser = userGroupsDao.findUserByName(userInfo.getUsername());
            if (originalUser == null) {
                throw new UsernameNotFoundException(
                        String.format("Cannot update user: '%s' since it does not exist!", userInfo.getUsername()));
            }
            User updatedUser = userInfoToUser(originalUser.getUserId(), userInfo);
            userGroupsDao.updateUser(updatedUser);

            // change passwordExpired state if user changes password during update
            if (originalUser.isCredentialsExpired() == updatedUser.isCredentialsExpired() &&
                    // admin not changing password expired state
                    !originalUser.getPassword().equals(updatedUser.getPassword())) {   // user indeed changed password
                userGroupsDao.unexpirePassword(originalUser.getUsername());
            }
        } catch (SQLException e) {
            throw new StorageException(String.format("Failed to update user: '%s'", userInfo.getUsername()), e);
        }
    }

    @Override
    public boolean createUser(UserInfo user) {
        return createUserWithProperties(user, false);
    }

    @Override
    public boolean createUserWithProperties(UserInfo user, boolean addUserProperties) {
        try {
            if (userExists(user.getUsername())) {
                return false;
            }
            User u = userInfoToUser(dbService.nextId(), user);
            int createUserSucceeded = userGroupsDao.createUser(u);
            Set<UserPropertyInfo> userProperties = user.getUserProperties();
            if (addUserProperties && userProperties != null && !userProperties.isEmpty()) {
                for (UserPropertyInfo userPropertyInfo : userProperties) {
                    String propKey = userPropertyInfo.getPropKey();
                    String propValue = userPropertyInfo.getPropValue();
                    propValue = shouldEncryptProperty(propKey) ?
                            CryptoHelper.encryptIfNeeded(ArtifactoryHome.get(), propValue) : propValue;
                    userPropertiesDao.addUserPropertyById(u.getUserId(), propKey, propValue);
                }
            }
            boolean result = createUserSucceeded > 0;
            if (result) {
                SecurityService securityService = getSecurityService();
                // if user was previously locked as unknown user
                // and create succeeded, we unlock it
                if (securityService instanceof UserGroupService) {
                    ((UserGroupService) securityService).unlockUser(user.getUsername());
                }
            }
            return result;
        } catch (SQLException e) {
            throw new StorageException(String.format("Failed to create user: '%s'", user.getUsername()), e);
        }
    }

    /**
     * @return {@link SecurityService}
     */
    private SecurityService getSecurityService() {
        if (securityService == null) {
            securityService = ContextHelper.get().beanForType(SecurityService.class);
        }
        return securityService;
    }

    @Override
    public void deleteUser(String username) {
        try {
            userGroupsDao.deleteUser(username);
        } catch (SQLException e) {
            throw new StorageException(String.format("Failed to delete user: '%s'", username), e);
        }
    }

    @Override
    public List<UserInfo> getAllUsers(boolean includeAdmins, boolean includePasswords) {
        List<UserInfo> results = new ArrayList<>();
        try {
            Collection<User> allUsers = userGroupsDao.getAllUsers(includeAdmins);
            Map<Long, Set<UserPropertyInfo>> allUserProperties = userPropertiesDao.getAllUsersProperties();
            for (User user : allUsers) {
                UserInfo userInfo = userToUserInfoWithProperties(user, allUserProperties.get(user.getUserId()));
                results.add(userInfo);
            }
            return results;
        } catch (SQLException e) {
            throw new StorageException("Could not execute get all users query", e);
        }
    }

    @Override
    public boolean deleteGroup(String groupName) {
        try {
            return userGroupsDao.deleteGroup(groupName) > 0;
        } catch (SQLException e) {
            throw new StorageException("Failed to delete group " + groupName, e);
        }
    }

    private List<GroupInfo> findAllGroups(UserGroupsDao.GroupFilter groupFilter) {
        List<GroupInfo> results = new ArrayList<>();
        try {
            Collection<Group> allGroups = userGroupsDao.findGroups(groupFilter);
            for (Group group : allGroups) {
                results.add(groupToGroupInfo(group));
            }
            return results;
        } catch (SQLException e) {
            throw new StorageException("Could not execute get all groups query", e);
        }
    }

    @Override
    public List<GroupInfo> getAllGroups() {
        return findAllGroups(UserGroupsDao.GroupFilter.ALL);
    }

    @Override
    public List<GroupInfo> getNewUserDefaultGroups() {
        return findAllGroups(UserGroupsDao.GroupFilter.DEFAULTS);
    }

    @Override
    public List<GroupInfo> getAllExternalGroups() {
        return findAllGroups(UserGroupsDao.GroupFilter.EXTERNAL);
    }

    @Override
    public List<GroupInfo> getInternalGroups() {
        return findAllGroups(UserGroupsDao.GroupFilter.INTERNAL);
    }

    @Override
    public Set<String> getNewUserDefaultGroupsNames() {
        Set<String> results = new HashSet<>();
        try {
            Collection<Group> allGroups = userGroupsDao.findGroups(UserGroupsDao.GroupFilter.DEFAULTS);
            for (Group group : allGroups) {
                results.add(group.getGroupName());
            }
            return results;
        } catch (SQLException e) {
            throw new StorageException("Could not execute get all default group names query", e);
        }
    }

    @Override
    public boolean updateGroup(MutableGroupInfo groupInfo) {
        try {
            Group originalGroup = userGroupsDao.findGroupByName(groupInfo.getGroupName());
            if (originalGroup == null) {
                throw new GroupNotFoundException("Cannot update non existent group '" + groupInfo.getGroupName() + "'");
            }
            Group newGroup = groupInfoToGroup(originalGroup.getGroupId(), groupInfo);
            if (userGroupsDao.updateGroup(newGroup) != 1) {
                throw new StorageException("Updating group did not find corresponding entity" +
                        " based on name='" + groupInfo.getGroupName() + "' and id=" + originalGroup.getGroupId());
            }
        } catch (SQLException e) {
            throw new StorageException("Could not update group " + groupInfo.getGroupName(), e);
        }
        return false;
    }

    @Override
    public boolean createGroup(GroupInfo groupInfo) {
        try {
            if (userGroupsDao.findGroupByName(groupInfo.getGroupName()) != null) {
                // Group already exists
                return false;
            }
            Group g = groupInfoToGroup(dbService.nextId(), groupInfo);
            return userGroupsDao.createGroup(g) > 0;
        } catch (SQLException e) {
            throw new StorageException("Could not create group " + groupInfo.getGroupName(), e);
        }
    }

    @Override
    public void addUsersToGroup(String groupName, List<String> usernames) {
        Group group;
        try {
            group =  userGroupsDao.findGroupByName(groupName);
            if (group == null) {
                throw new GroupNotFoundException("Cannot add users to non existent group " + groupName);
            }
            userGroupsDao.addUsersToGroup(group.getGroupId(), usernames, group.getRealm());
        } catch (SQLException e) {
            throw new StorageException("Could not add users " + usernames + " to group " + groupName, e);
        }
    }

    @Override
    public void removeUsersFromGroup(String groupName, List<String> usernames) {
        try {
            Group group = userGroupsDao.findGroupByName(groupName);
            if (group == null) {
                throw new GroupNotFoundException("Cannot remove users to non existent group " + groupName);
            }
            userGroupsDao.removeUsersFromGroup(group.getGroupId(), usernames);
        } catch (SQLException e) {
            throw new StorageException("Could not add users " + usernames + " to group " + groupName, e);
        }
    }

    @Override
    public List<String> findUsersInGroup(String groupName) {
        try {
            Group groupByName = userGroupsDao.findGroupByName(groupName);
            if (groupByName == null) {
                throw new StorageException("Could not find group with name='" + groupName + "'");
            }
            long groupId = groupByName.getGroupId();
            return userGroupsDao.findAllUserNamesInGroup(groupId);
        } catch (SQLException e) {
            throw new StorageException("Could not find users for group with name='" + groupName + "'", e);
        }
    }

    @Override
    @Nullable
    public GroupInfo findGroup(String groupName) {
        try {
            Group group = userGroupsDao.findGroupByName(groupName);
            if (group != null) {
                return groupToGroupInfo(group);
            }
            return null;
        } catch (SQLException e) {
            throw new StorageException("Could not search for group with name='" + groupName + "'", e);
        }
    }

    @Override
    @Nullable
    public UserInfo findUserByProperty(String key, String val, boolean exactKeyMatch) {
        try {
            String encryptedVal =
                    shouldEncryptProperty(key) ? CryptoHelper.encryptIfNeeded(ArtifactoryHome.get(), val) : val;
            long userId = userPropertiesDao.getUserIdByProperty(key, encryptedVal);
            if (userId == 0L) {
                // If not found and the 'encryptedVal' is not an 'encrypted' version of 'val', try to find the original 'val'.
                if (!encryptedVal.equals(val)) {
                    userId = userPropertiesDao.getUserIdByProperty(key, val);
                    return userId != 0L ? userToUserInfo(userGroupsDao.findUserById(userId)) : null;
                }
                return null;
            } else {
                return userToUserInfo(userGroupsDao.findUserById(userId));
            }
        } catch (SQLException e) {
            throw new StorageException("Could not search for user with property " + key + ":" + val, e);
        }
    }

    @Override
    @Nullable
    public String findUserProperty(String username, String key) {
        try {
            String property = userPropertiesDao.getUserProperty(username, key);
            if (StringUtils.isNotBlank(property)) {
                return CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), property);
            }
            return property;
        } catch (SQLException e) {
            throw new StorageException(String.format("Could not search for datum %s of user: '%s'", key, username), e);
        }
    }

    @Override
    public boolean addUserProperty(String username, String key, String val) {
        try {
            return userPropertiesDao.addUserPropertyByUserName(username, key, val);
        } catch (SQLException e) {
            throw new StorageException(
                    String.format("Could not add external data %s:%s to user: '%s'", key, val, username), e);
        }
    }

    @Override
    public boolean deleteUserProperty(String username, String key) {
        try {
            return userPropertiesDao.deleteProperty(userGroupsDao.findUserIdByUsername(username), key);
        } catch (SQLException e) {
            throw new StorageException(
                    String.format("Could not delete external data %s from user: '%s'", key, username), e);
        }
    }

    @Override
    public Properties findPropertiesForUser(String username) {
        try {
            List<UserProperty> userProperties = userPropertiesDao.getPropertiesForUser(username);
            PropertiesImpl properties = new PropertiesImpl();
            for (UserProperty userProperty : userProperties) {
                properties.put(userProperty.getPropKey(),
                        CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), userProperty.getPropValue()));
            }
            return properties;
        } catch (SQLException e) {
            throw new StorageException(String.format("Failed to load properties for user: '%s'", username), e);
        }
    }

    @Override
    public void deletePropertyFromAllUsers(String propertyKey) {
        try {
            userPropertiesDao.deletePropertyFromAllUsers(propertyKey);
        } catch (SQLException e) {
            throw new StorageException("Could not delete property by key" + propertyKey + " from all users");
        }

    }

    /**
     * Locks user upon incorrect login attempt
     *
     * @throws StorageException
     */
    @Override
    public void lockUser(@Nonnull String username) {
        User user;
        try {
            user = getUser(username);
            synchronized (lockedUsersCache) {
                // despite we use concurrency ready cache,
                // we lock it externally in sake of db/cache
                // synchronisation
                if (user != null)
                // we want to block non-existing users as well
                {
                    userGroupsDao.lockUser(user);
                }
                registerLockedOutUser(username);
            }
        } catch (SQLException e) {
            log.debug("Could not lock user: '{}', cause: {}", username, e);
            throw new StorageException(
                    String.format("Could not lock user: '%s', because %s", username, e.getMessage())
            );
        }
    }

    /**
     * Unlocks locked in user
     *
     * @throws StorageException
     */
    @Override
    public void unlockUser(@Nonnull String userName) {
        User user;
        try {
            user = getUser(userName);
            synchronized (lockedUsersCache) {
                // despite we use concurrency ready cache,
                // we lock it externally in sake of db/cache
                // synchronisation
                if (user != null) {
                    userGroupsDao.unlockUser(user);
                }
                unRegisterLockedOutUser(userName);
            }
            //resetIncorrectLoginAttempts(userName);
        } catch (SQLException e) {
            log.debug("Could not unlock user: '{}', cause: {}", userName, e);
            throw new StorageException(
                    String.format("Could not unlock user: '%s', because %s", userName, e.getMessage())
            );
        }
    }

    /**
     * Unlocks all locked out users
     */
    @Nonnull
    @Override
    public void unlockAllUsers() {
        try {
            synchronized (lockedUsersCache) {
                // despite we use concurrency ready cache,
                // we lock it externally in sake of db/cache
                // synchronisation
                userGroupsDao.unlockAllUsers();
                lockedUsersCache.invalidateAll();
            }
            synchronized (incorrectLoginAttemptsCache) {
                incorrectLoginAttemptsCache.clear();
            }
        } catch (SQLException e) {
            log.debug("Could not unlock all users, cause: {}", e);
            throw new StorageException(
                    "Could not unlock all users, because " + e.getMessage()
            );
        }
    }

    /**
     * Unlocks all locked out admin users
     */
    @Override
    public void unlockAdminUsers() {
        try {
            synchronized (lockedUsersCache) {
                // despite we use concurrency ready cache,
                // we lock it externally in sake of db/cache
                // synchronisation
                userGroupsDao.unlockAdminUsers();
                getAllUsers(true, false).stream()
                        .filter(UserInfo::isEffectiveAdmin)
                        .forEach(u -> {
                                    unRegisterLockedOutUser(u.getUsername());
                                    //resetIncorrectLoginAttempts(u.getUsername());
                                }
                        );
            }
        } catch (SQLException e) {
            log.debug("Could not unlock all admin users, cause: {}", e);
            throw new StorageException(
                    "Could not unlock all admin users, because " + e.getMessage()
            );
        }
    }

    /**
     * @return Collection of locked out users
     */
    @Override
    public Set<String> getLockedUsers() {
        try {
            Set<String> lockedUsers = userGroupsDao.getLockedUsersNames();
            lockedUsers.addAll(lockedUsersCache.asMap().keySet());
            return lockedUsers;
        } catch (SQLException e) {
            log.debug("Could not list locked in users, cause: {}", e);
            throw new StorageException(
                    "Could not list locked in users, because " + e.getMessage()
            );
        }
    }

    /**
     * Checks whether given user is locked
     * <p>
     * note: this method using caching in sake
     * of DB load preventing
     *
     * @return boolean
     */
    @Override
    public boolean isUserLocked(String userName) {
        if (shouldCacheLockedUsers()) {
            if (lockedUsersCache.getIfPresent(userName) != null) {
                return true;
            }
            User user = getUser(userName);
            if (user != null && user.isLocked()) {
                registerLockedOutUser(user.getUsername());
                return user.isLocked();
            }
        } else {
            User user = getUser(userName);
            if (user != null) {
                return user.isLocked();
            }
        }
        return false;
    }

    /**
     * Changes user password
     *
     * @return sucess/failure
     *
     * @throws StorageException if persisting password fails
     */
    @Override
    public void changePassword(UserInfo user, SaltedPassword newSaltedPassword, String rawPassword) {
        try {
            if (user == null) {
                throw new UsernameNotFoundException("The user is not exist");
            }
            if (!user.isUpdatableProfile()) {
                throw new PasswordChangeException("The specified user is not permitted to reset his password.");
            }
            userGroupsDao.changePassword(user.getUsername(), newSaltedPassword);
            userGroupsDao.unexpirePassword(user.getUsername());
        } catch (SQLException e) {
            throw new StorageException(
                    "Changing password for \"" + user.getUsername() + "\" has failed, " + e.getMessage(), e);
        }
    }

    /**
     * Makes user password expired
     */
    @Override
    public void expireUserPassword(String userName) {
        try {
            if (getUser(userName) != null) {
                userGroupsDao.expireUserPassword(userName);
                return;
            }
            throw new UsernameNotFoundException("User " + userName + " is not exist");
        } catch (SQLException e) {
            throw new StorageException(
                    String.format("Expiring password for user: '%s' has failed, %s", userName, e.getMessage()),
                    e);
        }
    }

    /**
     * Makes user password not expired
     */
    @Override
    public void revalidatePassword(String userName) {
        try {
            if (getUser(userName) != null) {
                userGroupsDao.unexpirePassword(userName);
                return;
            }
            throw new UsernameNotFoundException(String.format("The user: '%s' is not exist", userName));
        } catch (SQLException e) {
            throw new StorageException(
                    String.format("Expiring user password for user: '%s' has failed, %s", userName, e.getMessage()),
                    e);
        }
    }

    /**
     * Makes all users passwords expired
     */
    @Override
    public void expirePasswordForAllUsers() {
        try {
            userGroupsDao.expirePasswordForAllUsers();
        } catch (SQLException e) {
            throw new StorageException("Expiring passwords for all users has failed, " + e.getMessage(), e);
        }
    }

    /**
     * Makes all users passwords not expired
     */
    @Override
    public void revalidatePasswordForAllUsers() {
        try {
            userGroupsDao.unexpirePasswordForAllUsers();
        } catch (SQLException e) {
            throw new StorageException("UnExpiring passwords for all users has failed, " + e.getMessage(), e);
        }
    }

    /**
     * @return whether locked out users should be cached
     */
    private boolean shouldCacheLockedUsers() {
        return CACHE_BLOCKED_USERS;
    }

    /**
     * Registers locked out user in cache
     */
    private void registerLockedOutUser(String userName) {
        if (shouldCacheLockedUsers()) {
            lockedUsersCache.put(userName, System.currentTimeMillis());
        }
    }

    /**
     * Unregisters locked out user/s from cache
     *
     * @param user a user name to unlock or all users via ALL_USERS
     *             {@see UserGroupServiceImpl.ALL_USERS}
     */
    private void unRegisterLockedOutUser(String user) {
        if (shouldCacheLockedUsers()) {
            lockedUsersCache.invalidate(user);
        }
    }

    private GroupInfo groupToGroupInfo(Group group) {
        MutableGroupInfo result = InfoFactoryHolder.get().createGroup(group.getGroupName());
        result.setDescription(group.getDescription());
        result.setNewUserDefault(group.isNewUserDefault());
        result.setRealm(group.getRealm());
        result.setRealmAttributes(group.getRealmAttributes());
        result.setAdminPrivileges(group.isAdminPrivileges());
        return result;
    }

    private Group groupInfoToGroup(long groupId, GroupInfo groupInfo) {
        return new Group(groupId, groupInfo.getGroupName(), groupInfo.getDescription(),
                groupInfo.isNewUserDefault(), groupInfo.getRealm(), groupInfo.getRealmAttributes(), groupInfo.isAdminPrivileges());
    }

    private UserInfo userToUserInfo(User user) throws SQLException {
        return userToUserInfoWithProperties(user, null);
    }

    private UserInfo userToUserInfoWithProperties(User user, Set<UserPropertyInfo> userProperties) throws SQLException {
        UserInfoBuilder builder = new UserInfoBuilder(user.getUsername());
        Set<UserGroupInfo> groups = new HashSet<>(user.getGroups().size());
        boolean groupAdmin = false;
        for (UserGroup userGroup : user.getGroups()) {
            Group groupById = userGroupsDao.findGroupById(userGroup.getGroupId());
            if (groupById != null) {
                String groupname = groupById.getGroupName();
                if (groupById.isAdminPrivileges()) {
                    groupAdmin = true;
                }
                groups.add(InfoFactoryHolder.get().createUserGroup(groupname, userGroup.getRealm()));
            } else {
                log.error(String.format("Group ID %d does not exists! Skipping add group for user: '%s'",
                        userGroup.getGroupId(), user.getUsername()));
            }
        }
        builder.password(new SaltedPassword(user.getPassword(), user.getSalt())).email(user.getEmail())
                .admin(user.isAdmin()).enabled(user.isEnabled()).updatableProfile(user.isUpdatableProfile())
                .groups(groups);
        MutableUserInfo userInfo = builder.build();
        userInfo.setTransientUser(false);
        userInfo.setGenPasswordKey(user.getGenPasswordKey());
        userInfo.setRealm(user.getRealm());
        userInfo.setPrivateKey(user.getPrivateKey());
        userInfo.setPublicKey(user.getPublicKey());
        userInfo.setLastLoginTimeMillis(user.getLastLoginTimeMillis());
        userInfo.setLastLoginClientIp(user.getLastLoginClientIp());
        userInfo.setBintrayAuth(user.getBintrayAuth());
        userInfo.setLocked(user.isLocked());
        userInfo.setCredentialsExpired(user.isCredentialsExpired());
        userInfo.setCredentialsNonExpired(!user.isCredentialsExpired());
        userInfo.setGroupAdmin(groupAdmin);
        if (userProperties != null && !userProperties.isEmpty()) {
            userProperties.forEach(prop -> userInfo.putUserProperty(prop.getPropKey(),
                    CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), prop.getPropValue())));
        }
        return userInfo;
    }

    private User userInfoToUser(long userId, UserInfo userInfo) throws SQLException {
        User u = new User(userId, userInfo.getUsername(), userInfo.getPassword(), userInfo.getSalt(),
                userInfo.getEmail(), userInfo.getGenPasswordKey(),
                userInfo.isAdmin(), userInfo.isEnabled(), userInfo.isUpdatableProfile(), userInfo.getRealm(),
                userInfo.getPrivateKey(),
                userInfo.getPublicKey(), userInfo.getLastLoginTimeMillis(), userInfo.getLastLoginClientIp(),
                userInfo.getBintrayAuth(), userInfo.isLocked(), userInfo.isCredentialsExpired());
        Set<UserGroupInfo> groups = userInfo.getGroups();
        Set<UserGroup> userGroups = new HashSet<>(groups.size());
        for (UserGroupInfo groupInfo : groups) {
            Group groupByName = userGroupsDao.findGroupByName(groupInfo.getGroupName());
            if (groupByName != null) {
                userGroups.add(new UserGroup(u.getUserId(), groupByName.getGroupId(), groupInfo.getRealm()));
            } else {
                log.error(String.format("Group: '%s' does not exists! Skipping add group for user: '%s'",
                        groupInfo.getGroupName(), userInfo.getUsername()));
            }
        }
        u.setGroups(userGroups);
        return u;
    }

    @Override
    public Set<PasswordExpiryUser> getUsersWhichPasswordIsAboutToExpire(int daysToNotifyBefore,
            int daysToKeepPassword) {
        //User error = notification day > expiration day will cause mail to never be sent. Just send a day before
        if (daysToKeepPassword < daysToNotifyBefore) {
            if (daysToKeepPassword >= 2) {
                daysToNotifyBefore = daysToKeepPassword - 1;
            } else {
                daysToNotifyBefore = 1;
            }
        }
        long millisDaysToNotifyBefore = daysToNotifyBefore * MILLIS_IN_DAY;
        long millisDaysToKeepPassword = daysToKeepPassword * MILLIS_IN_DAY;
        long now = DateTime.now().getMillis();
        try {
            return userGroupsDao.getPasswordExpiredUsersByFilter(
                    (Long creationTime) ->
                            shouldNotifyUser(creationTime, now, millisDaysToKeepPassword, millisDaysToNotifyBefore));
        } catch (SQLException e) {
            log.debug("Could not fetch users with passwords that about to expire, cause: {}", e);
            throw new StorageException("Could not fetch users with passwords that about to expire");
        }
    }


    @Override
    public List<String> markUsersCredentialsExpired(int daysToKeepPassword) {
        long millisDaysToKeepPassword = daysToKeepPassword * MILLIS_IN_DAY;
        long now = DateTime.now().getMillis();
        Set<PasswordExpiryUser> usersToExpire;
        try {
            usersToExpire = userGroupsDao.getPasswordExpiredUsersByFilter(
                    (Long creationTime) -> passwordExpired(creationTime, now, millisDaysToKeepPassword));
            boolean hadErrors = markExpiryByBatch(usersToExpire);
            if (hadErrors) {
                log.debug("Could not mark credentials expired.");
                throw new StorageException("Could not mark credentials expired, see logs for more details");
            }
        } catch (SQLException e) {
            log.debug("Could not mark credentials expired, cause: {}", e);
            throw new StorageException("Could not mark credentials expired, see logs for more details", e);
        }
        return usersToExpire.stream()
                .map(PasswordExpiryUser::getUserName)
                .collect(Collectors.toList());
    }

    private boolean markExpiryByBatch(Set<PasswordExpiryUser> expiredPasswordsUsers) throws SQLException {
        throw new NotImplementedException("Implemented in access");
    }

    @Override
    public void expirePasswordForUserIds(Set<Long> userIds) throws SQLException {
        userGroupsDao.markCredentialsExpired(userIds.toArray(new Long[userIds.size()]));
    }

    /**
     * Checks whether user password is expired
     *
     * @param userName  a user name
     * @param expiresIn days for password to stay valid after creation
     * @return boolean
     */
    @Override
    public boolean isUserPasswordExpired(String userName, int expiresIn) {
        boolean isExpired;

        String passwordCreatedProperty = findUserProperty(userName, "passwordCreated");
        DateTime created;
        if (StringUtils.isBlank(passwordCreatedProperty)) {
            log.debug("Password creation for user: '{}' was not found, initiating default value (now)", userName);
            created = DateTime.now();
            addUserProperty(userName, "passwordCreated", Long.toString(created.getMillis()));
        } else {
            created = new DateTime(Long.valueOf(passwordCreatedProperty));
        }

        log.debug("Password creation for user: '{}' is {}, password expires after {}", userName, created, expiresIn);
        if (isExpired = created.plusDays(expiresIn).isBeforeNow()) {
            log.debug("The user: '{}' credentials have expired (updating profile accordingly)", userName);
            expireUserPassword(userName);
        }

        // todo: [mp] set user.credentialsExpired=True if expired indeed

        return isExpired;
    }

    /**
     * @return the date when last password was created
     *
     * @throws SQLException
     */
    @Override
    public Long getUserPasswordCreationTime(String userName) {
        try {
            return userGroupsDao.getUserPasswordCreationTime(userName);
        } catch (SQLException e) {
            log.debug("Could not check when credentials were created, cause: {}", e);
            throw new StorageException("Could not check when credentials were created, see logs for more details");
        }
    }

    /**
     * Get a map of user names and whether they are admins or not
     * For performance considerations(mostly DB), we've separated this to 2 different queries
     */
    @Override
    public Map<String, Boolean> getAllUsersAndAdminStatus(boolean justAdmins) {
        try {
            Map<String, Boolean> usernameAndAdminStatus = Maps.newHashMap();
            List<String> allAdminUserNames = userGroupsDao.getAllAdminUserNames();
            allAdminUserNames.forEach(adminUser->usernameAndAdminStatus.put(adminUser, true));
            if (!justAdmins) {
                List<String> allNonAdminUserNames = userGroupsDao.getAllNonAdminUserNames();
                allNonAdminUserNames.forEach(user -> usernameAndAdminStatus.putIfAbsent(user, false));
            }
            return usernameAndAdminStatus;
        } catch (SQLException e) {
            log.debug("Could not get all users names admin info, cause: {}", e);
            throw new StorageException("Could not get all user information, see logs for more details");
        }
    }

    @Override
    public List<String> getAllAdminGroupsNames() {
        try {
            return userGroupsDao.findAllAdminGroups();
        } catch (SQLException e) {
            throw new StorageException("Could not get all admin groups, cause: {}", e);
        }
    }

    @Override
    public ImmutableSetMultimap<String, String> getAllUsersInGroups() {
        throw new NotImplementedException("Replaced with AccessUserGroupStoreService");
    }

    private boolean passwordExpired(long creationTime, long millisNow, long millisDaysToKeepPassword) {
        return millisNow > (creationTime + millisDaysToKeepPassword);
    }

    private boolean shouldNotifyUser(long creationTime, long millisNow, long millisDaysToKeepPassword,
            long millisDaysToNotifyBefore) {
        // now is after the (expiration - notification days) == inside the notification window
        return (millisNow >= ((creationTime + millisDaysToKeepPassword) - millisDaysToNotifyBefore))
                // now is not pass expiration notification day + 1 day (end of notification window)
                &&
                (millisNow <= ((creationTime + millisDaysToKeepPassword) - millisDaysToNotifyBefore) + MILLIS_IN_DAY);
    }

    private boolean shouldEncryptProperty(String key) {
        Map<String, PropsTokenManager> beans = ContextHelper.get().beansForType(PropsTokenManager.class);
        if (beans != null) {
            return beans.values().stream()
                    .anyMatch(manager -> StringUtils.equals(manager.getPropKey(), key));
        }
        return false;

    }

    @Override
    public void init() {
        // nop
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {
        //nop
    }

    @Override
    public void destroy() {
        // nop
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        // nop
    }
}