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

package org.artifactory.rest.common;


import org.artifactory.addon.AddonsManager;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.ErrorResponse;
import org.artifactory.rest.util.AuthUtils;
import org.artifactory.security.access.AccessService;
import org.artifactory.storage.StorageService;
import org.artifactory.storage.fs.service.ConfigsService;
import org.glassfish.jersey.server.ContainerResponse;
import org.jfrog.client.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


/**
 * Intercepts all Jersey client error responses (status code >= 400) and sends the response
 * as a JSON object. Also adds a response entity in case it was not specified.
 *
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class RestResponseFilter implements ContainerResponseFilter {

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private StorageService storageService;

    @Autowired
    private AuthorizationService authenticationService;

    @Autowired
    private ConfigsService configsService;

    @Autowired
    private AccessService accessService;

    private GlobalMessageProvider messageProvider;

    public RestResponseFilter() {
        messageProvider = new GlobalMessageProvider();
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        int status = response.getStatus();
        // add message to response headers
        String baseUrl = PathUtils.trimTrailingSlashes(request.getUriInfo().getBaseUri().getPath());
        if(baseUrl != null && baseUrl.endsWith("ui")) {
            messageProvider.decorateWithGlobalMessages(
                    response, addonsManager, storageService, authenticationService, configsService, accessService);
        }
        AuthUtils.addSessionStatusToHeaders(response, uriInfo, this.request);
        if (status >= 400) {
            Object entity = response.getEntity();
            if (entity == null) {
                ErrorResponse errorResponse = new ErrorResponse(status, response.getStatusInfo().getReasonPhrase());
                createJsonErrorResponse((ContainerResponse) response, errorResponse);
            } else if (entity instanceof String && !MediaType.APPLICATION_JSON_TYPE.equals(response.getMediaType())) {
                ErrorResponse errorResponse = new ErrorResponse(status, (String) entity);
                createJsonErrorResponse((ContainerResponse) response, errorResponse);
            }
        }
    }
    private void createJsonErrorResponse(ContainerResponse response, ErrorResponse errorResponse) {
        response.setEntity(errorResponse);
        response.setMediaType(MediaType.APPLICATION_JSON_TYPE);
    }
}
