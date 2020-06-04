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

package org.artifactory.ui.rest.service.admin.security.auth.login;

import org.apache.http.HttpStatus;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.AccessLogger;
import org.artifactory.security.AuthenticationHelper;
import org.artifactory.security.exceptions.LoginDisabledException;
import org.artifactory.ui.rest.model.admin.security.login.UserLogin;
import org.artifactory.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletResponse;

/**
 * Provides login services
 *
 * @author Michael Pasternak
 */
public abstract class AbstractLoginService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(AbstractLoginService.class);

    @Autowired
    protected SecurityService securityService;

    @Autowired
    protected UserGroupService userGroupService;


    /**
     * Performs login administration
     *
     * @param request  - encapsulate all data require for request processing
     * @param response - encapsulate all data require from response
     */
    @Override
    public final void execute(ArtifactoryRestRequest request, RestResponse response) {
        UserLogin userLogin = (UserLogin) request.getImodel();
        long sessionAccessTime = HttpUtils.getSessionAccessTime(request.getServletRequest());
        try {
            // makes sure that user is not locked
            securityService.ensureUserIsNotLocked(userLogin.getUser());

            // delay login if necessary
            securityService.ensureLoginShouldNotBeDelayed(userLogin.getUser(),sessionAccessTime);

            // memorise user last access time
            securityService.updateUserLastAccess(
                    userLogin.getUser(),
                    HttpUtils.getRemoteClientAddress(request.getServletRequest()),
                    sessionAccessTime
            );

            doExecute(request, response);

            if(!response.isFailed()) {
                log.debug("The user: '{}' has logged in successfully", userLogin.getUser());
                // reset any previously registered incorrect login attempts
                securityService.interceptLoginSuccess(userLogin.getUser());
            }
        } catch (LockedException | LoginDisabledException e) {
            log.debug("{}, cause: {}", e.getMessage(), e);
            response.error(e.getMessage());
            response.responseCode(HttpServletResponse.SC_FORBIDDEN);
        } catch (CredentialsExpiredException e) {
            response.error(e.getMessage()).responseCode(HttpStatus.SC_FORBIDDEN);
            response.iModel(new CredentialsExpiredFailedLoginResponse(userGroupService.findUser(userLogin.getUser()).isUpdatableProfile()));
            return;
        } catch (AuthenticationException e) {
            log.debug("Username or password is incorrect, cause: ", e);
            Authentication authentication = AuthenticationHelper.getAuthentication();
            AccessLogger.loginDenied(authentication, userLogin.getUser());
            log.debug("Triggering interceptLoginFailure for user: '{}'", userLogin.getUser());
            // register incorrect login attempt and lock user (if enabled)
            securityService.interceptLoginFailure(
                    userLogin.getUser(),
                    HttpUtils.getSessionAccessTime(request.getServletRequest())
            );

            response.error("Username or password is incorrect");
            response.responseCode(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * Performs login
     *
     * @param request  - encapsulate all data require for request processing
     * @param response - encapsulate all data require from response
     */
    abstract void doExecute(ArtifactoryRestRequest request, RestResponse response);
}
