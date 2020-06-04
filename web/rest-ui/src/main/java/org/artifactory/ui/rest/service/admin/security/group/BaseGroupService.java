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

package org.artifactory.ui.rest.service.admin.security.group;

import org.artifactory.api.security.GroupNotFoundException;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;

import java.util.List;

/**
 * @author chen keinan
 */
public abstract class BaseGroupService implements RestService {

    /**
     * @param users     - user list to be added to group
     * @param groupName - group name
     */
    protected void addUsertoGroup(List<String> users, String groupName, RestResponse response,
                                  UserGroupService userGroupService) {
        if (users != null && !users.isEmpty()) {
            try {
                userGroupService.addUsersToGroup(
                        groupName, users);
                response.info("Successfully added selected users to group '" + groupName + "'");
            } catch (GroupNotFoundException gnfe) {
                response.error("Could not find group '" + groupName + "': " + gnfe.getMessage());
            }
        }
    }
}
