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

package org.artifactory.ui.rest.service.admin.security.user.userprofile;

import org.apache.http.HttpStatus;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.AccessLogger;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.model.admin.configuration.bintray.BintrayUIModel;
import org.artifactory.ui.rest.model.admin.configuration.ssh.SshClientUIModel;
import org.artifactory.ui.rest.model.admin.security.user.User;
import org.artifactory.ui.rest.model.admin.security.user.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateUserProfileService implements RestService {

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserProfileHelperService userProfileHelperService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        UserProfile profile = (UserProfile) request.getImodel();
        // update user profile
        updateUserInfo(response, profile);
    }

    private void updateUserInfo(RestResponse artifactoryResponse, UserProfile profile) {
        User user = profile.getUser();
        BintrayUIModel bintray = profile.getBintray();
        SshClientUIModel ssh = profile.getSsh();
        UserInfo userInfo = userProfileHelperService.loadUserInfo();
        if (userInfo.isAnonymous()) {
            artifactoryResponse.error("Unable to edit settings for anonymous user");
            artifactoryResponse.responseCode(HttpStatus.SC_FORBIDDEN);
            return;
        }
        if (!userInfo.isUpdatableProfile()) {
            artifactoryResponse.error("User not allowed to update profile");
            artifactoryResponse.responseCode(HttpStatus.SC_FORBIDDEN);
            return;
        }
        if (userInfo.isPasswordDisabled() && !StringUtils.isEmpty(user.getPassword())) {
            artifactoryResponse.error("User not allowed to set password");
            artifactoryResponse.responseCode(HttpStatus.SC_FORBIDDEN);
            return;
        }
        if (!StringUtils.hasText(profile.getUser().getEmail()) && userInfo.getRealm().equals("internal")) {
            artifactoryResponse.error("Email address is required");
        } else if (StringUtils.hasText(bintray.getUserName()) &&
                !StringUtils.hasText(bintray.getApiKey())) {
            artifactoryResponse.error("Cannot save Bintray username without an API key");
        } else if (StringUtils.hasText(bintray.getApiKey()) &&
                !StringUtils.hasText(bintray.getUserName())) {
            artifactoryResponse.error("Cannot save Bintray API key without username");
        } else {
            updateUserProfileToDB(artifactoryResponse, profile, user, bintray, ssh, userInfo);
        }
    }

    /**
     * update user profile to DB
     * @param artifactoryResponse - encapsulate data require for response
     * @param profile - update user profile
     * @param user - user part from user profile
     * @param bintray -- bintray part from user profile
     * @param ssh
     * @param userInfo - current logged user info
     */
    private void updateUserProfileToDB(RestResponse artifactoryResponse, UserProfile profile, User user,
            BintrayUIModel bintray, SshClientUIModel ssh, UserInfo userInfo) {
        MutableUserInfo mutableUser = InfoFactoryHolder.get().copyUser(userInfo);
        mutableUser.setEmail(profile.getUser().getEmail());
        if (org.apache.commons.lang.StringUtils.isNotBlank(bintray.getApiKey()) &&
                org.apache.commons.lang.StringUtils.isNotBlank(bintray.getUserName())) {
            mutableUser.setBintrayAuth(bintray.getUserName() + ":" + bintray.getApiKey());
        } else {
            mutableUser.setBintrayAuth("");
        }
        if (!authorizationService.isDisableInternalPassword()) {
            String newPassword = user.getPassword();
            if (StringUtils.hasText(newPassword)) {
                mutableUser.setPassword(securityService.generateSaltedPassword(newPassword));
              }
        }
        userGroupService.updateUser(mutableUser, !mutableUser.hasSameAuthorizationContext(userInfo));

        userGroupService.deleteUserProperty(userInfo.getUsername(), "sshPublicKey");
        if ((ssh != null) && org.apache.commons.lang.StringUtils.isNotBlank(ssh.getPublicKey())) {
            String publicKey = ssh.getPublicKey();
            String[] keyTokens = publicKey.split("\\s");
            userGroupService.addUserProperty(userInfo.getUsername(), "sshPublicKey", keyTokens[0] + (keyTokens.length >= 2 ? " " + keyTokens[1] : ""));
        }

        AccessLogger.updated(
                String.format("The user: '%s' has updated his profile successfully", mutableUser.getUsername()));
        artifactoryResponse.info("Successfully updated profile '" + mutableUser.getUsername() + "'");
    }
}
