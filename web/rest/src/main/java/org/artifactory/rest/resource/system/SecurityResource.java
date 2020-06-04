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


import org.artifactory.addon.AddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.rest.constant.HaRestConstants;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.access.CreatedTokenInfo;
import org.artifactory.descriptor.security.EncryptionPolicy;
import org.artifactory.descriptor.security.PasswordSettings;
import org.artifactory.rest.ErrorResponse;
import org.artifactory.security.SecurityInfo;
import org.artifactory.security.access.AccessService;
import org.artifactory.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.artifactory.api.rest.constant.SystemRestConstants.PATH_ACCESS_PROXY;

/**
 * @author freds
 */

@RolesAllowed({AuthorizationService.ROLE_ADMIN, HaRestConstants.ROLE_HA})
public class SecurityResource {
    private static final Logger log = LoggerFactory.getLogger(SecurityResource.class);
    private final AddonsManager addonManager;

    private final SecurityService securityService;
    private final CentralConfigService centralConfigService;
    private final HttpServletRequest httpServletRequest;
    private final AccessService accessService;

    public SecurityResource(SecurityService securityService, CentralConfigService service,
            HttpServletRequest httpServletRequest, AddonsManager addonsManager,
            AccessService accessService) {
        this.securityService = securityService;
        centralConfigService = service;
        this.httpServletRequest = httpServletRequest;
        this.addonManager = addonsManager;
        this.accessService = accessService;
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public SecurityInfo getSecurityData() {
        log.warn("Security XML export is deprecated");
        return securityService.getSecurityData();
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_PLAIN)
    @Deprecated
    public String importSecurityDataOverride(@QueryParam("overwrite") boolean overwrite, String securityXml) {
        log.warn("Security XML import is deprecated");
        log.debug("Activating import of new security data: {}", securityXml);
        securityService.importSecurityData(securityXml, overwrite);
        SecurityInfo securityData = securityService.getSecurityData();
        int x = securityData.getUsers().size();
        int y = securityData.getGroups().size();
        int z = securityData.getRepoAcls().size();
        int buildAcls = securityData.getBuildAcls().size();
        return "Import of new Security data (" + x + " users, " + y + " groups, " + z + " acls, buildAcls: " +
                buildAcls + ") succeeded";
    }

    @POST
    @Path("passwordPolicy/{policyName}")
    public void setPasswordPolicy(@PathParam("policyName") String policyName) {
        EncryptionPolicy policy;
        try {
            policy = EncryptionPolicy.valueOf(policyName);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        PasswordSettings passwordSettings = centralConfigService.getDescriptor().getSecurity().getPasswordSettings();
        passwordSettings.setEncryptionPolicy(policy);
    }

    @POST
    @Path("logout")
    public void logout() {
        httpServletRequest.getSession().invalidate();
    }

    @Path(SystemRestConstants.PATH_CERTIFICATES)
    public CertificatesResource getSecurityResource() {
        return new CertificatesResource(addonManager);
    }

    /**
     * Binding a service to authentication provider.
     * Accept all authentication provider information: url, token & certificate.
     *
     * @param authenticationProviderInfo - contains information about the service to be bound with
     * @return bindAuthenticationResult - contains access admin token
     */
    @POST
    @Path("access")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response bindAuthenticationProvider(AuthenticationProviderInfo authenticationProviderInfo) {
        BindAuthenticationResult bindAuthenticationResult = new BindAuthenticationResult();
        BindAuthenticationProviderHelper bindAuthenticationProviderHelper = new BindAuthenticationProviderHelper();
        try {
            bindAuthenticationProviderHelper.validateUserInput(authenticationProviderInfo);
            CreatedTokenInfo createdTokenInfo = bindAuthenticationProviderHelper
                    .createToken(authenticationProviderInfo.getRegistryId());
            bindAuthenticationResult
                    .setStatus(new BindAuthenticationStatusResult("Success"));
            bindAuthenticationResult.setToken(bindAuthenticationProviderHelper.toTokenResponseModel(createdTokenInfo));
            return Response.status(Response.Status.OK).entity(bindAuthenticationResult).build();
        } catch (IllegalArgumentException e) {
            bindAuthenticationResult.setStatus(
                    new BindAuthenticationStatusResult("Failed to execute, Reason: " + e.getMessage()));
            log.debug("Error while trying to authenticate provider, Reason: {}", e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).entity(bindAuthenticationResult).build();
        } catch (Exception e) {
            bindAuthenticationResult.setStatus(
                    new BindAuthenticationStatusResult("Failed to execute, reason: " + e.getMessage()));
            log.error("Error while trying to authenticate provider, Reason: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(bindAuthenticationResult).build();
        }
    }

    /**
     * Pars of specs https://docs.google.com/document/d/1RVZZm9X6bYDKbUDyRR4-uFqFYQfDn1oDz61NZjeKYis under 7.2
     * Returns id and url or Access
     */
    @GET
    @Path("access")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccessInfo() {
        String artifactoryUrl = HttpUtils.getServletContextUrl(httpServletRequest);
        String accessServiceId;
        try {
            accessServiceId = accessService.getAccessClient().system().getAccessServiceId().getFormattedName();
        } catch (IOException e) {
            ErrorResponse errorResponse = getErrorResponse(e);
            return Response.serverError().entity(errorResponse).build();
        }
        String accessServiceUrl = artifactoryUrl + PATH_ACCESS_PROXY;
        return Response.ok().entity(new AuthProviderInfo(accessServiceId, accessServiceUrl)).build();
    }

    private ErrorResponse getErrorResponse(IOException e) {
        log.error("Could not retrieve AccessServiceId. ", e);
        return new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
    }

}
