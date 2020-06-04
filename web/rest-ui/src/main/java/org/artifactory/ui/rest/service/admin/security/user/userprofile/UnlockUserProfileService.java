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

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.PasswordEncryptionFailureException;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.model.admin.configuration.bintray.BintrayUIModel;
import org.artifactory.ui.rest.model.admin.configuration.ssh.SshClientUIModel;
import org.artifactory.ui.rest.model.admin.security.login.UserLogin;
import org.artifactory.ui.rest.model.admin.security.user.User;
import org.artifactory.ui.rest.model.admin.security.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UnlockUserProfileService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(UnlockUserProfileService.class);

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private UserProfileHelperService userProfileHelperService;


    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        UserLogin userLogin = (UserLogin) request.getImodel();
        // fetch user profile
        fetchUserProfile(response, userLogin);
    }

    /**
     * fetch user profile
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param userLogin           - hold entered password details
     */
    private void fetchUserProfile(RestResponse artifactoryResponse, UserLogin userLogin) {
        UserInfo userInfo = userProfileHelperService.loadUserInfo();
        if (userInfo.isAnonymous()) {
            artifactoryResponse.error("Unable to unlock settings for anonymous user");
            return;
        }

        String enteredCurrentPassword = userLogin.getPassword();
        if (authorizationService.requireProfileUnlock() && !userProfileHelperService.authenticate(userInfo, enteredCurrentPassword)) {
            artifactoryResponse.error("The specified password is incorrect");
        } else {
            // get user  profile
            UserProfile userProfile = getUserProfile(userInfo, userLogin);
            artifactoryResponse.iModel(userProfile);
        }
    }

    /**
     * get user profile66
     *
     * @param userInfo - current logged user info
     * @return current user profile
     */
    private UserProfile getUserProfile(UserInfo userInfo, UserLogin userLogin) {
        UserProfile userProfile = new UserProfile();
        updateUserInfo(userInfo, userProfile, userLogin);
        updateBintrayData(userInfo, userProfile);
        updateSshInfo(userInfo, userProfile);
        return userProfile;
    }

    /**
     * update user info in profile
     *
     * @param userInfo    - user info
     * @param userProfile - user profile
     */
    private void updateUserInfo(UserInfo userInfo, UserProfile userProfile, UserLogin userLogin) {
        User user = new User();
        user.setRealm(userInfo.getRealm());
        user.setEmail(userInfo.getEmail());
        user.setProfileUpdatable(userInfo.isUpdatableProfile());
        user.setInternalPasswordDisabled(userInfo.isPasswordDisabled());
        userProfile.setUser(user);
        MutableUserInfo mutableUser = InfoFactoryHolder.get().copyUser(userInfo);
        try {
            String encryptedPassword = userGroupService.createEncryptedPasswordIfNeeded(mutableUser, userLogin.getPassword());
            user.setPassword(encryptedPassword);
        } catch (Exception e) {
            if (e instanceof PasswordEncryptionFailureException) {
                resetUserKeys(mutableUser);

                // Fallback and try to get the encryptedPassword again
                log.trace("Fallback retrieving the encrypted password");
                try {
                    String encryptedPassword = userGroupService.createEncryptedPasswordIfNeeded(mutableUser, userLogin.getPassword());
                    user.setPassword(encryptedPassword);
                } catch (PasswordEncryptionFailureException e1) {
                    resetUserKeys(mutableUser);
                    userGroupService.updateUser(mutableUser, false);
                    user.setPassword(null);
                    throw new IllegalStateException("Fallback password encryption failed", e);
                }
            }
        }
    }

    private void resetUserKeys(MutableUserInfo mutableUser) {
        mutableUser.setPrivateKey(null);
        mutableUser.setPublicKey(null);
    }

    /**
     * update user profile bintray data
     *
     * @param userInfo    - user info from db
     * @param userProfile - user profile
     */
    private void updateBintrayData(UserInfo userInfo, UserProfile userProfile) {
        String[] splitBintrayAuth = null;
        String bintrayAuth = userInfo.getBintrayAuth();

        if (userInfo.getBintrayAuth() != null) {
            if (CryptoHelper.isArtifactoryKeyEncrypted(bintrayAuth)) {
                bintrayAuth = CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), bintrayAuth);
            }
            splitBintrayAuth = bintrayAuth.split(":");
        }
        BintrayUIModel bintray = new BintrayUIModel();
        if (splitBintrayAuth != null && splitBintrayAuth.length > 1) {
            bintray.setUserName(splitBintrayAuth[0]);
            bintray.setApiKey(splitBintrayAuth[1]);
        }
        userProfile.setBintray(bintray);
    }

    private void updateSshInfo(UserInfo userInfo, UserProfile userProfile) {
        Properties propertiesForUser = userGroupService.findPropertiesForUser(userInfo.getUsername());
        String sshPublicKey = propertiesForUser.getFirst("sshPublicKey");
        SshClientUIModel sshClientUIModel = new SshClientUIModel();
        if (org.apache.commons.lang.StringUtils.isNotBlank(sshPublicKey)) {
            sshClientUIModel.setPublicKey(sshPublicKey);
        }
        userProfile.setSsh(sshClientUIModel);
    }
}
