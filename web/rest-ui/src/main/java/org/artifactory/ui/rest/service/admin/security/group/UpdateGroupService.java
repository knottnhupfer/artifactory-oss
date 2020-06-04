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

import org.apache.http.HttpStatus;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.security.MutableGroupInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.model.admin.security.group.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateGroupService extends BaseGroupService {
    private static final Logger log = LoggerFactory.getLogger(UpdateGroupService.class);

    @Autowired
    protected UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        MutableGroupInfo group = (MutableGroupInfo) request.getImodel();

        if (isResourceIDNotFound(request)) {
            response.responseCode(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            // update group model changed data
            List<String> usersInGroup = ((Group) group).getUsersInGroup();
            if (group.isAdminPrivileges()) {
                if (usersInGroup.contains(UserInfo.ANONYMOUS)) {
                    response.error("Anonymous user cannot be associated with a group with admin privileges.")
                            .responseCode(HttpStatus.SC_BAD_REQUEST);
                    return;
                }
                if (group.isNewUserDefault()) {
                    response.error("For security reasons, automatically joining new users to a group that is granted with Admin privileges is not supported.")
                            .responseCode(HttpStatus.SC_BAD_REQUEST);
                    return;
                }
            }
            updateGroupInfoData(group);
            // update users group
            userGroupService.updateGroupUsers(group, usersInGroup);
            //update response
            response.info("Successfully updated group '" + group.getGroupName() + "'");
        } catch (Exception e) {
            log.error("error updating group {} users", group.getGroupName());
            log.debug("error updating group {} users", group.getGroupName(), e);
        }
    }

    /**
     * remove group users before update
     *
     * @param group - group data
     */
    private void removePrevGroupUsers(MutableGroupInfo group) {
        List<String> usersInGroup = userGroupService.findUsersInGroup(group.getGroupName());
        if (usersInGroup != null && !usersInGroup.isEmpty()) {
            userGroupService.removeUsersFromGroup(group.getGroupName(), usersInGroup);
        }
    }

    /**
     * update group info data
     *
     * @param group - group data to be updated
     */
    private void updateGroupInfoData(MutableGroupInfo group) {
        userGroupService.updateGroup(group);
    }

    /**
     * check if resource id has been send on path param
     * artifactoryRequest - encapsulate data related to request
     *
     * @return if true resource id not found on path param
     */
    private boolean isResourceIDNotFound(ArtifactoryRestRequest artifactoryRequest) {
        String id = artifactoryRequest.getPathParamByKey("id");
        return id == null || id.length() == 0;
    }
}
