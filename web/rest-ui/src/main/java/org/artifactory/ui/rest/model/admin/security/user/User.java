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

package org.artifactory.ui.rest.model.admin.security.user;

import org.artifactory.security.UserGroupInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.model.admin.security.group.UserGroup;

import java.util.HashSet;
import java.util.Set;

/**
 * @author chen keinan
 */
public class User extends BaseUser {

    private Set<UserGroup> userGroups;
    private String externalRealmStatus;
    private boolean disableUIAccess;

    public User() {
    }

    public User(UserInfo user) {
        super(user);
    }

    public void setUserGroups(Set<UserGroup> userGroups) {
        this.userGroups = userGroups;
    }

    public Set<UserGroupInfo> getUserGroups() {
        if (userGroups == null) {
            return null;
        }

        Set<UserGroupInfo> userGroupInfos = new HashSet<>();
        for (UserGroup group : userGroups) {
            userGroupInfos.add(group);
        }
        return userGroupInfos;
    }

    public String getExternalRealmStatus() {
        return externalRealmStatus;
    }

    public void setExternalRealmStatus(String externalRealmStatus) {
        this.externalRealmStatus = externalRealmStatus;
    }

    public boolean isDisableUIAccess() {
        return disableUIAccess;
    }

    public void setDisableUIAccess(boolean disableUIAccess) {
        this.disableUIAccess = disableUIAccess;
    }
}
