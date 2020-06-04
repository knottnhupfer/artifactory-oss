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

package org.artifactory.ui.rest.service.admin.security.auth.forgotpassword;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.ResetPasswordException;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.login.UserLogin;
import org.artifactory.util.HttpUtils;
import org.jfrog.client.util.PathUtils;
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
public class ForgotPasswordService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(ForgotPasswordService.class);

    @Autowired
    UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        UserLogin userLogin = (UserLogin) request.getImodel();
        String resetPasswordUrl = getResetPasswordPageUrl();
        String username = userLogin.getUser();
        //Check if username is valid
        if (StringUtils.isEmpty(username)) {
            response.error("User is required");
            response.responseCode(400);
            return;
        }
        // rest user password
        resetPassword(request, response, username, resetPasswordUrl);
    }

    /**
     * reset user password
     *
     * @param artifactoryRequest  - encapsulate data require for request
     * @param artifactoryResponse - encapsulate data related to response
     * @param username            - username
     */
    private void resetPassword(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse,
            String username, String resetPasswordUrl) {
        // if in aol mode then have to go to the dashboard to reset password
        String remoteAddress = HttpUtils.getRemoteClientAddress(artifactoryRequest.getServletRequest());
        try {
            String status = userGroupService.resetPassword(username, remoteAddress, resetPasswordUrl);
            artifactoryResponse.info(status);
        } catch (ResetPasswordException e) {
            log.warn("Error while resetting password for user: '{}', requested from address '{}'. {}", username,
                    remoteAddress, e.getMessage());
            log.debug("Error while resetting password for user: '{}', requested from address '{}'. {}", username,
                    remoteAddress, e);
            artifactoryResponse.error(e.getMessage());
            artifactoryResponse.responseCode(HttpStatus.SC_FORBIDDEN);
        } catch (Exception e) {
            log.error("Error while resetting password for user: '{}', requested from address '{}'. {}", username,
                    remoteAddress, e);
            artifactoryResponse.error(e.getMessage());
        }
    }

    /**
     * Get the bookmarkable URL of the reset password page
     *
     * @return String - URL to reset password page
     */
    private String getResetPasswordPageUrl() {
        CoreAddons addon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(CoreAddons.class);
        String resetPasswordUrl = addon.getMailConfigArtifactoryUrl();
        // In case we have no url at all, just return relative url, better than nothing.
        if (resetPasswordUrl == null) {
            log.warn("No URL found for password expiration notification");
            resetPasswordUrl = "";
        }
        return PathUtils.addTrailingSlash(resetPasswordUrl) + "#/resetpassword";
    }
}
