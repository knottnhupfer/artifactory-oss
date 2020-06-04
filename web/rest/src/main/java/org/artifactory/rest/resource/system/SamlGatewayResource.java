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
package org.artifactory.rest.resource.system;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.sso.saml.SamlException;
import org.artifactory.addon.sso.saml.SamlHandler;
import org.artifactory.addon.sso.saml.SamlSsoAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.access.GenericTokenSpec;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.security.sso.SamlSettings;
import org.artifactory.rest.ErrorResponse;
import org.artifactory.security.AuthenticationHelper;
import org.artifactory.security.access.AccessService;
import org.artifactory.util.HttpUtils;
import org.jfrog.access.common.ServiceId;
import org.jfrog.access.common.SubjectFQN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.artifactory.api.rest.constant.SecurityRestConstants.RETURN_URL_ACCESS_EXTENSION_PARAM;
import static org.artifactory.api.rest.constant.SystemRestConstants.PATH_GATEWAY;

/**
 * Enabling Authenticating with SAML for external services.
 * login - generate the saml login request url with the return url of the service as token.
 * logout - executing the logout, doing redirect for the saml logout request.
 * @deprecated Use new OpenID connect flow
 * @author Tamir Hadad
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(PATH_GATEWAY + "/saml")
@Deprecated
public class SamlGatewayResource {
    private static final Logger log = LoggerFactory.getLogger(SamlGatewayResource.class);

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    @Autowired
    private AccessService accessService;

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private AddonsManager addonsManager;

    private final String SAML_LOGOUT_SUFFIX = "/api/system/gateway/saml/logout";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(AuthorizationService.ROLE_ADMIN)
    public Response getSamlInfo(@QueryParam("service_url") String serviceUrl) {
        SamlSsoAddon samlSsoAddon = addonsManager.addonByType(SamlSsoAddon.class);
        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
        SamlSettings samlSettings = descriptor.getSecurity().getSamlSettings();
        SamlInfoResponse info = new SamlInfoResponse();
        String samlLinkIfEnable = samlSettings == null ? "" : samlSsoAddon.getSamlLoginIdentityProviderUrl(request, null);
        if (StringUtils.isEmpty(samlLinkIfEnable)) {
            log.debug("SAML link was not found for request '{}' and service_url param '{}'", request.getRequestURI(),
                    serviceUrl);
            info.setEnabled(false);
            return Response.ok(info).build();
        }
        boolean isEnable = samlSettings.isEnableIntegration();
        info.setEnabled(isEnable);
        if (isEnable) {
            try {
                String subject = new SubjectFQN(ServiceId.generateUniqueId("saml-auth")).toString();
                // This token is for encrypting the return url and avoid csrf attacks while redirecting the response at the SamlHandlerImpl
                String token = accessService.createNoPermissionToken(
                        ImmutableList.of("dummy"),
                        GenericTokenSpec.create(subject)
                                .expiresIn(60L),
                        JsonNodeFactory.instance.objectNode().put(RETURN_URL_ACCESS_EXTENSION_PARAM, serviceUrl).toString()).getTokenValue();
                // RelayState is a query param which is guaranteed to echo back by the sso provider
                samlLinkIfEnable = new URIBuilder(samlLinkIfEnable).addParameter("RelayState", token).build()
                        .toString();
            } catch (URISyntaxException e) {
                log.debug("SAML link is not valid: {}", e.getMessage());
                log.debug("SAML link is not valid", e);
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                                e.getMessage()))
                        .build();
            }
            info.setLoginUrl(samlLinkIfEnable);
            info.setLogoutUrl(HttpUtils.getServletContextUrl(request) + SAML_LOGOUT_SUFFIX);
        }
        return Response.ok(info).build();
    }

    /**
     * We must use GET because this endpoint is accessible using redirect.
     */
    @GET
    @Path("logout")
    public Response samlLogout() {
        SamlSsoAddon samlSsoAddon = addonsManager.addonByType(SamlSsoAddon.class);
        if (!samlSsoAddon.isSamlAuthentication()) {
            Authentication auth = AuthenticationHelper.getAuthentication();
            log.debug("The user: '{}' not Authenticated using SAML.", auth != null ? auth.getPrincipal() : "unknown");
            CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
            SamlSettings samlSettings = descriptor.getSecurity().getSamlSettings();
            boolean isEnable = samlSettings.isEnableIntegration();
            try {
                if (isEnable) {
                    response.sendRedirect(samlSettings.getLogoutUrl());
                } else {
                    response.sendRedirect(HttpUtils.getServletContextUrl(request));
                }
            } catch (IOException e) {
                return handleLogoutRedirectError(e);
            }
        } else {
            SamlHandler samlHandler = ContextHelper.get().beanForType(SamlHandler.class);
            try {
                response.sendRedirect(samlHandler.generateSamlLogoutRedirectUrl(request, response));
            } catch (IOException | SamlException e) {
                return handleLogoutRedirectError(e);
            }
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    private Response handleLogoutRedirectError(Exception e) {
        log.error("Failed to generate SAML logout url.", e.getMessage());
        log.debug("Failed to generate SAML logout url.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage()))
                .build();
    }
}
