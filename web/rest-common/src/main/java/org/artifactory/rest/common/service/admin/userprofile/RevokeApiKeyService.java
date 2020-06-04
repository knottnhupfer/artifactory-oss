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

package org.artifactory.rest.common.service.admin.userprofile;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.props.auth.ApiKeyManager;
import org.artifactory.security.props.auth.DockerTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RevokeApiKeyService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(RevokeApiKeyService.class);

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    ApiKeyManager apiKeyManager;

    @Autowired
    DockerTokenManager dockerTokenManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        if (authorizationService.isAnonymous()) {
            return;
        }
        String userName = request.getPathParamByKey("id");
        boolean deleteAll = request.getQueryParamByKey("deleteAll").equals("1");
        String authUserName = authorizationService.currentUsername();
        if (StringUtils.isEmpty(userName) && !deleteAll) {
            if (authorizationService.isApiKeyAuthentication()) {
                setError(response);
                return;
            }
            userName = authUserName;
        }
        boolean isAdmin = authorizationService.isAdmin();
        if (((!StringUtils.isEmpty(userName) && isAdmin) || (userName.equals(authUserName)) && !deleteAll)) {
            // revoke apiKey
            revokeApiKey(response, userName);
        } else {
            if (StringUtils.isEmpty(userName) && isAdmin && deleteAll) {
                // revoke all api keys
                revokeAllApiKeys(response);
            } else {
                response.responseCode(HttpServletResponse.SC_FORBIDDEN);
            }
        }
    }

    private void setError(RestResponse response) {
        response.responseCode(HttpServletResponse.SC_FORBIDDEN);
        response.error("The use of apiKey as authentication is forbidden for this api call");
    }

    /**
     * revoke all api keys
     *
     * @param response - encapsulate data related tto response
     */
    private void revokeAllApiKeys(RestResponse response) {
        boolean revokeSucceeded = apiKeyManager.revokeAllTokens();
        if (revokeSucceeded) {
            dockerTokenManager.revokeAllTokens();
            response.info("All api keys have been successfully revoked");
            if (log.isDebugEnabled()) {
                log.debug("All api keys have been successfully revoked by: '{}'", authorizationService.currentUsername());
            }
        } else {
            log.error("Error revoking all api keys");
            response.error("Error revoking all api keys");
        }
    }

    /**
     * revoke api key for specific user
     *
     * @param response - artifactory rest response
     * @param userName - user name to revoke api
     */
    private void revokeApiKey(RestResponse response, String userName) {
        if (apiKeyManager.revokeToken(userName)) {
            dockerTokenManager.revokeToken(userName);
            response.info("Api key for user: '" + userName + "' has been successfully revoked");
            if (log.isDebugEnabled()) {
                log.debug("Api key for user: '{}' has been successfully revoked by user : '{}'", userName,
                        authorizationService.currentUsername());
            }
        } else {
            log.error("Error revoking api key for user: '{}'", userName);
            response.error("Error revoking api key for user: " + userName);
        }
    }
}
