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

package org.artifactory.ui.rest.service.admin.security.user;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.common.SecurityModelPopulator;
import org.artifactory.ui.rest.model.admin.security.user.User;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAllUsersService implements RestService {

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        List<UserInfo> userInfos = userGroupService.getAllUsers(true);
        CoreAddons addons = addonsManager.addonByType(CoreAddons.class);
        DateTimeFormatter dateFormatter = configService.getDateFormatter();
        List<User> baseUserList  = userInfos.parallelStream()
                .filter(userInfo -> !addons.isAolAdmin(userInfo))
                .map(SecurityModelPopulator::getUserConfiguration)
                .peek(user -> setUserLastLogin(user, dateFormatter))
                .collect(Collectors.toList());
        response.iModelList(baseUserList);
    }
    private void setUserLastLogin(User user, DateTimeFormatter formatter) {
        if (user.getLastLoggedInMillis() > 0) {
            user.setLastLoggedIn(formatter.print(user.getLastLoggedInMillis()));
        }
    }
}
