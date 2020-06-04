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

package org.artifactory.security.access;

import org.apache.http.HttpStatus;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.security.*;
import org.artifactory.storage.db.security.service.access.UserMapper;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.jfrog.access.client.AccessClientException;
import org.jfrog.access.client.AccessClientHttpException;
import org.jfrog.access.client.model.ErrorsModel;
import org.jfrog.access.client.model.MessageModel;
import org.jfrog.access.model.MessageModelCode;
import org.jfrog.access.rest.user.LoginRequest;
import org.jfrog.access.rest.user.UserBase;
import org.jfrog.access.rest.user.UserWithGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

/**
 * @author Noam Shemesh
 */
@Service("accessAuthenticationProvider")
public class AccessUserPassAuthenticationProvider implements RealmAwareAuthenticationProvider,
        UserPassAuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(AccessUserPassAuthenticationProvider.class);
    public static final String ACCESS_REALM = "access";
    public static final int SC_TOO_MANY_REQUESTS = 429;

    private AccessService accessService;

    private UserGroupStoreService userGroupStoreService;

    private CentralConfigService configService;

    @Autowired
    public AccessUserPassAuthenticationProvider(AccessService accessService,
            UserGroupStoreService userGroupStoreService, CentralConfigService configService) {
        this.accessService = accessService;
        this.userGroupStoreService = userGroupStoreService;
        this.configService = configService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getPrincipal().toString();
        String password = (String) authentication.getCredentials();

        try {
            UserWithGroups userRes = getUserLoggedIn(username, password);
            return new AccessResponseAuthentication(true, simpleUserFromModel(userRes, password));
        } catch (AccessClientException e) {
            // Case we got response from Access server and the user can't authenticate
            if (e instanceof AccessClientHttpException &&
                    shouldHandleHttpException((AccessClientHttpException) e)) {
                handleAccessClientHttpException(authentication, e);
                throw new AuthenticationServiceException(e.getMessage());
            }
            // Case there is no response from Access server
            // Throwing same exception so authentication filter will catch that as server error, not auth error
            throw e;
        }
    }

    private boolean shouldHandleHttpException(AccessClientHttpException e) {
        int statusCode = e.getStatusCode();
        return statusCode < (HttpStatus.SC_NOT_FOUND) || statusCode == SC_TOO_MANY_REQUESTS;
    }

    private void handleAccessClientHttpException(Authentication authentication, AccessClientException e) {
        int statusCode = ((AccessClientHttpException) e).getStatusCode();
        log.debug("{}, Access didn't authenticate user: '{}'", statusCode, authentication.getPrincipal());
        ErrorsModel errorsModel = ((AccessClientHttpException) e).getErrorsModel();
        if (errorsModel != null) {
            Optional<MessageModel> tooManyRequests = isCode(errorsModel,
                    MessageModelCode.CODE_TOO_MANY_REQUESTS);
            Optional<MessageModel> passwordExpired = isCode(errorsModel,
                    MessageModelCode.CODE_PASSWORD_EXPIRED);
            if (tooManyRequests.isPresent()) {
                throw new LockedException(tooManyRequests
                        .map(MessageModel::getMessage).orElse("Too many attempts, please try again later"), e);
            } else if (passwordExpired.isPresent()) {
                throw new CredentialsExpiredException("Password has expired", e);
            }
        }
    }

    private Optional<MessageModel> isCode(ErrorsModel errorsModel, String code) {
        return errorsModel.getErrors().stream().filter(model -> code.equals(model.getCode())).findFirst();
    }

    @Override
    public boolean canUserLogin(String username, String password) {
        try {
            getUserLoggedIn(username, password);
            return true;
        } catch (AccessClientHttpException e) {
            // TODO [NS] Is this really the best way to find out if the user can login?
            // If all the errors are password expiration error it means that the user would've logged in if the password is ok
            long numberOfExpiredPasswordErrors = e.getErrorsModel().getErrors().stream()
                    .filter(model -> model.getCode().equals(MessageModelCode.CODE_PASSWORD_EXPIRED)).count();
            return e.getErrorsModel() != null && e.getErrorsModel().getErrors().size() == numberOfExpiredPasswordErrors;
        }
    }

    private UserWithGroups getUserLoggedIn(String username, String password) {
        LoginRequest model = new LoginRequest().username(username).password(password);
        return accessService.getAccessClient().auth().authenticate(model);
    }

    private SimpleUser simpleUserFromModel(UserBase user, String password) {
        MutableUserInfo userInfo = UserMapper.toArtifactoryUser(user); // No need to decrypt properties as SimpleUser doesn't hold user properties
        userInfo.setPassword(new SaltedPassword(password, null));
        return new SimpleUser(userInfo);
    }

    @Override
    public String getRealm() {
        return ACCESS_REALM;
    }

    @Override
    public void addExternalGroups(String username, Set<UserGroupInfo> groups) {
        // Noop
    }

    @Override
    public boolean userExists(String username) {
        return userGroupStoreService.userExists(username);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }

    @Override
    @Nonnull
    public UserDetails reauthenticateRememberMe(String username) {
        UserInfo user = userGroupStoreService.findUser(username);
        if (user == null) {
            String errorMessage = String.format("The user: '%s' does not exist", username);
            log.debug(errorMessage);
            throw new UsernameNotFoundException(errorMessage);
        }
        boolean lockPolicyEnabled = configService.getDescriptor().getSecurity().getUserLockPolicy().isEnabled();
        if (lockPolicyEnabled && user.isLocked()) {
            String errorMessage = String.format("The user: '%s' is locked", username);
            log.debug(errorMessage);
            throw  new RememberMeAuthenticationException(errorMessage);
        }
        return new SimpleUser(user);
    }
}
