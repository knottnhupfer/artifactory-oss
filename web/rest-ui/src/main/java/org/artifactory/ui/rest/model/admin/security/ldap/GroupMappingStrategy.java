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

package org.artifactory.ui.rest.model.admin.security.ldap;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class GroupMappingStrategy extends BaseModel {

    private String groupKeyMember;
    private String filter;
    private String groupNameAttribute;
    private String description;

    public GroupMappingStrategy(String groupKeyMember, String filter, String groupNameAttribute, String description) {
        this.groupKeyMember = groupKeyMember;
        this.filter = filter;
        this.groupNameAttribute = groupNameAttribute;
        this.description = description;
    }

    public String getGroupKeyMember() {
        return groupKeyMember;
    }

    public void setGroupKeyMember(String groupKeyMember) {
        this.groupKeyMember = groupKeyMember;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getGroupNameAttribute() {
        return groupNameAttribute;
    }

    public void setGroupNameAttribute(String groupNameAttribute) {
        this.groupNameAttribute = groupNameAttribute;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
