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

package org.artifactory.storage.db.security.service.access.emigrate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;
import org.artifactory.api.security.UserInfoBuilder;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.model.xstream.security.UserProperty;
import org.artifactory.security.*;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.CheckedSupplier;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.client.util.PathUtils;
import org.jfrog.storage.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Noam Shemesh
 */
@Component
public class SecurityEmigratorFetchers extends BaseDao {
    private static final Logger log = LoggerFactory.getLogger(SecurityEmigratorFetchers.class);

    @Autowired
    public SecurityEmigratorFetchers(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public List<UserInfo> getAllUserInfos() {
        Map<Long, Set<UserPropertyInfo>> allUserProperties = wrapSqlException("users properties", this::getAllUsersProperties);
        return wrapSqlException("users", this::getAllUsers).stream().map(
                user -> wrapSqlException("group", () -> Converters.userToUserInfoWithProperties(
                        this,
                        user,
                        allUserProperties.get(user.userId)
                ))).collect(Collectors.toList());
    }

    public List<GroupInfo> getAllGroupInfos() {
        return wrapSqlException("groups", this::findAllGroups).stream()
                .map(Converters::groupToGroupInfo)
                .collect(Collectors.toList());
    }

    public List<RepoAcl> getAllAclInfos() {
        Map<Long, RepoPermissionTarget> targetMap =
                wrapSqlException("permission targets", this::getAllPermissionTargets)
                        .entrySet().stream()
                        .map(entry -> Pair.of(entry.getKey(), Converters.permissionTargetToInfo(entry.getValue())))
                        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        return getAllAclsInfo(targetMap);
    }

    private List<RepoAcl> getAllAclsInfo(Map<Long, RepoPermissionTarget> targetMap) {
        Map<Long, String> groups = wrapSqlException("groups", this::getAllGroupNamePerIds);
        Map<Long, String> users = wrapSqlException("users", this::getAllUsernamePerIds);
        return wrapSqlException("acls", this::getAllAcls).stream()
                .map(acl -> Converters.aclToInfo(
                        groups,
                        users,
                        targetMap,
                        acl))
                .collect(Collectors.toList());
    }

    private Collection<User> getAllUsers() throws SQLException {
        Map<Long, User> results = new HashMap<>();
        Map<Long, Set<UserGroup>> groups = new HashMap<>();
        String query = "SELECT * FROM users";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query)){
            while (resultSet.next()) {
                User user = Converters.user(resultSet);
                results.put(user.userId, user);
                groups.put(user.userId, new HashSet<>());
            }
        }
        try (ResultSet resultSet = jdbcHelper.executeSelect("SELECT * FROM users_groups")){
            while (resultSet.next()) {
                UserGroup userGroup = Converters.userGroup(resultSet);
                Set<UserGroup> userGroups = groups.get(userGroup.userId);
                // Group not found due to admin filtering
                if (userGroups != null) {
                    userGroups.add(userGroup);
                }
            }
        }
        for (Map.Entry<Long, Set<UserGroup>> entry : groups.entrySet()) {
            User user = results.get(entry.getKey());
            if (user == null) {
                throw new IllegalStateException("Map population of users and groups failed!");
            } else {
                user.groups = ImmutableSet.copyOf(entry.getValue());
            }
        }
        return results.values();
    }

    private Map<Long, Set<UserPropertyInfo>> getAllUsersProperties() throws SQLException {
        ResultSet rs = null;
        Set<UserPropertyInfo> results = null;
        Map<Long, Set<UserPropertyInfo>> userPropertyMap = Maps.newHashMap();
        try {
            String sel = "SELECT user_id,prop_key,prop_value FROM user_props order by user_id ";
            rs = jdbcHelper.executeSelect(sel);
            while (rs.next()) {
                Long userId = rs.getLong(1);
                if (userPropertyMap.get(userId) == null) {
                    results = Sets.newHashSet();
                    results.add(Converters.propertyFromData(rs));
                    userPropertyMap.put(userId, results);
                } else {
                    results.add(Converters.propertyFromData(rs));
                }
            }
            return userPropertyMap;
        } finally {
            DbUtils.close(rs);
        }
    }

    private Group findGroupById(long groupId) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM groups WHERE group_id = ?", groupId);
            if (resultSet.next()) {
                return Converters.group(resultSet);
            }
            return null;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    private List<Group> findAllGroups() throws SQLException {
        List<Group> results = new ArrayList<>();
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM groups");
            while (resultSet.next()) {
                results.add(Converters.group(resultSet));
            }
            return results;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    private Collection<Acl> getAllAcls() throws SQLException {
        ResultSet resultSet = null;
        Map<Long, Acl> aclsMap = new HashMap<>();
        Map<Long, Set<Ace>> acesMap = new HashMap<>();
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM acls");
            while (resultSet.next()) {
                Acl acl = Converters.acl(resultSet);
                aclsMap.put(acl.aclId, acl);
                acesMap.put(acl.aclId, new HashSet<Ace>(3));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM aces");
            while (resultSet.next()) {
                Ace ace = Converters.ace(resultSet);
                Set<Ace> aces = acesMap.get(ace.aclId);
                aces.add(ace);
            }
        } finally {
            DbUtils.close(resultSet);
        }
        for (Acl acl : aclsMap.values()) {
            acl.aces = ImmutableSet.copyOf(acesMap.get(acl.aclId));
        }
        return aclsMap.values();
    }

    private Map<Long, PermissionTarget> getAllPermissionTargets() throws SQLException {
        ResultSet resultSet = null;
        Map<Long, Set<String>> repoKeys = getAllRepoKeys();
        Map<Long, PermissionTarget> permTargets = new HashMap<>(repoKeys.size());
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM permission_targets");
            while (resultSet.next()) {
                PermissionTarget pt = Converters.permissionTarget(resultSet);
                Set<String> keys = repoKeys.get(pt.permTargetId);
                pt.repoKeys = keys == null ? ImmutableSet.of() : ImmutableSet.copyOf(keys);
                permTargets.put(pt.permTargetId, pt);
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return permTargets;
    }

    private Map<Long, Set<String>> getAllRepoKeys() throws SQLException {
        ResultSet resultSet = null;
        Map<Long, Set<String>> repoKeys = new HashMap<>(64);
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM permission_target_repos");
            while (resultSet.next()) {
                repoKeys.computeIfAbsent(resultSet.getLong(1),
                        k -> new HashSet<>(3)).add(resultSet.getString(2));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return repoKeys;
    }

    private Map<Long, String> getAllGroupNamePerIds() throws SQLException {
        Map<Long, String> results = new HashMap<>();
        String query = "SELECT group_id, group_name FROM groups";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query)){
            while (resultSet.next()) {
                results.put(resultSet.getLong(1), resultSet.getString(2));
            }
        }
        return results;
    }

    private Map<Long, String> getAllUsernamePerIds() throws SQLException {
        Map<Long, String> results = new HashMap<>();
        String query = "SELECT user_id, username FROM users";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query)) {
            while (resultSet.next()) {
                results.put(resultSet.getLong(1), resultSet.getString(2));
            }
        }
        return results;
    }

    private <T> T wrapSqlException(String model, CheckedSupplier<T, SQLException> function) {
        try {
            return function.get();
        } catch (SQLException e) {
            throw new StorageException("Could not get all " + model, e);
        }
    }

    private static class Converters {
        private static UserProperty propertyFromData(ResultSet resultSet) throws SQLException {
            String propKey = resultSet.getString(2);
            String propValue = emptyIfNull(resultSet.getString(3));
            return new UserProperty(propKey, propValue);
        }

        private static User user(ResultSet rs) throws SQLException {
            return new User(rs.getLong(1), rs.getString(2), emptyIfNull(rs.getString(3)),
                    nullIfEmpty(rs.getString(4)), nullIfEmpty(rs.getString(5)), nullIfEmpty(rs.getString(6)),
                    rs.getBoolean(7), rs.getBoolean(8), rs.getBoolean(9),
                    nullIfEmpty(rs.getString(10)), nullIfEmpty(rs.getString(11)), nullIfEmpty(rs.getString(12)),
                    rs.getLong(13), nullIfEmpty(rs.getString(14)),
                    nullIfEmpty(rs.getString(17)), rs.getBoolean(18), rs.getBoolean(19));
        }

        private static UserGroup userGroup(ResultSet rs) throws SQLException {
            return new UserGroup(rs.getLong(1), rs.getLong(2), rs.getString(3));
        }

        private static Group group(ResultSet rs) throws SQLException {
            return new Group(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getBoolean(4),
                    rs.getString(5), rs.getString(6), rs.getBoolean(7));
        }

        private static Acl acl(ResultSet resultSet) throws SQLException {
            return new Acl(resultSet.getLong(1), resultSet.getLong(2), resultSet.getLong(3), resultSet.getString(4));
        }

        private static Ace ace(ResultSet resultSet) throws SQLException {
            return new Ace(resultSet.getLong(1), resultSet.getLong(2), resultSet.getInt(3),
                    zeroIfNull(resultSet.getLong(4)), zeroIfNull(resultSet.getLong(5)));
        }

        private static PermissionTarget permissionTarget(ResultSet resultSet) throws SQLException {
            return new PermissionTarget(
                    resultSet.getLong(1), resultSet.getString(2),
                    resultSet.getString(3), resultSet.getString(4));
        }


        private static UserInfo userToUserInfoWithProperties(SecurityEmigratorFetchers securityEmigratorFetchers, User user, Set<UserPropertyInfo> userProperties)
                throws SQLException {
            UserInfoBuilder builder = new UserInfoBuilder(user.username);
            Set<UserGroupInfo> groups = new HashSet<>(user.groups.size());
            boolean groupAdmin = false;
            for (UserGroup userGroup : user.groups) {
                Group groupById = securityEmigratorFetchers.findGroupById(userGroup.groupId);
                if (groupById != null) {
                    String groupname = groupById.groupName;
                    groupAdmin = groupById.adminPrivileges;
                    groups.add(InfoFactoryHolder.get().createUserGroup(groupname, userGroup.realm));
                } else {
                    log.error("Group ID " + userGroup.groupId + " does not exists!" +
                            " Skipping add group for user " + user.username);
                }
            }
            builder.password(new SaltedPassword(user.password, user.salt)).email(user.email)
                    .admin(user.admin).enabled(user.enabled).updatableProfile(user.updatableProfile)
                    .groups(groups);
            MutableUserInfo userInfo = builder.build();
            userInfo.setTransientUser(false);
            userInfo.setGenPasswordKey(user.genPasswordKey);
            userInfo.setRealm(user.realm);
            userInfo.setPrivateKey(user.privateKey);
            userInfo.setPublicKey(user.publicKey);
            userInfo.setLastLoginTimeMillis(user.lastLoginTimeMillis);
            userInfo.setLastLoginClientIp(user.lastLoginClientIp);
            userInfo.setBintrayAuth(user.bintrayAuth);
            userInfo.setLocked(user.locked);
            userInfo.setCredentialsExpired(user.credentialsExpired);
            userInfo.setCredentialsNonExpired(!user.credentialsExpired);
            userInfo.setGroupAdmin(groupAdmin);
            if (userProperties != null && !userProperties.isEmpty()) {
                userProperties.forEach(prop -> userInfo.putUserProperty(prop.getPropKey(),
                        CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), prop.getPropValue())));
            }
            return userInfo;
        }

        private static GroupInfo groupToGroupInfo(Group group) {
            MutableGroupInfo result = InfoFactoryHolder.get().createGroup(group.groupName);
            result.setDescription(group.description);
            result.setNewUserDefault(group.newUserDefault);
            result.setRealm(group.realm);
            result.setRealmAttributes(group.realmAttributes);
            result.setAdminPrivileges(group.adminPrivileges);
            return result;
        }

        private static RepoPermissionTarget permissionTargetToInfo(PermissionTarget permTarget) {
            MutableRepoPermissionTarget info = InfoFactoryHolder.get().createRepoPermissionTarget(
                    permTarget.name, new ArrayList<>(permTarget.repoKeys));
            info.setIncludes(permTarget.includes);
            info.setExcludes(permTarget.excludes);

            return info;
        }

        private static RepoAcl aclToInfo(Map<Long, String> groups, Map<Long, String> users,
                Map<Long, RepoPermissionTarget> targetMap, Acl acl) {
            return InfoFactoryHolder.get().createRepoAcl(targetMap.get(acl.permTargetId),
                    convertAces(groups, users, acl.aces), acl.lastModifiedBy, acl.lastModified);
        }

        private static Set<AceInfo> convertAces(Map<Long, String> groups, Map<Long, String> users, ImmutableSet<Ace> aces) {
            return aces.stream().map(
                    ace -> InfoFactoryHolder.get().createAce(
                            ace.isOnGroup() ? groups.get(ace.groupId) : users.get(ace.userId),
                            ace.isOnGroup(),
                            ace.mask)
            ).collect(Collectors.toSet());
        }
    }

    private static class Ace {
        private final long aceId;
        private final long aclId;
        private final int mask;
        private final long userId;
        private final long groupId;

        private Ace(long aceId, long aclId, int mask, long userId, long groupId) {
            this.aceId = aceId;
            this.aclId = aclId;
            this.mask = mask;
            this.userId = userId;
            this.groupId = groupId;
        }

        private boolean isOnGroup() {
            return groupId > 0;
        }
    }

    private static class Acl {
        private final long aclId;
        private final long permTargetId;
        private final long lastModified;
        private final String lastModifiedBy;
        private ImmutableSet<Ace> aces;

        private Acl(long aclId, long permTargetId, long lastModified, String lastModifiedBy) {
            this.aclId = aclId;
            this.permTargetId = permTargetId;
            this.lastModified = lastModified;
            this.lastModifiedBy = lastModifiedBy;
            this.aces = aces;
        }
    }

    private static class PermissionTarget {
        private final long permTargetId;
        private final String name;
        private final List<String> includes;
        private final List<String> excludes;
        private ImmutableSet<String> repoKeys;

        private PermissionTarget(long id, String name, String includes, String excludes) {
            this.permTargetId = id;
            this.name = name;
            this.includes = includes == null ? Collections.emptyList() : PathUtils.includesExcludesPatternToStringList(includes);
            this.excludes = excludes == null ? Collections.emptyList() : PathUtils.includesExcludesPatternToStringList(excludes);
        }
    }

    private static class UserGroup {
        private final long userId;
        private final long groupId;
        private final String realm;

        private UserGroup(long userId, long groupId, String realm) {
            this.userId = userId;
            this.groupId = groupId;
            this.realm = realm;
        }
    }

    private static class User {
        private final long userId;
        private final String username;
        private final String password;
        private final String salt;
        private final String email;
        private final String genPasswordKey;
        private final String bintrayAuth;
        private final boolean admin;
        private final boolean enabled;
        private final boolean updatableProfile;

        private final boolean locked;
        private final boolean credentialsExpired;

        private final String realm;
        private final String privateKey;
        private final String publicKey;

        private final long lastLoginTimeMillis;
        private final String lastLoginClientIp;
        private ImmutableSet<UserGroup> groups;

        private User(long userId, String username,
                String password, String salt, String email, String genPasswordKey,
                boolean admin, boolean enabled, boolean updatableProfile,
                String realm, String privateKey, String publicKey,
                long lastLoginTimeMillis, String lastLoginClientIp,
                String bintrayAuth, boolean locked, boolean credentialsExpired) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.salt = salt;
            this.email = email;
            this.genPasswordKey = genPasswordKey;
            this.bintrayAuth = bintrayAuth;
            this.admin = admin;
            this.enabled = enabled;
            this.updatableProfile = updatableProfile;
            this.realm = realm;
            this.privateKey = privateKey;
            this.publicKey = publicKey;
            this.lastLoginTimeMillis = lastLoginTimeMillis;
            this.lastLoginClientIp = lastLoginClientIp;
            this.locked = locked;
            this.credentialsExpired = credentialsExpired;
        }
    }

    private static class Group {
        private final long groupId;
        private final String groupName;
        private final String description;
        private final boolean newUserDefault;
        private final boolean adminPrivileges;
        private final String realm;
        private final String realmAttributes;

        private Group(long groupId, String groupName, String description, boolean newUserDefault, String realm,
                String realmAttributes, boolean adminPrivileges) {
            this.groupId = groupId;
            this.groupName = groupName;
            this.description = description;
            this.newUserDefault = newUserDefault;
            this.realm = realm;
            this.realmAttributes = realmAttributes;
            this.adminPrivileges = adminPrivileges;
        }
    }
}