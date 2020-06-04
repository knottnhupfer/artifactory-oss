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

package org.artifactory.model.xstream.security;

import com.google.common.collect.ImmutableSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.sapi.security.SecurityConstants;
import org.artifactory.security.*;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Fred Simon
 */
@XStreamAlias("user")
public class UserImpl implements MutableUserInfo {
    private static final Logger log = LoggerFactory.getLogger(UserImpl.class);

    private String username;
    private String password;
    private String email;
    private String salt;
    private String genPasswordKey;
    private boolean admin;
    @XStreamOmitField
    private Boolean groupAdmin;
    private boolean enabled;
    private boolean updatableProfile;
    private boolean accountNonExpired;
    private boolean credentialsExpired;
    private boolean credentialsNonExpired;
    private boolean accountNonLocked;

    private String realm;
    private String privateKey;
    private String publicKey;
    private boolean transientUser;

    private Set<UserGroupInfo> groups = new HashSet<>(1);
    private Set<UserPropertyInfo> userPropertyInfos = new HashSet<>();

    private long lastLoginTimeMillis;
    private String lastLoginClientIp;

    private long lastAccessTimeMillis;
    private String lastAccessClientIp;

    private String bintrayAuth;

    private boolean locked;
    @XStreamOmitField
    private boolean passwordDisabled;


    public UserImpl() {
    }

    public UserImpl(String username) {
        this.username = username;
    }

    public UserImpl(UserInfo user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.salt = user.getSalt();
        this.email = user.getEmail();
        this.admin = user.isAdmin();
        this.groupAdmin = user.isGroupAdminVerbatim();
        this.enabled = user.isEnabled();
        this.updatableProfile = user.isUpdatableProfile();
        this.accountNonExpired = user.isAccountNonExpired();
        this.credentialsExpired = user.isCredentialsExpired();
        this.credentialsNonExpired = user.isCredentialsNonExpired();
        this.accountNonLocked = user.isAccountNonLocked();
        this.transientUser = user.isTransientUser();
        this.realm = user.getRealm();

        Set<UserGroupInfo> groups = user.getGroups();
        if (groups != null) {
            this.groups = new HashSet<>(groups);
        } else {
            this.groups = new HashSet<>(1);
        }
        Set<UserPropertyInfo> userPropertyInfos = user.getUserProperties();
        if (userPropertyInfos != null) {
            this.userPropertyInfos = new HashSet<>(userPropertyInfos);
        } else {
            this.userPropertyInfos = new HashSet<>();
        }

        setPrivateKey(user.getPrivateKey());
        setPublicKey(user.getPublicKey());
        setGenPasswordKey(user.getGenPasswordKey());
        setLastLoginClientIp(user.getLastLoginClientIp());
        setLastLoginTimeMillis(user.getLastLoginTimeMillis());
        setBintrayAuth(user.getBintrayAuth());
        setLocked(user.isLocked());
        setCredentialsExpired(user.isCredentialsExpired());
        setCredentialsNonExpired(user.isCredentialsNonExpired());
        setPasswordDisabled(user.isPasswordDisabled());
    }

    private static boolean equalGroupsSet(Set<UserGroupInfo> s1, Set<UserGroupInfo> s2) {
        if (s1 == s2) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        if (s1.equals(s2)) {
            return true;
        }
        if (s1.size() != s2.size()) {
            return false;
        }
        for (UserGroupInfo g1 : s1) {
            if (!s2.contains(g1)) {
                return false;
            }
        }
        return true;
    }

    private static UserGroupInfo getDummyGroup(String groupName) {
        UserGroupInfo userGroupInfo = new UserGroupImpl(groupName, "whatever");
        return userGroupInfo;
    }

    /**
     * @deprecated Not supported by access and has no use in artifactory ui
     */
    @Deprecated
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    /**
     * @deprecated Not supported by access and has no use in artifactory ui
     */
    @Deprecated
    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(SaltedPassword saltedPassword) {
        this.password = saltedPassword.getPassword();
        this.salt = saltedPassword.getSalt();
    }

    @Override
    public String getSalt() {
        return salt;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPrivateKey() {
        return privateKey;
    }

    @Override
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String getGenPasswordKey() {
        return genPasswordKey;
    }

    @Override
    public void setGenPasswordKey(String genPasswordKey) {
        this.genPasswordKey = genPasswordKey;
    }

    @Override
    public boolean isEffectiveAdmin() {
        return admin || isGroupAdmin();
    }

    @Override
    public boolean isAdmin() {
        return admin;
    }

    @Override
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public boolean isGroupAdmin() {
        if (groupAdmin == null) {
            if (CollectionUtils.notNullOrEmpty(groups)) {
                UserGroupService userGroupService = (ContextHelper.get() != null) ?
                        ContextHelper.get().beanForType(UserGroupService.class) : null;
                if (userGroupService != null) {
                    List<String> adminGroupNames = userGroupService.getAllAdminGroupsNames();
                    boolean isInAdminGroup = groups.stream()
                            .map(UserGroupInfo::getGroupName)
                            .anyMatch(adminGroupNames::contains);
                    setGroupAdmin(isInAdminGroup);
                } else {
                    log.warn(
                            "Artifactory context not found, can't determine effective admin status for user: '{}'.",
                            username);
                }
            } else {
                setGroupAdmin(false);
            }
        }
        return groupAdmin != null && groupAdmin;
    }

    @Override
    public Boolean isGroupAdminVerbatim() {
        return groupAdmin;
    }

    @Override
    public void setGroupAdmin(Boolean groupAdmin) {
        this.groupAdmin = groupAdmin;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isUpdatableProfile() {
        return updatableProfile;
    }

    @Override
    public void setUpdatableProfile(boolean updatableProfile) {
        this.updatableProfile = updatableProfile;
    }


    /**
     * @deprecated Not supported by access and has no use in artifactory ui
     */
    @Override
    @Deprecated
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }


    /**
     * @deprecated Not supported by access and has no use in artifactory ui
     */
    @Override
    @Deprecated
    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }


    /**
     * @deprecated Not supported by access and has no use in artifactory ui
     */
    @Override
    @Deprecated
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }


    /**
     * @deprecated Not supported by access and has no use in artifactory ui
     */
    @Override
    @Deprecated
    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    @Override
    public boolean isTransientUser() {
        return transientUser;
    }

    @Override
    public void setTransientUser(boolean transientUser) {
        this.transientUser = transientUser;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public void setRealm(String realm) {
        this.realm = realm;
    }

    @Override
    public boolean isExternal() {
        return !SecurityConstants.DEFAULT_REALM.equals(realm);
    }

    @Override
    public boolean isCredentialsExpired() {
        return credentialsExpired;
    }

    @Override
    public void setCredentialsExpired(boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    @Override
    public boolean isAnonymous() {
        return (username != null && username.equalsIgnoreCase(ANONYMOUS));
    }

    @Override
    public boolean isInGroup(String groupName) {
        //Use the equals() behavior with a dummy userGroupInfo
        UserGroupInfo userGroupInfo = getDummyGroup(groupName);
        return getGroups().contains(userGroupInfo);
    }

    @Override
    public void addGroup(String groupName) {
        addGroup(groupName, SecurityConstants.DEFAULT_REALM);
    }

    @Override
    public void addGroup(String groupName, String realm) {
        addGroupToGroupList(groupName, realm);
        UserGroupService userGroupService = (ContextHelper.get() != null) ?
                ContextHelper.get().beanForType(UserGroupService.class) : null;
        if (userGroupService != null) {
            GroupInfo artifactoryGroupInfo = userGroupService.findGroup(groupName);
            if (artifactoryGroupInfo != null && artifactoryGroupInfo.isAdminPrivileges()) {
                setGroupAdmin(true);
            }
        }
    }

    @Override
    public void addGroup(GroupInfo groupInfo) {
        addGroupToGroupList(groupInfo.getGroupName(), groupInfo.getRealm());
        if (groupInfo.isAdminPrivileges()) {
            setGroupAdmin(true);
        }
    }

    @Override
    public void removeGroup(String groupName) {
        //Use the equals() behavior with a dummy userGroupInfo
        UserGroupInfo userGroupInfo = getDummyGroup(groupName);
        _groups().remove(userGroupInfo);
    }

    /**
     * @return The _groups() names this user belongs to. Empty list if none.
     */
    @Override
    public Set<UserGroupInfo> getGroups() {
        return ImmutableSet.copyOf(_groups());
    }

    /**
     * @return The _groups() names this user belongs to. Empty list if none.
     */
    @Override
    public Set<UserGroupInfo> getGroupsReference() {
        return _groups();
    }

    @Override
    public void setGroups(Set<UserGroupInfo> groups) {
        if (groups == null) {
            this.groups = new HashSet<>(1);
        } else {
            this.groups = new HashSet<>(groups);
        }
    }

    @Override
    public Set<UserPropertyInfo> getUserProperties() {
        if (userPropertyInfos == null) {
            this.userPropertyInfos = new HashSet<>();
        }
        return userPropertyInfos;
    }

    @Override
    public void setUserProperties(Set<UserPropertyInfo> userProperties) {
        userPropertyInfos = userProperties;
    }

    private void addGroupToGroupList(String groupName, String realm) {
        UserGroupInfo userGroupInfo = new UserGroupImpl(groupName, realm);
        // group equality is currently using group name only, so make sure to remove existing group with the same name
        _groups().remove(userGroupInfo);
        _groups().add(userGroupInfo);
    }

    // Needed because XStream inject nulls :(
    private Set<UserGroupInfo> _groups() {
        if (groups == null) {
            this.groups = new HashSet<>(1);
        }
        return groups;
    }

    @Override
    public void putUserProperty(String key, String val) {
        if (userPropertyInfos == null) {
            this.userPropertyInfos = new HashSet<>();
        }
        userPropertyInfos.add(new UserProperty(key, val));
    }

    @Override
    public Optional<String> getUserProperty(String key) {
        if (CollectionUtils.isNullOrEmpty(userPropertyInfos)) {
            return Optional.empty();
        }
        return userPropertyInfos.stream().filter(p -> p.getPropKey().equals(key))
                .map(UserPropertyInfo::getPropValue).findFirst();
    }

    @Override
    public void setInternalGroups(Set<String> groups) {
        if (groups == null) {
            this.groups = new HashSet<>(1);
            return;
        }
        //Add groups with the default internal realm
        _groups().clear();
        for (String group : groups) {
            addGroup(group);
        }
    }

    @Override
    public long getLastLoginTimeMillis() {
        return lastLoginTimeMillis;
    }

    @Override
    public void setLastLoginTimeMillis(long lastLoginTimeMillis) {
        this.lastLoginTimeMillis = lastLoginTimeMillis;
    }

    @Override
    public String getLastLoginClientIp() {
        return lastLoginClientIp;
    }

    @Override
    public void setLastLoginClientIp(String lastLoginClientIp) {
        this.lastLoginClientIp = lastLoginClientIp;
    }

    @Override
    @Deprecated
    public long getLastAccessTimeMillis() {
        return lastAccessTimeMillis;
    }

    @Override
    @Deprecated
    public void setLastAccessTimeMillis(long lastAccessTimeMillis) {
        this.lastAccessTimeMillis = lastAccessTimeMillis;
    }

    @Override
    @Deprecated
    public String getLastAccessClientIp() {
        return lastAccessClientIp;
    }

    @Override
    @Deprecated
    public void setLastAccessClientIp(String lastAccessClientIp) {
        this.lastAccessClientIp = lastAccessClientIp;
    }

    @Override
    public String getBintrayAuth() {
        return bintrayAuth;
    }

    @Override
    public void setBintrayAuth(String bintrayAuth) {
        this.bintrayAuth = bintrayAuth;
    }

    /**
     * Compare the groups and login flags of the users to know if a force re-login is needed.
     *
     * @return true if users have same flags and groups, false otherwise.
     */
    @Override
    public boolean hasSameAuthorizationContext(UserInfo o) {
        if (o == null) {
            return false;
        }
        return isAdmin() == o.isAdmin()
                && isEnabled() == o.isEnabled()
                && hasSamePassword(o)
                && isAccountNonLocked() == o.isAccountNonLocked()
                && isCredentialsExpired() == o.isCredentialsExpired()
                && equalGroupsSet(getGroups(), o.getGroups());
    }

    private boolean hasSamePassword(UserInfo user) {
        return password != null ? password.equals(user.getPassword()) : user.getPassword() == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserImpl info = (UserImpl) o;

        return !(username != null ? !username.equals(info.username) : info.username != null);
    }

    @Override
    public String toString() {
        return username;
    }

    @Override
    public int hashCode() {
        return (username != null ? username.hashCode() : 0);
    }

    /**
     * @return whether given user is locked
     */
    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public boolean isPasswordDisabled() {
        return passwordDisabled;
    }

    @Override
    public void setPasswordDisabled(boolean passwordDisabled) {
        this.passwordDisabled = passwordDisabled;
    }

    /**
     * @param locked whether given user is locked
     */
    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

}