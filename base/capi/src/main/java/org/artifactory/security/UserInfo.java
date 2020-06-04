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

package org.artifactory.security;

import org.artifactory.common.Info;

import java.util.Optional;
import java.util.Set;

/**
 * @author Fred Simon
 */
public interface UserInfo extends Info {
    String MISSION_CONTROL = "Mission-Control";
    String ANONYMOUS = "anonymous";

    String getUsername();

    String getPassword();

    String getSalt();

    String getEmail();

    String getPrivateKey();

    String getPublicKey();

    String getGenPasswordKey();

    boolean isEffectiveAdmin();

    boolean isAdmin();

    boolean isGroupAdmin();

    Boolean isGroupAdminVerbatim();

    boolean isEnabled();

    boolean isUpdatableProfile();

    /**
     * @deprecated This filed is never used by Artifactory. Account can be locked with {@link UserInfo#isLocked()}
     * Also, not supported by access and has no use in artifactory ui
     */
    @Deprecated
    boolean isAccountNonExpired();

    /**
     * @deprecated Not supported by access and has no use in artifactory ui
     */
    @Deprecated
    boolean isAccountNonLocked();

    boolean isTransientUser();

    String getRealm();

    boolean isExternal();

    /**
     * Indicates whether the user's credentials (password) has expired. Expired credentials prevent
     * authentication.
     *
     * @return <code>false</code> if the user's credentials are valid (ie non-expired), <code>true</code> if no longer
     *         valid (ie expired)
     */
    boolean isCredentialsExpired();

    /**
     * @deprecated Not supported by access and has no use in artifactory ui
     */
    @Deprecated
    boolean isCredentialsNonExpired();

    boolean isAnonymous();

    boolean isInGroup(String groupName);

    Set<UserGroupInfo> getGroups();

    Set<UserGroupInfo> getGroupsReference();

    Set<UserPropertyInfo> getUserProperties();

    /**
     * @param key The property key
     * @return Optional property value
     */
    Optional<String> getUserProperty(String key);

    long getLastLoginTimeMillis();

    String getLastLoginClientIp();
    @Deprecated
    long getLastAccessTimeMillis();
    @Deprecated
    String getLastAccessClientIp();

    boolean hasSameAuthorizationContext(UserInfo o);

    String getBintrayAuth();

    boolean isLocked();

    boolean isPasswordDisabled();
}
