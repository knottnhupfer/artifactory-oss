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

package org.artifactory.addon.ldapgroup;

/**
 * @author Chen Keinan
 */
public class LdapUserGroup {
    private String groupName;
    private String description;
    private String groupDn;
    private Status requiredUpdate = Status.DOES_NOT_EXIST;

    protected LdapUserGroup() {
    }

    public LdapUserGroup(String groupName, String description, String groupDn) {
        this.groupName = groupName;
        this.description = description;
        this.groupDn = groupDn;
    }

    public Status getRequiredUpdate() {
        return requiredUpdate;
    }

    public void setRequiredUpdate(Status requiredUpdate) {
        this.requiredUpdate = requiredUpdate;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDescription() {
        return description;
    }

    public String getGroupDn() {
        return groupDn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LdapUserGroup group = (LdapUserGroup) o;
        return groupName.equals(group.groupName);
    }

    @Override
    public int hashCode() {
        return groupName.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("LdapGroup");
        sb.append("{groupName='").append(groupName).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public enum Status {
        DOES_NOT_EXIST("Ready to be imported."),
        REQUIRES_UPDATE("Group information is ou-of-date. DN has changed in LDAP."),
        IN_ARTIFACTORY("Group information is up-to-date in Artifactory.");

        private String description;

        Status(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
