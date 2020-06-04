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

package org.artifactory.rest.resource.security;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.SecurityResourceAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.rest.constant.SecurityRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.PasswordSettings;
import org.artifactory.descriptor.security.UserLockPolicy;
import org.artifactory.rest.common.BlockOnConversion;
import org.artifactory.rest.common.exception.RestException;
import org.artifactory.rest.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Set;

/**
 * @author Noam Y. Tenne
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(SecurityRestConstants.PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class SecurityResource {

    private static final String ENTITY_TYPE = "entityType";
    private static final String ENTITY_KEY = "entityKey";
    private static final String INNER_PATH = "{" + ENTITY_TYPE + ": .+}/{" + ENTITY_KEY + ": .+}";

    @Autowired
    AddonsManager addonsManager;

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    SecurityService securityService;

    @Context
    private HttpServletRequest request;

    @Autowired
    private CentralConfigService centralConfigService;


    @GET
    @Path("{" + ENTITY_TYPE + ": .+}")
    @Produces({SecurityRestConstants.MT_USERS, SecurityRestConstants.MT_GROUPS,
            SecurityRestConstants.MT_PERMISSION_TARGETS, MediaType.APPLICATION_JSON})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response getSecurityEntities(@PathParam(ENTITY_TYPE) String entityType) throws UnsupportedEncodingException {
        SecurityResourceAddon securityResourceAddon = addonsManager.addonByType(SecurityResourceAddon.class);
        return securityResourceAddon.getSecurityEntities(request, decodeEntityKey(entityType));
    }

    @GET
    @Path(INNER_PATH)
    @Produces({SecurityRestConstants.MT_USER, SecurityRestConstants.MT_GROUP,
            SecurityRestConstants.MT_PERMISSION_TARGET, MediaType.APPLICATION_JSON})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response getSecurityEntity(@PathParam(ENTITY_TYPE) String entityType,
            @PathParam(ENTITY_KEY) String entityKey,
            @QueryParam("includeUsers") @DefaultValue("false") boolean includeUsers)
            throws UnsupportedEncodingException {
        SecurityResourceAddon securityResourceAddon = addonsManager.addonByType(SecurityResourceAddon.class);
        return securityResourceAddon.getSecurityEntity(entityType, decodeEntityKey(entityKey), includeUsers);
    }

    @DELETE
    @Path(INNER_PATH)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @BlockOnConversion
    public Response deleteSecurityEntity(@PathParam(ENTITY_TYPE) String entityType,
            @PathParam(ENTITY_KEY) String entityKey) throws UnsupportedEncodingException {
        SecurityResourceAddon securityResourceAddon = addonsManager.addonByType(SecurityResourceAddon.class);
        return securityResourceAddon.deleteSecurityEntity(entityType, decodeEntityKey(entityKey));
    }

    @PUT
    @Path(INNER_PATH)
    @Consumes({SecurityRestConstants.MT_USER, SecurityRestConstants.MT_GROUP,
            SecurityRestConstants.MT_PERMISSION_TARGET, MediaType.APPLICATION_JSON})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @BlockOnConversion
    public Response createOrReplaceSecurityEntity(@PathParam(ENTITY_TYPE) String entityType,
            @PathParam(ENTITY_KEY) String entityKey) throws IOException {
        SecurityResourceAddon securityResourceAddon = addonsManager.addonByType(SecurityResourceAddon.class);
        return securityResourceAddon.createOrReplaceSecurityEntity(entityType, decodeEntityKey(entityKey), request);
    }

    @POST
    @Path(INNER_PATH)
    @BlockOnConversion
    @Consumes({SecurityRestConstants.MT_USER, SecurityRestConstants.MT_GROUP,
            SecurityRestConstants.MT_PERMISSION_TARGET, MediaType.APPLICATION_JSON})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response updateSecurityEntity(@PathParam(ENTITY_TYPE) String entityType,
            @PathParam(ENTITY_KEY) String entityKey) throws IOException {
        SecurityResourceAddon securityResourceAddon = addonsManager.addonByType(SecurityResourceAddon.class);
        return securityResourceAddon.updateSecurityEntity(entityType, decodeEntityKey(entityKey), request);

    }

    @GET
    @Path("encryptedPassword")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getEncryptedPassword() {
        if (!securityService.isPasswordEncryptionEnabled()) {
            throw new RestException(HttpStatus.SC_CONFLICT, "Server doesn't support encrypted passwords");
        }

        String encryptedPassword = authorizationService.currentUserEncryptedPassword();
        if (StringUtils.isNotBlank(encryptedPassword)) {
            return Response.ok(encryptedPassword).build();
        }

        throw new NotFoundException(
                String.format("User not found. The user: '%s'", authorizationService.currentUsername()));
    }

    private String decodeEntityKey(String entityKey) throws UnsupportedEncodingException {
        return URLDecoder.decode(entityKey, CharEncoding.UTF_8);
    }

    @PUT
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("userLockPolicy")
    @BlockOnConversion
    public Response updateUserLockPolicy(UserLockPolicy userLockPolicy) {
        if (userLockPolicy.getLoginAttempts() > 100 || userLockPolicy.getLoginAttempts() < 1)
            return Response.status(Response.Status.BAD_REQUEST).entity("LoginAttempts must be between 1 - 100").build();

        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        UserLockPolicy userLockPolicyConfig = mutableDescriptor.getSecurity().getUserLockPolicy();
        if (userLockPolicyConfig == null) {
            mutableDescriptor.getSecurity().setUserLockPolicy(userLockPolicy);
        } else {
            userLockPolicyConfig.setEnabled(userLockPolicy.isEnabled());
            userLockPolicyConfig.setLoginAttempts(userLockPolicy.getLoginAttempts());
        }
        centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
        return Response.ok().entity("UserLockPolicy was successfully updated").build();
    }

    @GET
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("userLockPolicy")
    public UserLockPolicy getUserLockPolicy() {
        return centralConfigService.getDescriptor().getSecurity().getUserLockPolicy();
    }

    @POST
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @Path("unlockUsers/{userName}")
    @BlockOnConversion
    public String unlockUser(@PathParam("userName") String userName) {
        ((UserGroupService)securityService).unlockUser(userName);
        return String.format("The user: '%s' was successfully unlocked", userName);
    }

    @POST
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @Path("unlockAllUsers")
    @BlockOnConversion
    public String unlockAllUsers() {
        ((UserGroupService)securityService).unlockAllUsers();
        return "All users were successfully unlocked";
    }

    @POST
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("unlockUsers")
    @BlockOnConversion
    public String unlockUsers(List<String> users) {
        if(users != null)
            users.parallelStream().forEach(u -> {
                ((UserGroupService)securityService).unlockUser(u);
            });
        return "Specified users were successfully unlocked";
    }

    @GET
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("lockedUsers")
    public Set<String> getAllLockedUsers() {
        return ((UserGroupService)securityService).getLockedUsers();
    }

    @PUT
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("passwordSettings")
    @BlockOnConversion
    public Response updatePasswordPolicy(PasswordSettings userPasswordSettings) {
        if (userPasswordSettings.getExpirationPolicy().getPasswordMaxAge() > 999 || userPasswordSettings.getExpirationPolicy().getPasswordMaxAge() < 1)
            return Response.status(Response.Status.BAD_REQUEST).entity("Password expiration must be between 1 - 999").build();

        MutableCentralConfigDescriptor descriptor = centralConfigService.getMutableDescriptor();
        PasswordSettings originalPasswordSettings = descriptor.getSecurity().getPasswordSettings();

        if (originalPasswordSettings == null) {
            descriptor.getSecurity().setPasswordSettings(userPasswordSettings);
        } else {
            originalPasswordSettings.getExpirationPolicy().setEnabled(userPasswordSettings.getExpirationPolicy().getEnabled());
            originalPasswordSettings.getExpirationPolicy().setPasswordMaxAge(userPasswordSettings.getExpirationPolicy().getPasswordMaxAge());
        }
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
        return Response.ok().entity("PasswordSettings were successfully updated").build();
    }

    @GET
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("passwordSettings")
    public PasswordSettings getPasswordPolicy() {
        return centralConfigService.getDescriptor().getSecurity().getPasswordSettings();
    }
}
