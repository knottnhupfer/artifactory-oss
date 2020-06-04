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

import com.google.common.collect.Lists;
import org.apache.http.HttpStatus;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.validator.NameValidator;
import org.artifactory.security.MutableGroupInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.model.admin.security.group.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreateGroupService extends BaseGroupService {
    @Autowired
    protected SecurityService securityService;
    @Autowired
    protected UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        MutableGroupInfo group = (MutableGroupInfo) request.getImodel();
        String groupName = group.getGroupName();
        Optional<String> possibleError = NameValidator.validate(groupName);
        if (possibleError.isPresent()) {
            response.error(possibleError.get());
            return;
        }

        if (userGroupService.findGroup(groupName) != null) {
            response.error("Group '" + groupName + "' already exists.");
            return;
        }

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
        boolean created = userGroupService.createGroup(group);
        addUsertoGroup(usersInGroup, groupName, response, userGroupService);
        // update response data
        updateResponse(response, group, created);
    }


    /**
     * create group in DB and update response
     * @param restResponse - encapsulate data require for response
     * @param created - if true group successfully created
     */
    private void updateResponse(RestResponse restResponse,
            MutableGroupInfo group,boolean created) {
        if (!created) {
            String errorMsg = "Error with creating group: " + group.getGroupName() ;
            restResponse.error(errorMsg);
            return;
        }
        else{
            restResponse.info("Successfully created group '" + group.getGroupName() + "'");
            restResponse.responseCode(HttpServletResponse.SC_CREATED);
        }
    }
}
