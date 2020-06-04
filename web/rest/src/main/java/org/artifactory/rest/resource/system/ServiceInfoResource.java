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

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.security.access.AccessService;
import org.artifactory.util.HttpUtils;
import org.jfrog.access.common.ServiceId;
import org.jfrog.common.JsonUtils;
import org.jfrog.common.platform.SystemServiceInfo;
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
import java.time.ZonedDateTime;

/**
 * Resource to get information about Artifactory services.
 * See specs: https://docs.google.com/document/d/1RVZZm9X6bYDKbUDyRR4-uFqFYQfDn1oDz61NZjeKYis
 *
 * @author Dudi Morad
 */
@Component
@Path(SystemRestConstants.SERVICE_INFO)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({AuthorizationService.ROLE_USER, AuthorizationService.ROLE_ADMIN})
public class ServiceInfoResource {

    private AccessService accessService;

    @Context
    private HttpServletRequest servletRequest;
    private JsonUtils jsonUtils = JsonUtils.getInstance();

    @Autowired
    public ServiceInfoResource(AccessService accessService) {
        this.accessService = accessService;
    }

    /**
     * Returns the service information of Artifactory instance.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServiceInfo() {
        String artifactoryUrl = HttpUtils.getServletContextUrl(servletRequest);
        ServiceId serviceId = accessService.getArtifactoryServiceId();
        String serviceType = serviceId.getServiceType();
        String serviceIdFormatted = serviceId.getFormattedName();
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        SystemServiceInfo model = new SystemServiceInfo(serviceIdFormatted, serviceType, artifactoryUrl,
                zonedDateTime.getOffset().getId(), zonedDateTime.getZone().getId());
        return Response.ok().entity(jsonUtils.valueToString(model)).build();
    }

    /**
     * Returns the service registry (mission control) that controls the Artifactory instance.
     * Currently with no implementation until product decisions
     */
    @GET
    @Path(SystemRestConstants.REGISTRY)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRegistryInfo() {
        return Response.ok().entity(new ServiceRegistryInfo(StringUtils.EMPTY, StringUtils.EMPTY)).build();
    }

    /**
     * Binding a service to enterprise+ service registry (mission control).
     * Currently with no implementation until product decisions
     */
    @POST
    @Path(SystemRestConstants.REGISTRY)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response bindServiceRegistry(BindServiceInfo bindServiceInfo) {
        return Response.status(Response.Status.OK).build();
    }
}