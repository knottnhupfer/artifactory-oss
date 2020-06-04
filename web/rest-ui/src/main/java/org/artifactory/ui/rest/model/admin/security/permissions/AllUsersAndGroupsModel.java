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

package org.artifactory.ui.rest.model.admin.security.permissions;

import org.artifactory.rest.common.model.BaseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Shay Yaakov
 */
public class AllUsersAndGroupsModel extends BaseModel {

    private List<PrincipalInfo> allGroups = new ArrayList<>();
    private List<PrincipalInfo> allUsers = new ArrayList<>();

    public List<PrincipalInfo> getAllGroups() {
        return allGroups;
    }

    public void setAllGroups(List<PrincipalInfo> allGroups) {
        this.allGroups = allGroups;
    }

    public List<PrincipalInfo> getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(List<PrincipalInfo> allUsers) {
        this.allUsers = allUsers;
    }

    public static class PrincipalInfo {
        private String principal;
        private boolean isAdmin;

        public PrincipalInfo(String principal, boolean isAdmin) {
            this.principal = principal;
            this.isAdmin = isAdmin;
        }

        public String getPrincipal() {
            return principal;
        }

        public void setPrincipal(String principal) {
            this.principal = principal;
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public void setAdmin(boolean admin) {
            isAdmin = admin;
        }
    }
}
