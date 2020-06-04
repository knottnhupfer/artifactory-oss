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

package org.artifactory.ui.rest.service.admin.security.auth.logout;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.handler.ExternalLogoutHandler;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.AuthenticationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LogoutService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(LogoutService.class);

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            Collection<LogoutHandler> logoutHandlers = ContextHelper.get().beansForType(LogoutHandler.class).values();
            if (isInternal(request)) {
                logoutHandlers.removeIf(logoutHandler -> logoutHandler instanceof ExternalLogoutHandler);
            }
            tryToLogoutFromProviders(request, response, logoutHandlers);
        } catch (Exception e) {
            log.debug("failed to perform session logout", e);
        }
    }

    private boolean isInternal(ArtifactoryRestRequest request) {
        boolean isInternal = false;
        String internalParam = request.getQueryParamByKey("internal");
        if (StringUtils.isNotEmpty(internalParam) && internalParam.equalsIgnoreCase("true")) {
            isInternal = true;
        }
        return isInternal;
    }

    /**
     * iterate security providers and try to logout
     *
     * @param artifactoryRequest  - encapsulate data related to request
     * @param artifactoryResponse - encapsulate data require for response
     * @param logoutHandlers      - map of logout handlers
     */
    private void tryToLogoutFromProviders(ArtifactoryRestRequest artifactoryRequest,
            RestResponse artifactoryResponse, Collection<LogoutHandler> logoutHandlers) {
        HttpServletRequest servletRequest = artifactoryRequest.getServletRequest();
        HttpServletResponse servletResponse = artifactoryResponse.getServletResponse();
        Authentication authentication = AuthenticationHelper.getAuthentication();
        // logout from all providers
        for (LogoutHandler logoutHandler : logoutHandlers) {
            logoutHandler.logout(servletRequest, servletResponse, authentication);
        }
    }
}
