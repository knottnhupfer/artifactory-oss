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

package org.artifactory.ui.rest.resource.admin.advanced.sumologic;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.sumologic.SumoLogicConfigDescriptor;
import org.artifactory.logging.sumologic.SumoLogicException;
import org.artifactory.logging.sumologic.SumoLogicService;
import org.artifactory.rest.common.model.sumologic.SumoLogicModel;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.advanced.AdvancedServiceFactory;
import org.artifactory.util.MaskedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.stream.Stream;

import static org.jfrog.common.ArgUtils.requireSatisfies;

/**
 * @author Shay Yaakov
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("sumologic")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SumoLogicResource extends BaseResource {
    private static final Logger log = LoggerFactory.getLogger(SumoLogicResource.class);

    @Autowired
    private AdvancedServiceFactory factory;

    @Autowired
    private SumoLogicService sumoLogicService;

    @Autowired
    private CentralConfigService centralConfigService;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSumoLogicConfig(SumoLogicModel sumoLogicModel) {
        return runService(factory.updateSumoLogicConfigService(), sumoLogicModel);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSumoLogicConfig() {
        return runService(factory.getSumoLogicConfigService());
    }

    @GET
    @Path("auth_callback/{username}/{setup}")
    public Response callbackFromSumoLogicAuth(@PathParam("username") String username, @PathParam("setup") String setup,
            @QueryParam("code") String code, @QueryParam("base_uri") String baseUri,
            @QueryParam("error") String error, @QueryParam("error_description") String errorDescription) {
        try {
            throwOnError(StringUtils.isNotBlank(errorDescription) ? errorDescription : error);
            requireSatisfies(username, StringUtils::isNotBlank, "username is required");
            requireSatisfies(setup, SetupType::contains, "unexpected setup type: " + setup);
            requireSatisfies(code, StringUtils::isNotBlank, "code is required");
            requireSatisfies(baseUri, StringUtils::isNotBlank, "base_uri is required");

            String accessToken = sumoLogicService.createToken(username, code, baseUri);

            SetupType setupType = SetupType.valueOf(setup);
            boolean newSetup;
            switch (setupType) {
                case new_app: newSetup = true; break;
                case existing_app: newSetup = false; break;
                default: // Should not get here because of the validation above, but still...
                    throw new IllegalArgumentException("Unexpected setup type: " + setupType);
            }
            sumoLogicService.setupApplication(username, baseUri, newSetup);

            SumoLogicConfigDescriptor sumoLogicConfig = centralConfigService.getDescriptor().getSumoLogicConfig();
            String uri = sumoLogicConfig.getDashboardUrl() + "?access_token=" + accessToken;
            log.debug("Redirecting to dashboard: {}?access_token={}", sumoLogicConfig.getDashboardUrl(), MaskedValue.of(accessToken));
            return Response.seeOther(new URI(uri)).build();
        } catch (IllegalArgumentException e) {
            String msg = "Error handling authorization callback request from Sumo Logic: " + e.getMessage();
            log.error(msg, e);
            return Response.status(400).entity(msg).build();
        } catch (SumoLogicException e) {
            String msg = "Error handling authorization callback request from Sumo Logic: " + e.getMessage();
            log.error(msg, e);
            return Response.status(e.getRelaxedStatus()).entity(msg).build();
        } catch (Exception e) {
            String msg = "Error handling authorization callback request from Sumo Logic: " + e.getMessage();
            log.error(msg, e);
            return Response.status(500).entity(msg).build();
        }
    }

    private void throwOnError(String error) {
        if (error != null) {
            int status = "invalid_client_id".equals(error) ? 401 : 400;
            throw new SumoLogicException(error, status);
        }
    }

    private enum SetupType {
        new_app, existing_app;

        static boolean contains(String value) {
            return Stream.of(values()).anyMatch(setup -> setup.name().equals(value));
        }
    }

    @POST
    @Path("refreshToken")
    public Response refreshToken() {
        return runService(factory.refreshSumoLogicTokenService());
    }

    @POST
    @Path("registerSumoLogicApplication")
    public Response registerSumoLogicApplicationService() {
        return runService(factory.registerSumoLogicApplicationService());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("reset")
    public Response resetSumoLogicApplication() {
        return runService(factory.resetSumoLogicApplicationService());
    }

    @POST
    @Path("updateProxy")
    public Response updateSumoLogicProxyService() {
        return runService(factory.updateSumoLogicProxyService());
    }
}
