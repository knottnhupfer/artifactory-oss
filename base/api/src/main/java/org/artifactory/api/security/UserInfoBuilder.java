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

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.util.Builder;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.SaltedPassword;
import org.artifactory.security.UserGroupInfo;
import org.artifactory.security.UserPropertyInfo;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Builder for user info with sensible defaults.
 *
 * @author Yossi Shaul
 */
public class UserInfoBuilder implements Builder<MutableUserInfo> {

    private final String username;

    // TODO [NS] Remove SaltedPassword object, all references should send raw password as the first parameter and ignore salt
    private SaltedPassword password = new SaltedPassword(null, null);
    private String email = "";
    private boolean admin = false;
    private Boolean groupAdmin = false;
    private boolean enabled = true;
    private boolean updatableProfile = false;
    private boolean transientUser = false;
    private Set<UserGroupInfo> groups = new HashSet<>();
    private String bintrayAuth;
    private Set<UserPropertyInfo> userPropertyInfos;
    private String privateKey;
    private String publicKey;
    private boolean credentialsExpired;
    private long lastLoginTime;
    private String lastLoginIp;
    private String genPasswordKey;
    private String realm;
    private boolean passwordDisabled;
    private boolean locked;

    public UserInfoBuilder(String username) {
        this.username = username;
    }

    /**
     * @return The user.
     */
    @Override
    public MutableUserInfo build() {
        if (StringUtils.isBlank(username)) {
            throw new IllegalStateException("User must have a username");
        }

        MutableUserInfo user = InfoFactoryHolder.get().createUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setAdmin(admin);
        user.setEnabled(enabled);
        user.setUpdatableProfile(updatableProfile);
        user.setCredentialsExpired(credentialsExpired);
        user.setCredentialsNonExpired(!credentialsExpired);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setTransientUser(transientUser);
        user.setLocked(locked);
        user.setGroups(groups);
        user.setBintrayAuth(bintrayAuth);
        user.setUserProperties(userPropertyInfos);
        user.setGroupAdmin(groupAdmin);
        user.setPrivateKey(privateKey);
        user.setPublicKey(publicKey);
        user.setLastLoginTimeMillis(lastLoginTime);
        user.setLastLoginClientIp(lastLoginIp);
        user.setGenPasswordKey(genPasswordKey);
        user.setRealm(realm);
        user.setPasswordDisabled(passwordDisabled);

        return user;
    }

    public UserInfoBuilder email(String email) {
        this.email = email;
        return this;
    }

    public UserInfoBuilder password(SaltedPassword saltedPassword) {
        this.password = saltedPassword;
        return this;
    }

    public UserInfoBuilder admin(boolean admin) {
        this.admin = admin;
        return this;
    }

    public UserInfoBuilder groupAdmin(Boolean groupAdmin) {
        this.groupAdmin = groupAdmin;
        return this;
    }

    public UserInfoBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public UserInfoBuilder updatableProfile(boolean updatableProfile) {
        this.updatableProfile = updatableProfile;
        return this;
    }

    public UserInfoBuilder credentialsExpired(boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
        return this;
    }

    public UserInfoBuilder lastLogin(long time, String ip) {
        this.lastLoginTime = time;
        this.lastLoginIp = ip;
        return this;
    }

    public UserInfoBuilder genPasswordKey(String genPasswordKey) {
        this.genPasswordKey = genPasswordKey;
        return this;
    }

    public UserInfoBuilder transientUser() {
        this.transientUser = true;
        return this;
    }

    public UserInfoBuilder internalGroups(Set<String> groupNames) {
        if (groupNames != null) {
            groups(InfoFactoryHolder.get().createGroups(groupNames));
        } else {
            groups(null);
        }
        return this;
    }

    public UserInfoBuilder groups(@Nullable Set<UserGroupInfo> groups) {
        if (groups != null) {
            this.groups = groups;
        } else {
            this.groups = Collections.emptySet();
        }
        return this;
    }

    public UserInfoBuilder addProp(UserPropertyInfo prop) {
        if (userPropertyInfos == null) {
            this.userPropertyInfos = Sets.newHashSet();
        }
        userPropertyInfos.add(prop);
        return this;
    }

    public UserInfoBuilder bintrayAuth(String bintrayAuth) {
        this.bintrayAuth = bintrayAuth;
        return this;
    }

    public UserInfoBuilder privateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public UserInfoBuilder publicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public UserInfoBuilder realm(String realm) {
        this.realm = realm;
        return this;
    }

    public UserInfoBuilder passwordDisabled(boolean passwordDisabled) {
        this.passwordDisabled = passwordDisabled;
        return this;
    }

    public UserInfoBuilder locked(boolean locked) {
        this.locked = locked;
        return this;
    }
}
