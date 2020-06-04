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

package org.artifactory.rest.common.exception.mapper;

import org.apache.http.HttpStatus;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.ErrorResponse;
import org.artifactory.util.UiRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Intercepts and maps different exceptions thrown by the REST API.
 * <p/>
 * <strong>NOTE!</strong> Candidates exceptions <b>MUST NOT</b> have an entity, otherwise they will not get intercepted.
 *
 * @author Shay Yaakov
 */
public abstract class ArtifactoryRestExceptionMapperBase<T extends RuntimeException> implements ExceptionMapper<T> {

    @Autowired
    private CentralConfigService centralConfig;

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    private BasicAuthenticationEntryPoint authenticationEntryPoint;

    @Context
    UriInfo uriInfo;

    @Context
    HttpServletRequest servletRequest;


    @Override
    public Response toResponse(T exception) {
        Response jerseyResponse = getExceptionResponse(exception);
        if (isForbiddenException(exception)) {
            return interceptForbiddenStatus(jerseyResponse);
        } else if (isUnauthorizedException(exception)) {
            return interceptUnauthorizedStatus(jerseyResponse);
        }
        return jerseyResponse;
    }

    /**
     * Handle forbidden response (403) by verifying if anonymous access is globally allowed, if so we need to send
     * an unauthorized (401) response with basic authentication challenge.
     * If the global hide unauthorized resources is set, the result will be not found (404).
     *
     * @param jerseyResponse The original response, will be returned in there is no need for a conversion.
     */
    private Response interceptForbiddenStatus(Response jerseyResponse) {
        if (authorizationService.isAnonAccessEnabled() && authorizationService.isAnonymous()) {
            if (centralConfig.getDescriptor().getSecurity().isHideUnauthorizedResources()) {
                return Response.status(HttpStatus.SC_NOT_FOUND).build();
            }

            return createUnauthorizedResponseWithChallenge();
        }

        return jerseyResponse;
    }

    /**
     * Handle unauthorized response (401) by adding it with a basic authentication challenge in case it's missing
     * (WWW-Authenticate header with Artifactory realm).
     */
    private Response interceptUnauthorizedStatus(Response jerseyResponse) {
        MultivaluedMap<String, Object> headers = jerseyResponse.getMetadata();
        if (headers != null && headers.containsKey("WWW-Authenticate")) {
            return jerseyResponse;
        }
        return createUnauthorizedResponseWithChallenge();
    }

    private Response createUnauthorizedResponseWithChallenge() {
        Response.ResponseBuilder responseBuilder = Response.status(HttpStatus.SC_UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"));
        // ui related request do not require chanllange message
        if (!UiRequestUtils.isUiRestRequest(servletRequest)) {
            responseBuilder.header("WWW-Authenticate", "Basic realm=\"" + authenticationEntryPoint.getRealmName() + "\"");
        }
        return responseBuilder.build();
    }

    abstract boolean isForbiddenException(T exception);
    abstract boolean isUnauthorizedException(T exception);
    abstract Response getExceptionResponse(T exception);
}
