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
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.SaltedPassword;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.model.admin.security.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;

import static org.artifactory.api.security.UserGroupService.UI_VIEW_BLOCKED_USER_PROP;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateUserService<T extends User> implements RestService<T> {
    private static final Logger log = LoggerFactory.getLogger(UpdateUserService.class);

    @Autowired
    protected SecurityService securityService;
    @Autowired
    protected UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        String id = request.getPathParamByKey("id");
        if (isUserIDNotFound(id)) {
            response.responseCode(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // get orig user from db
        UserInfo origUser = userGroupService.findUser(id);
        // update orig user properties
        User user = request.getImodel();
        // save user changes to DB
        boolean userUpdated = saveUserChanges(origUser, user);
        // update response data
        updateArtifactoryResponse(response, user, userUpdated, false);
    }

    /**
     * save user changes to DB
     *
     * @param origUser - user info before changes
     * @param user     -  - user info after changes
     * @return if true user successfully saved to DB
     */
    private boolean saveUserChanges(UserInfo origUser, User user) {
        MutableUserInfo userInfo = updateUserProperties(user, origUser);
        // save changes to db
        if (saveUser(origUser, userInfo)) {
            if (user.isDisableUIAccess()) {
                userGroupService.addUserProperty(userInfo.getUsername(), UI_VIEW_BLOCKED_USER_PROP, "true");
            } else {
                userGroupService.deleteUserProperty(userInfo.getUsername(), UI_VIEW_BLOCKED_USER_PROP);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * check if user id not found
     *
     * @param id - user id found on path param
     * @return if true , user id not found on path param
     */
    private boolean isUserIDNotFound(String id) {
        return id == null || id.length() == 0;
    }

    /**
     * update user properties data
     * @param user - updated user data from request
     * @param origUser - orig user data from db
     * @return MutableUserInfo update with data from request
     */
    private MutableUserInfo updateUserProperties(User user, UserInfo origUser){
        MutableUserInfo userInfo = InfoFactoryHolder.get().copyUser(origUser);
        userInfo.setEmail(user.getEmail());
        userInfo.setAdmin(user.isAdmin());
        userInfo.setUpdatableProfile(user.isProfileUpdatable());
        userInfo.setGroups(user.getUserGroups());
        if (user.isInternalPasswordDisabled()) {
            // user should authenticate externally - set password to invalid
            userInfo.setPassword(new SaltedPassword("", null));
            userInfo.setPasswordDisabled(true);
        } else if (StringUtils.hasText(user.getPassword())) {
            userInfo.setPassword(securityService.generateSaltedPassword(user.getPassword()));
            userInfo.setPasswordDisabled(false);
        }
        return userInfo;
    }

    /**
     * save update user data to DB
     * @param origUser - orig user data
     * @param userInfo - updated user data
     * @return true if save user succeeded
     */
    private boolean saveUser(UserInfo origUser, MutableUserInfo userInfo) {
        boolean userUpdated = true;
        try {
            userGroupService.updateUser(userInfo, !userInfo.hasSameAuthorizationContext(origUser));
        }catch (Exception e){
            userUpdated = false;
            log.error("failed to update user: '{}'", userInfo.getUsername());
        }
        return userUpdated;
    }
    /**
     * update artifactory response with model ans status code to response
     * @param artifactoryRestResponse - encapsulate data require for response
     * @param user - new user model
     * @param succeeded - if true user has been successfully created
     * @param create - if true create user else update
     */
    private void updateArtifactoryResponse(RestResponse<User> artifactoryRestResponse,
            User user,boolean succeeded ,boolean create){
        if (!succeeded) {
            artifactoryRestResponse.error("User '" + user.getName() + " already exists");
            return;
        }else{
            artifactoryRestResponse.info("Successfully updated user '" + user.getName() + "'");
        }
        // update successful user creation data
        if(create) {
            artifactoryRestResponse.iModel(user);
            artifactoryRestResponse.responseCode(HttpServletResponse.SC_CREATED);
        }
    }
}
