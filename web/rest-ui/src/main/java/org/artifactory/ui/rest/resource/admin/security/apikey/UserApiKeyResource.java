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

package org.artifactory.ui.rest.resource.admin.security.apikey;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.BlockOnConversion;
import org.artifactory.rest.common.model.userprofile.UserProfileModel;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
import org.artifactory.ui.rest.service.admin.security.user.userprofile.UserProfileHelperService;
import org.jfrog.common.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.Charset;
import java.util.function.Supplier;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * @author Chen Keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Path("userApiKey{id:(/[^/]+?)?}")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UserApiKeyResource extends BaseResource {

    private static final String BASIC_AUTH = "Basic";

    private static final String BAD_CREDENTIALS_ERROR = "Bad credentials";

    protected SecurityServiceFactory securityFactory;

    protected UserProfileHelperService userProfileHelperService;

    protected AuthorizationService authorizationService;

    @Context
    private HttpServletRequest request;

    @Autowired
    public UserApiKeyResource(SecurityServiceFactory securityFactory,
            UserProfileHelperService userProfileHelperService,
            AuthorizationService authorizationService) {
        this.securityFactory = securityFactory;
        this.userProfileHelperService = userProfileHelperService;
        this.authorizationService = authorizationService;
    }

    @HEAD
    @Produces(MediaType.APPLICATION_JSON)
    public Response isExist() {
        Response response = runService(securityFactory.getApiKey());
        Object entity = response.getEntity();
        if (entity == null) {
            return Response.status(HttpStatus.SC_NOT_FOUND).build();
        }
        UserProfileModel userProfileModel = JsonUtils.getInstance()
                .readValue(entity.toString(), UserProfileModel.class);
        if (StringUtils.isBlank(userProfileModel.getApiKey())) {
            return Response.status(HttpStatus.SC_NOT_FOUND).build();
        }
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiKey() {
        return runServiceWrapper(() -> runService(securityFactory.getApiKey()));
    }

    @DELETE
    @BlockOnConversion
    @Produces(MediaType.APPLICATION_JSON)
    public Response revokeApiKey() {
        return runService(securityFactory.revokeApiKey());
    }

    @PUT
    @BlockOnConversion
    @Produces(MediaType.APPLICATION_JSON)
    public Response regenerateApiKey() {
        return runServiceWrapper(() -> runService(securityFactory.regenerateApiKey()));
    }

    @POST
    @BlockOnConversion
    @Produces(MediaType.APPLICATION_JSON)
    public Response createApiKey() {
        return runServiceWrapper(() -> runService(securityFactory.createApiKey()));
    }

    String isAllowApiKeyAccess() {
        UserInfo userInfo = userProfileHelperService.loadUserInfo();
        String errMsg = "";
        // requireProfileUnlock returns false if authentication method such as SAML , SSO are used.
        // in those cases we skip password verification.
        if (!authorizationService.requireProfileUnlock()) {
            return errMsg;
        }
        if (userInfo.isAnonymous()) {
            errMsg = "Unable to unlock settings for anonymous user";
        }
        String userPassword = getUserPasswordFromHeader();
        if (isBlank(userPassword)) {
            errMsg = BAD_CREDENTIALS_ERROR;
        }
        if (!userProfileHelperService.authenticate(userInfo, userPassword)) {
            errMsg = BAD_CREDENTIALS_ERROR;
        }
        return errMsg;
    }

    @Nullable
     String getUserPasswordFromHeader() {
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith(BASIC_AUTH)) {
            String credentials = authorization.substring(BASIC_AUTH.length()).trim();
            String decodedCredentials = new String(Base64.decode(credentials.getBytes()), Charset.forName("UTF-8"));
            String[] values = StringUtils.split(decodedCredentials, ":", 2);
            if (values.length != 2) {
                return null;
            } else {
                return values[1];
            }
        }
        return null;
    }

    private Response runServiceWrapper(Supplier<Response> serviceRunnerFunction) {
        String errorMsg = isAllowApiKeyAccess();
        if (isBlank(errorMsg)) {
            return serviceRunnerFunction.get();
        }
        return artifactoryResponse.responseCode(HttpStatus.SC_UNAUTHORIZED).error(errorMsg).buildResponse();
    }
}
