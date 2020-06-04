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
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.GroupInfo;
import org.artifactory.ui.rest.common.SecurityModelPopulator;
import org.artifactory.ui.rest.model.admin.security.group.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.artifactory.addon.CoreAddons.SUPER_USER_NAME;

/**
 * @author Chen Keinan
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetGroupService implements RestService {

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String groupName = request.getPathParamByKey("id");
        Group group = getGroup(groupName);
        if (group == null) {
            response.error("No such group '" + groupName + "'.").responseCode(HttpStatus.SC_NOT_FOUND);
        } else {
            addGroupUsers(groupName, group);
            response.iModel(group);
        }
    }

    private void addGroupUsers(String groupName, Group group) {
        List<String> usersInGroup = userGroupService.findUsersInGroup(groupName);
        if (usersInGroup != null) {
            if (addonsManager.addonByType(CoreAddons.class).isAol()) {
                usersInGroup.remove(SUPER_USER_NAME);
            }
            group.setUsersInGroup(usersInGroup);
        }
    }

    private Group getGroup(String groupName) {
        GroupInfo group = userGroupService.findGroup(groupName);
        return group == null ? null : SecurityModelPopulator.getGroupConfiguration(group);
    }
}
