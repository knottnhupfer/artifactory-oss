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

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.model.userprofile.UserProfileModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.props.auth.ApiKeyManager;
import org.artifactory.security.props.auth.model.TokenKeyValue;
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
public class CreateApiKeyService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(CreateApiKeyService.class);

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    ApiKeyManager apiKeyManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {

        if (authorizationService.isAnonymous()) {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String userName = authorizationService.currentUsername();
        TokenKeyValue token = apiKeyManager.getToken(userName);
        if (token != null) {
            log.error("Api key already exists for user: '{}'", userName);
            response.error("Api key already exists for user: " + userName);
            return;
        }
        token = apiKeyManager.createToken(userName);
        if (token == null) {
            log.error("Error while generating api key for user: '{}'", userName);
            response.error("Failed to create api key for user: " + userName);
            return;
        }
        log.debug("The user: '{}' successfully created api key", userName);
        response.responseCode(HttpServletResponse.SC_CREATED);

        UserProfileModel userProfileModel = new UserProfileModel(token.getToken());
        response.iModel(userProfileModel);
    }
}
