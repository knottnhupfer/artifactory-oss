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

import java.util.Map;
import java.util.Set;

/**
 * @author Noam Y. Tenne
 */
public class PrincipalConfiguration {

    private Map<String, Set<String>> users;
    private Map<String, Set<String>> groups;

    public Map<String, Set<String>> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Set<String>> users) {
        this.users = users;
    }

    public Map<String, Set<String>> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, Set<String>> groups) {
        this.groups = groups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PrincipalConfiguration)) {
            return false;
        }

        PrincipalConfiguration that = (PrincipalConfiguration) o;

        if (groups != null ? !groups.equals(that.groups) : that.groups != null) {
            return false;
        }
        return users != null ? users.equals(that.users) : that.users == null;
    }

    @Override
    public int hashCode() {
        int result = users != null ? users.hashCode() : 0;
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        return result;
    }
}
