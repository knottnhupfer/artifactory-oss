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

package org.artifactory.storage.db.security.service.access;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSetMultimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.artifactory.api.security.PasswordExpiryUser;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.model.xstream.security.UserImpl;
import org.artifactory.sapi.security.SecurityConstants;
import org.artifactory.security.*;
import org.artifactory.security.access.AccessService;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.access.client.AccessClient;
import org.jfrog.access.client.AccessClientHttpException;
import org.jfrog.access.client.user.FindUsersRequest;
import org.jfrog.access.client.user.UsersClient;
import org.jfrog.access.model.UserStatus;
import org.jfrog.access.rest.group.Group;
import org.jfrog.access.rest.group.Groups;
import org.jfrog.access.rest.group.ManageGroupMembersRequest;
import org.jfrog.access.rest.user.UpdateUserRequest;
import org.jfrog.access.rest.user.User;
import org.jfrog.access.rest.user.UserBase;
import org.jfrog.access.rest.user.UserRequest;
import org.jfrog.common.ThrowingFunction;
import org.jfrog.common.config.diff.DataDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.artifactory.storage.db.security.service.access.UserMapper.ArtifactoryBuiltInUserProperty.artifactory_admin;
import static org.artifactory.storage.db.security.service.access.UserPropertiesSearchHelper.addSearchablePropIfNeeded;
import static org.artifactory.storage.db.security.service.access.UserPropertiesSearchHelper.deleteSearchablePropIfNeeded;

/**
 * Implementation of {@link UserGroupStoreService} that user Access as the storage engine.
 *
 * @author Yossi Shaul
 */
@Service
@Reloadable(beanClass = UserGroupStoreService.class,
        initAfter = AccessService.class,
        listenOn = CentralConfigKey.none)
public class AccessUserGroupStoreService implements UserGroupStoreService {
    private static final Logger log = LoggerFactory.getLogger(AccessUserGroupStoreService.class);

    @Autowired
    private AccessService accessService;

    public static final String REMEMBER_ME_SCOPE = "authentication:remember-me";

    public enum GroupFilter {
        ALL(in -> true),
        ADMIN(GroupInfo::isAdminPrivileges),
        DEFAULTS(GroupInfo::isNewUserDefault),
        EXTERNAL(gi -> !SecurityConstants.DEFAULT_REALM.equals(gi.getRealm())),
        INTERNAL(gi -> gi.getRealm() == null || gi.getRealm().equals(SecurityConstants.DEFAULT_REALM));

        public final Predicate<GroupInfo> filterFunction;

        GroupFilter(Predicate<GroupInfo> filterFunction) {
            this.filterFunction = filterFunction;
        }
    }

    @Override
    public boolean createUser(UserInfo user) {
        return createUserWithProperties(user, false);
    }

    @Override
    public boolean createUserWithProperties(UserInfo user, boolean addUserProperties) {
        if (userExists(user.getUsername())) {
            return false;
        }
        // create a copy of the user
        user = copyUser(user);
        UserRequest accessUser = UserMapper.toAccessUser(user, addUserProperties);
        getClient().users().createUser(accessUser);
        return true;
    }

    @Override
    public void updateUser(MutableUserInfo userInfo) {
        if (!Strings.isNullOrEmpty(userInfo.getPassword())) {
            getClient().token().revokeAllForUserAndScope(userInfo.getUsername(), REMEMBER_ME_SCOPE);
        }
        getClient().users().updateUser(UserMapper.toUpdateUserRequest(copyUser(userInfo), true));
    }

    private UserImpl copyUser(UserInfo user) {
        return new UserImpl(user);
    }

    @Override
    public UserInfo findUser(String username) {
        return findUserInternal(username, true)
                .map(UserMapper::toArtifactoryUser)
                .map(UserPropertiesSearchHelper::decryptUserProperties)
                .orElse(null);
    }

    private Optional<? extends UserBase> findUserInternal(String username, boolean withGroups) {
        UsersClient usersClient = getClient().users();
        return accessService.ensureAuth(() -> withGroups ? usersClient.findUserWithGroupsByUsername(username) :
                usersClient.findUserByUsername(username));
    }

    @Override
    public void deleteAllGroupsAndUsers() {
        //TODO: [by YS] add api entry point in Access server unless it's only required for the import
        getAllGroups().forEach(g -> getClient().groups().deleteGroup(g.getGroupName()));
        getAllUsers(true, false).forEach(u -> getClient().users().deleteUser(u.getUsername()));
    }

    @Override
    public boolean adminUserExists() {
        return isNotEmpty(getClient().users().findUsersByCustomData(artifactory_admin.name(), "true", true).getUsers());
    }

    @Override
    public boolean userExists(String username) {
        return findUserInternal(username, false).isPresent();
    }

    @Override
    public void deleteUser(String username) {
        try {
            getClient().users().deleteUser(username);
        } catch (AccessClientHttpException e) {
            if (e.getStatusCode() != 404) {
                throw e;
            }
        }
    }

    @Nullable
    @Override
    public UserInfo findUserByProperty(String key, String val, boolean exactKeyMatch) {
        return UserPropertiesSearchHelper.findUserByProperty(getClient(), key, val, exactKeyMatch);
    }

    @Nullable
    @Override
    public String findUserProperty(String username, String key) {
        UserInfo user = findUser(username);
        if (user == null) {
            log.debug("The user: '{}' doesn't exist. Cannot find property", username);
            return null;
        }
        // no need to decrypt.
        return user.getUserProperty(key)
                .filter(StringUtils::isNotBlank)
                .map(v -> CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), v))
                .orElse(null);
    }

    @Override
    public boolean addUserProperty(String username, String key, String val) {
        if (!userExists(username)) {
            log.debug("The user: '{}' doesn't exist. Cannot add property", username);
            return false;
        }
        UpdateUserRequest updateUserRequest = UpdateUserRequest.create();
        updateUserRequest.username(username).addCustomData(key, val, true);
        addSearchablePropIfNeeded(updateUserRequest, key, val);
        getClient().users().updateUser(updateUserRequest);
        return true;
    }

    @Override
    public boolean deleteUserProperty(String username, String propKey) {
        if (!userExists(username)) {
            log.debug("User {} doesn't exist. Cannot delete property {}", username, propKey);
            return false;
        }

        UserInfo user = findUser(username);
        if (user == null) {
            log.debug("The user: '{}' doesn't exist. Cannot delete property {}", username, propKey);
            return false;
        }
        return deleteUserProperty(user, propKey);
    }

    private boolean deleteUserProperty(UserInfo user, String propKey) {
        boolean propertyExist = user.getUserProperty(propKey).isPresent();
        if (!propertyExist) {
            log.debug("The user: '{}' doesn't have the specified property: {}.", user.getUsername(), propKey);
            return false;
        }

        UpdateUserRequest updateUserRequest = UpdateUserRequest.create();
        updateUserRequest.username(user.getUsername()).addCustomData(propKey, null);
        deleteSearchablePropIfNeeded(updateUserRequest, propKey);
        getClient().users().updateUser(updateUserRequest);
        return true;
    }

    @Override
    public void deletePropertyFromAllUsers(String propertyKey) {
        getClient().users().findUsersByCustomData(propertyKey, true).getUsers().stream()
                .map(UserMapper::toArtifactoryUser)
                .forEach(u -> deleteUserProperty(u, propertyKey));
    }

    @Override
    public Properties findPropertiesForUser(String username) {
        UserInfo user = findUser(username);
        if (user == null) {
            log.debug("The user: '{}' not found. Returning empty properties", username);
            return new PropertiesImpl();
        }

        PropertiesImpl properties = new PropertiesImpl();
        for (UserPropertyInfo userProperty : user.getUserProperties()) {
            properties.put(userProperty.getPropKey(),
                    CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), userProperty.getPropValue()));
        }
        return properties;
    }

    @Override
    public List<UserInfo> getAllUsers(boolean includePasswordsAndEncrypted, boolean includePasswords) {
        return getAllUsers(includePasswordsAndEncrypted, includePasswords, true);
    }

    private List<UserInfo> getAllUsers(boolean includeAdmins, boolean secured, boolean withGroupAdmins) {
        Stream<UserInfo> userInfoStream = getAllUsersInternal(secured).stream()
                .map(UserMapper::toArtifactoryUser)
                .map(UserPropertiesSearchHelper::decryptUserProperties)
                .filter(userInfo -> includeAdmins || !userInfo.isAdmin());

        if (withGroupAdmins) {
            List<String> groupAdmins = getAllAdminGroupsNames();
            userInfoStream = userInfoStream.map(u -> {
                boolean groupAdmin = u.getGroups().stream()
                        .anyMatch(group -> groupAdmins.contains(group.getGroupName()));
                MutableUserInfo copyUser = InfoFactoryHolder.get().copyUser(u);
                copyUser.setGroupAdmin(groupAdmin);
                return copyUser;
            });
        }

        return userInfoStream.collect(Collectors.toList());
    }

    @Override
    public Map<String, Boolean> getAllUsersAndAdminStatus(boolean justAdmins) {
        return getAllUsers(true, false, true).stream()
                .filter(u -> !justAdmins || u.isEffectiveAdmin())
                .collect(Collectors.toMap(UserInfo::getUsername, UserInfo::isEffectiveAdmin));
    }

    @Override
    public List<String> getAllAdminGroupsNames() {
        return getGroupsByFilter(GroupFilter.ADMIN).stream().map(GroupInfo::getGroupName).collect(Collectors.toList());
    }

    private List<User> getAllUsersInternal(boolean secured) {
        if (!secured) {
            return getClient().users().findUsers().getUsers();
        }

        return getClient().users().findUsers(new FindUsersRequest()
                .expand(UserRequest.Expand.passwords)
                .expand(UserRequest.Expand.encryptedData)
        ).getUsers();
    }

    @Override
    public ImmutableSetMultimap<String, String> getAllUsersInGroups() {
        ImmutableSetMultimap.Builder<String, String> builder = ImmutableSetMultimap.builder();
        getAllUsers(true, false).forEach(userInfo -> builder.putAll(
                userInfo.getUsername(),
                userInfo.getGroups()
                        .stream()
                        .map(UserGroupInfo::getGroupName)
                        .collect(Collectors.toList()))
        );

        return builder.build();
    }

    @Override
    public void updateUserLastLogin(String username, long lastLoginTimeMillis, String lastLoginIp) {
        getClient().users().replaceUserLastLogin(username, lastLoginTimeMillis, lastLoginIp);
    }

    @Override
    public void lockUser(@Nonnull String username) {
        handleLockException(ignore ->
                patchUser(createUpdateUserRequest(username, request -> request.status(UserStatus.LOCKED))), username, "lock");
    }

    @Override
    public void unlockUser(@Nonnull String username) {
        handleLockException(ignore ->
                patchUser(createUpdateUserRequest(username, (request) -> request.status(UserStatus.ENABLED))), username, "unlock");
    }

    private void handleLockException(ThrowingFunction<Void, User, Exception> execute, String username, String operation) {
        try {
            execute.apply(null);
        } catch (AccessClientHttpException e) {
            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                log.debug("The user: '{}' to {} not found. Ignoring", username, operation);
                return;
            }
            throwStorageException(username, operation, e);
        } catch (Exception e) {
            throwStorageException(username, operation, e);
        }
    }

    private void throwStorageException(String username, String operation, Exception e) {
        log.debug("Could not {} user: '{}', cause: {}", operation, username, e);
        throw new StorageException(String.format("Could not lock user: '%s', reason: %s", username, e.getMessage()));
    }

    @Override
    public boolean isUserLocked(String userName) {
        return findUserInternal(userName, false)
                .map(UserBase::getStatus)
                .map(UserStatus.LOCKED::equals)
                .orElse(false);
    }

    private AccessClient getClient() {
        return accessService.getAccessClient();
    }

    @Override
    public void unlockAllUsers() {
        getLockedUsers().forEach(this::unlockUser);
    }

    @Override
    public void unlockAdminUsers() {
        getAllUsersInternal(false)
                .stream()
                .filter(user -> UserStatus.LOCKED.equals(user.getStatus()))
                .map(UserMapper::toArtifactoryUser)
                .filter(UserInfo::isAdmin)
                .map(UserInfo::getUsername)
                .forEach(this::unlockUser);
    }

    @Override
    public Set<String> getLockedUsers() {
        return getAllUsersInternal(false)
                .stream()
                .filter(user -> UserStatus.LOCKED.equals(user.getStatus()))
                .map(User::getUsername)
                .collect(Collectors.toSet());
    }

    private User patchUser(UpdateUserRequest userRequest) {
        return getClient().users().updateUser(userRequest);
    }

    private UpdateUserRequest createUpdateUserRequest(String username,
            Function<UpdateUserRequest, UserRequest> updates) {
        return (UpdateUserRequest) updates.apply((UpdateUserRequest) UpdateUserRequest.create().username(username));
    }

    @Override
    public void changePassword(UserInfo user, SaltedPassword newSaltedPassword, String rawPassword) {
        patchUser(createUpdateUserRequest(user.getUsername(),
                (request) -> request.password(rawPassword)));
    }

    @Override
    public boolean isUserPasswordExpired(String userName, int expiresIn) {
        return Optional.ofNullable(findUser(userName)).map(UserInfo::isCredentialsExpired).orElse(false);
    }

    @Override
    public void expireUserPassword(String userName) {
        patchUser(createUpdateUserRequest(userName,
                (request) -> request.passwordExpired(true)));
    }

    @Override
    public void revalidatePassword(String userName) {
        patchUser(createUpdateUserRequest(userName,
                (request) -> request.passwordExpired(false)));
    }

    @Override
    public void expirePasswordForAllUsers() {
        getAllUsers(true, false).stream().map(UserInfo::getUsername).forEach(this::expireUserPassword);
    }

    @Override
    public void revalidatePasswordForAllUsers() {
        getAllUsers(true, false).stream().map(UserInfo::getUsername).forEach(this::revalidatePassword);
    }

    @Override
    public List<String> markUsersCredentialsExpired(int daysToKeepPassword) {
        // [NS] Irrelevant, access manages expiry of users
        return Collections.emptyList();
    }

    @Override
    public void expirePasswordForUserIds(Set<Long> userIds) throws SQLException {
        // [NS] Irrelevant, access manages different ids
    }

    @Override
    public Long getUserPasswordCreationTime(String userName) {
        return findUserInternal(userName, false).map(UserBase::getPasswordLastModified).orElse(0L);
    }

    @Override
    public Set<PasswordExpiryUser> getUsersWhichPasswordIsAboutToExpire(int daysToNotifyBefore,
            int daysToKeepPassword) {
        return accessService.getAccessClient().users().findUsers(new FindUsersRequest().daysToExpire(daysToNotifyBefore))
                .getUsers().stream()
                .map(user -> Pair.of(user, UserMapper.toArtifactoryUser(user)))
                .filter(users -> SecurityConstants.DEFAULT_REALM.equals(users.getRight().getRealm()))
                .map(users -> new PasswordExpiryUser(
                        users.getLeft().getUsername(), users.getLeft().getEmail(), users.getLeft().getPasswordLastModified()))
                .collect(Collectors.toSet());
    }

    @Nullable
    @Override
    public GroupInfo findGroup(String groupName) {
        Optional<Group> group = getClient().groups().findGroupByName(groupName);
        return group.map(GroupMapper::toArtifactoryGroup).orElse(null);
    }

    @Override
    public boolean createGroup(GroupInfo groupInfo) {
        try {
            getClient().groups().createGroup(GroupMapper.toAccessGroup(groupInfo));
            return true;
        } catch (AccessClientHttpException e) {
            log.debug("Create group failed: {}", e.getStatusCode(), e);
            if (e.getStatusCode() == 404 || e.getStatusCode() == 409) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public void addUsersToGroup(String groupName, List<String> usernames) {
        accessService.getAccessClient().groups().manageGroupUsers(groupName,
                new ManageGroupMembersRequest().addUsers(usernames.toArray(new String[]{})));
    }

    @Override
    public void removeUsersFromGroup(String groupName, List<String> usernames) {
        accessService.getAccessClient().groups().manageGroupUsers(groupName,
                new ManageGroupMembersRequest().removeUsers(usernames.toArray(new String[]{})));

    }

    @Override
    public List<String> findUsersInGroup(String groupName) {
        return accessService.getAccessClient()
                .groups()
                .findGroupUsers(groupName).getUsers()
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteGroup(String groupName) {
        try {
            getClient().groups().deleteGroup(groupName);
            return true;
        } catch (AccessClientHttpException e) {
            log.debug("Delete group failed: {}", e.getStatusCode(), e);
            if (e.getStatusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public List<GroupInfo> getAllGroups() {
        Groups groups = getClient().groups().findGroups();
        return groups.getGroups().stream().map(GroupMapper::toArtifactoryGroup).collect(Collectors.toList());
    }

    @Override
    public Map<String, GroupInfo> getAllGroupsByNames(List<String> groupNames) {
        return getClient()
                .groups()
                .findGroupsByNames(groupNames)
                .getGroups()
                .stream()
                .collect(Collectors.toMap(Group::getName, GroupMapper::toArtifactoryGroup));
    }

    private List<GroupInfo> getGroupsByFilter(GroupFilter filter) {
        return getAllGroups().stream().filter(filter.filterFunction).collect(Collectors.toList());
    }

    @Override
    public List<GroupInfo> getNewUserDefaultGroups() {
        return getGroupsByFilter(GroupFilter.DEFAULTS);
    }

    @Override
    public List<GroupInfo> getAllExternalGroups() {
        return getGroupsByFilter(GroupFilter.EXTERNAL);
    }

    @Override
    public List<GroupInfo> getInternalGroups() {
        return getGroupsByFilter(GroupFilter.INTERNAL);
    }

    @Override
    public Set<String> getNewUserDefaultGroupsNames() {
        return getNewUserDefaultGroups().stream().map(GroupInfo::getGroupName).collect(Collectors.toSet());
    }

    @Override
    public boolean updateGroup(MutableGroupInfo groupInfo) {
        try {
            getClient().groups().updateGroup(GroupMapper.toUpdateAccessGroup(groupInfo));
            return true;
        } catch (AccessClientHttpException e) {
            if (e.getStatusCode() != 404) {
                log.debug("Update group failed: {}", e.getStatusCode(), e);
            }
        }
        return false;
    }

    @Override
    public void init() {
        if (accessService.getAccessClient() == null) {
            throw new IllegalStateException("Access client cannot be null at this point");
        }
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        // nop
    }
}
