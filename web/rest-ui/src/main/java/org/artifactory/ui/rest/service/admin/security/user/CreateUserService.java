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

import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.api.security.UserInfoBuilder;
import org.artifactory.model.xstream.security.UserGroupImpl;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.validator.NameValidator;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.SaltedPassword;
import org.artifactory.security.UserGroupInfo;
import org.artifactory.ui.rest.model.admin.security.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreateUserService<T extends User> implements RestService<T> {

    protected SecurityService securityService;
    protected UserGroupService userGroupService;

    @Autowired
    public CreateUserService(SecurityService securityService, UserGroupService userGroupService) {
        this.securityService = securityService;
        this.userGroupService = userGroupService;
    }

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        User user = request.getImodel();

        Optional<String> possibleError = NameValidator.validate(user.getName());
        if (possibleError.isPresent()) {
            response.error(possibleError.get());
            return;
        }
        MutableUserInfo newUser = getMutableUserInfo(user);
        //create user in DB
        boolean created = user.isDisableUIAccess() ? userGroupService.createUserWithNoUIAccess(newUser)
                : userGroupService.createUser(newUser);
        //Update Artifactory Response Data
        updateArtifactoryResponse(response, user, created);
    }

    /**
     * create mutable user info from user model data
     *
     * @param user - user model
     * @return mutable user info build with user model data
     */
    private MutableUserInfo getMutableUserInfo(User user) {
        UserInfoBuilder builder = new UserInfoBuilder(user.getName());
        SaltedPassword saltedPassword;
        if (user.isInternalPasswordDisabled() || (user.getPassword() == null)) {
            saltedPassword = new SaltedPassword("", null);
        } else {
            saltedPassword = securityService.generateSaltedPassword(user.getPassword());
        }
        Set<UserGroupInfo> userGroupsFromUI = user.getUserGroups() == null ? new HashSet<>() : user.getUserGroups();
        builder.password(saltedPassword)
                .passwordDisabled(user.isInternalPasswordDisabled())
                .email(user.getEmail())
                .admin(user.isAdmin())
                .updatableProfile(user.isProfileUpdatable())
                .groups(userGroupsFromUI);
        return builder.build();
    }

    /**
     * update artifactory response with model ans status code to response
     *
     * @param artifactoryRestResponse - encapsulate data require for response
     * @param user                    - new user model
     * @param succeeded               - if true user has been successfully created
     */
    private void updateArtifactoryResponse(RestResponse artifactoryRestResponse, User user, boolean succeeded) {
        if (!succeeded) {
            artifactoryRestResponse.error("User '" + user.getName() + "' already exists");
            return;
        }
        // update successful user creation data
        artifactoryRestResponse.info("Successfully created user '" + user.getName() + "'");
        artifactoryRestResponse.responseCode(HttpServletResponse.SC_CREATED);
    }
}
