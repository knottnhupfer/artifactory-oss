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

package org.artifactory.ui.rest.model.admin.security.group;

import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.security.GroupConfigurationImpl;
import org.artifactory.security.MutableGroupInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
public class Group extends GroupConfigurationImpl implements RestModel, MutableGroupInfo {

    private boolean external;

    public Group(){}

    private List<String> usersInGroup = new ArrayList<>();

    private List<String> groups;

    private List<String> permissions;

    public Group(String groupName) {
        setGroupName(groupName);
    }

    @Override
    public String getGroupName() {
        return super.getName();
    }

    @Override
    public void setGroupName(String groupName) {
        super.setName(groupName);
    }

    @Override
    public boolean isNewUserDefault() {
        return super.isAutoJoin();
    }

    @Override
    public void setNewUserDefault(boolean newUserDefault) {
        super.setAutoJoin(newUserDefault);
    }

    @Override
    public boolean isExternal() {
        return external;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }

    public List<String> getUsersInGroup() {
        return usersInGroup;
    }

    public void setUsersInGroup(List<String> usersInGroup) {
        this.usersInGroup = usersInGroup;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
