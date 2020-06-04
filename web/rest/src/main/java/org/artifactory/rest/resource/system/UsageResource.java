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

import org.apache.http.HttpStatus;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.metrics.exception.MaxSizeExceededException;
import org.artifactory.metrics.model.IntegrationsProductUsage;
import org.artifactory.metrics.providers.features.IntegrationsFeature;
import org.artifactory.metrics.services.CallHomeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This resource accepts usage data from various clients and aggregates it where the
 * {@link org.artifactory.metrics.jobs.CallHomeJob} can transmit it later on.
 *
 * @author shivaramr
 */
@Component
@Path(SystemRestConstants.PATH_ROOT + "/usage")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class UsageResource {
    private static final Logger log = LoggerFactory.getLogger(UsageResource.class);

    @Autowired
    private IntegrationsFeature integrationsFeature;

    @Autowired
    private CallHomeService callHomeService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleCallHomeExtensionApi(IntegrationsProductUsage integrationsProductUsage) {
        log.info("handleCallHomeExtensionApi invoked with input= {}", integrationsProductUsage);
        if (callHomeService.getIsOffline().get()) {
            return Response.accepted().build();
        }
        try {
            integrationsFeature.addUsageData(integrationsProductUsage);
        } catch (MaxSizeExceededException e) {
            //Already logged
            return Response
                    .status(HttpStatus.SC_REQUEST_TOO_LONG)
                    .entity(e.getMessage())
                    .build();
        }
        return Response.ok().build();
    }
}
