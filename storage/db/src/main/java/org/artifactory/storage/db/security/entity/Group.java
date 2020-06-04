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

package org.artifactory.storage.db.security.entity;

import org.apache.commons.lang.StringUtils;

/**
 * Date: 8/26/12
 * Time: 11:08 PM
 *
 * @deprecated Users and groups are in access
 * @author freds
 */
@Deprecated
public class Group {
    private final long groupId;

    private final String groupName;

    private final String description;

    /**
     * indicates if this group should automatically be added to newly created users
     */
    private final boolean newUserDefault;

    private final boolean adminPrivileges;

    private final String realm;

    private final String realmAttributes;

    public Group(long groupId, String groupName, String description, boolean newUserDefault, String realm,
            String realmAttributes, boolean adminPrivileges) {
        if (groupId <= 0L) {
            throw new IllegalArgumentException("Group id cannot be zero or negative!");
        }
        if (StringUtils.isBlank(groupName)) {
            throw new IllegalArgumentException("Group name cannot be null!");
        }
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
        this.newUserDefault = newUserDefault;
        this.realm = realm;
        this.realmAttributes = realmAttributes;
        this.adminPrivileges = adminPrivileges;
    }

    public long getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isNewUserDefault() {
        return newUserDefault;
    }

    public boolean isAdminPrivileges() {
        return adminPrivileges;
    }

    public String getRealm() {
        return realm;
    }

    public String getRealmAttributes() {
        return realmAttributes;
    }

    public boolean isIdentical(Group group) {
        if (this == group) {
            return true;
        }
        if (group == null || getClass() != group.getClass()) {
            return false;
        }
        if (newUserDefault != group.newUserDefault) {
            return false;
        }
        if (description != null ? !description.equals(group.description) : group.description != null) {
            return false;
        }
        if (!groupName.equals(group.groupName)) {
            return false;
        }
        if (realm != null ? !realm.equals(group.realm) : group.realm != null) {
            return false;
        }
        if (realmAttributes != null ? !realmAttributes.equals(group.realmAttributes) : group.realmAttributes != null) {
            return false;
        }
        return adminPrivileges == group.adminPrivileges;
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                ", description='" + description + '\'' +
                ", newUserDefault=" + newUserDefault +
                ", realm='" + realm + '\'' +
                ", realmAttributes='" + realmAttributes + '\'' +
                ", adminPrivileges='" + adminPrivileges + '\'' +
                '}';
    }
}
