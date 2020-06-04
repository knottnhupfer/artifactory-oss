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

package org.artifactory.rest.resource.plugin;

import com.google.common.collect.Maps;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.plugin.ResponseCtx;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.rest.constant.HaRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.io.SimpleResourceStreamHandle;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.rest.ErrorResponse;
import org.artifactory.util.KeyValueList;
import org.artifactory.util.StringInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static org.artifactory.api.rest.constant.PluginRestConstants.*;

/**
 * A resource for plugin execution
 *
 * @author Tomer Cohen
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Path(PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_USER, AuthorizationService.ROLE_ADMIN})
public class PluginsResource {

    private static final Logger log = LoggerFactory.getLogger(PluginsResource.class);


    @Autowired
    AddonsManager addonsManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPluginInfo() {
        return getPluginInfo(null);
    }

    @GET
    @Path("{pluginType: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPluginInfo(@Nullable @PathParam("pluginType") String pluginType) {
        return addonsManager.addonByType(RestAddon.class).getUserPluginInfo(pluginType);
    }

    @POST
    @Consumes(WILDCARD)
    @Path(PATH_EXECUTE + "/{executionName: .+}")
    @Produces(TEXT_PLAIN)
    public Response execute(@Context Request request,
            InputStream body,
            @PathParam("executionName") String executionName,
            @QueryParam(PARAM_PARAMS) KeyValueList paramsList,
            @QueryParam(PARAM_ASYNC) int async) throws Exception {
        Map<String, List<String>> params =
                paramsList != null ? paramsList.toStringMap() : Maps.<String, List<String>>newHashMap();
        try (ResourceStreamHandle handle = new SimpleResourceStreamHandle(body)) {
            ResponseCtx responseCtx =
                    addonsManager.addonByType(RestAddon.class).runPluginExecution(executionName, request.getMethod(),
                            params,
                            handle, async == 1);
            if (async == 1) {
                //Just return accepted (202)
                return Response.status(HttpStatus.SC_ACCEPTED).build();
            } else {
                return responseFromResponseCtx(responseCtx);
            }
        }
    }

    @PUT
    @Consumes(WILDCARD)
    @Path(PATH_EXECUTE + "/{executionName: .+}")
    @Produces(TEXT_PLAIN)
    public Response executePut(@Context Request request,
            InputStream body,
            @PathParam("executionName") String executionName,
            @QueryParam(PARAM_PARAMS) KeyValueList paramsList,
            @QueryParam(PARAM_ASYNC) int async) throws Exception {
        return execute(request, body, executionName, paramsList, async);
    }

    @GET
    @Path(PATH_EXECUTE + "/{executionName: .+}")
    @Produces(TEXT_PLAIN)
    public Response execute(@Context Request request, @PathParam("executionName") String executionName,
            @QueryParam(PARAM_PARAMS) KeyValueList paramsList,
            @QueryParam(PARAM_ASYNC) int async) throws Exception {
        Map<String, List<String>> params =
                paramsList != null ? paramsList.toStringMap() : Maps.<String, List<String>>newHashMap();
        ResponseCtx responseCtx = addonsManager.addonByType(RestAddon.class).runPluginExecution(executionName,
                request.getMethod(), params, null, async == 1);
        if (async == 1) {
            //Just return accepted (202)
            return Response.status(HttpStatus.SC_ACCEPTED).build();
        } else {
            return responseFromResponseCtx(responseCtx);
        }
    }

    @DELETE
    @Path(PATH_EXECUTE + "/{executionName: .+}")
    @Produces(TEXT_PLAIN)
    public Response executeDelete(@Context Request request, @PathParam("executionName") String executionName,
            @QueryParam(PARAM_PARAMS) KeyValueList paramsList,
            @QueryParam(PARAM_ASYNC) int async) throws Exception {
        return execute(request, executionName, paramsList, async);
    }

    @GET
    @Path(PATH_STAGING + "/{strategyName: .+}")
    @Produces({MT_BUILD_STAGING_STRATEGY, MediaType.APPLICATION_JSON})
    public Response getBuildStagingStrategy(
            @PathParam("strategyName") String strategyName,
            @QueryParam("buildName") String buildName,
            @QueryParam(PARAM_PARAMS) KeyValueList paramsList) {
        Map<String, List<String>> params =
                paramsList != null ? paramsList.toStringMap() : Maps.<String, List<String>>newHashMap();
        return addonsManager.addonByType(RestAddon.class).getStagingStrategy(strategyName, buildName, params);
    }

    @POST
    @Path(PATH_PROMOTE + "/{promotionName: .+}/{buildName: .+}/{buildNumber: .+}")
    @Produces(TEXT_PLAIN)
    public Response promote(
            @PathParam("promotionName") String promotionName,
            @PathParam("buildName") String buildName,
            @PathParam("buildNumber") String buildNumber,
            @QueryParam(PARAM_PARAMS) KeyValueList paramsList) {
        Map<String, List<String>> params =
                paramsList != null ? paramsList.toStringMap() : Maps.<String, List<String>>newHashMap();
        ResponseCtx responseCtx = addonsManager.addonByType(RestAddon.class).promoteBuildPlugin(promotionName, buildName,
                buildNumber, params);
        return responseFromResponseCtx(responseCtx);
    }

    @PUT
    @Path("{scriptName: .+}")
    @Consumes({"text/x-groovy", TEXT_PLAIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deploy(Reader pluginScript, @PathParam("scriptName") String scriptName) {
        ResponseCtx responseCtx = addonsManager.addonByType(RestAddon.class).deployPlugin(pluginScript, scriptName);
        if (responseCtx.getStatus() >= 400) {
            ErrorResponse errorResponse = new ErrorResponse(responseCtx.getStatus(), responseCtx.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(errorResponse).build();
        } else {
            return responseFromResponseCtx(responseCtx);
        }
    }

    @POST
    @Path("reload")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, HaRestConstants.ROLE_HA})
    public Response reload() {
        try {
            ResponseCtx responseCtx = addonsManager.addonByType(RestAddon.class).reloadPlugins();
            return Response.status(responseCtx.getStatus())
                    // ugly hack to force text response (to overcome our default in org.artifactory.rest.common.RestErrorResponseFilter
                    .entity(new StringInputStream(responseCtx.getMessage()))
                    .type(MediaType.TEXT_PLAIN).build();
        } catch (Exception e) {
            log.error("Failed to reload plugins", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * @param pluginName The plugin name
     * @return The content of the specified user plugin by its name (with or without the .groovy extension).
     */
    @GET
    @Path("download" + "/{pluginName: .+}")
    @Produces("text/x-groovy-source")
    @RolesAllowed(AuthorizationService.ROLE_ADMIN)
    public Response downloadPlugin(@Nullable @PathParam("pluginName") String pluginName) {
        ResponseCtx responseCtx = addonsManager.addonByType(RestAddon.class).downloadPlugin(pluginName);
        return responseFromResponseCtx(responseCtx);
    }

    private Response responseFromResponseCtx(ResponseCtx responseCtx) {
        Response.ResponseBuilder builder;
        int status = responseCtx.getStatus();
        if (status != ResponseCtx.UNSET_STATUS) {
            builder = Response.status(status);
        } else {
            builder = Response.ok();
        }

        if (responseCtx.getMessage() != null) {
            builder.entity(responseCtx.getMessage());
        } else if (responseCtx.getInputStream() != null) {
            builder.entity(responseCtx.getInputStream());
        }
        return builder.build();
    }
}
