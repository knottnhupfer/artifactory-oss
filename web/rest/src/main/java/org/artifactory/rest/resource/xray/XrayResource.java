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

package org.artifactory.rest.resource.xray;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.api.rest.constant.HaRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.model.xray.XrayArtifactInfo;
import org.artifactory.rest.common.model.xray.XrayConfigModel;
import org.artifactory.rest.common.model.xray.XrayRepoModel;
import org.artifactory.rest.common.model.xray.XrayScanBuildModel;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.rest.exception.AuthorizationRestException;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.services.ConfigServiceFactory;
import org.artifactory.rest.services.RepoServiceFactory;
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
import java.util.List;

import static org.artifactory.rest.common.model.xray.ArtifactXrayModelHelper.getXrayInfo;
import static org.artifactory.rest.resource.ci.BuildResource.ACCESS_DENIED_MSG;
import static org.artifactory.rest.resource.xray.XrayResourceHelper.toModel;

/**
 * @author Shay Yaakov
 * @author Chen Keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("xray")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class XrayResource extends BaseResource {
    private static final Logger log = LoggerFactory.getLogger(XrayResource.class);

    //Suddenly required by the scan build functionality for no apparent reason, adding this to appease our corporate overlords.
    private static final String NGINX_ACCEL_HEADER = "X-Accel-Buffering";

    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private RepoServiceFactory repoServiceFactory;
    @Autowired
    private ConfigServiceFactory configServiceFactory;
    @Autowired
    private AddonsManager addonsManager;

    @POST
    @Path("index")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response index() {
        return runService(repoServiceFactory.indexXray());
    }

    @POST
    @Path("scanBuild")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.WILDCARD)
    @RolesAllowed({AuthorizationService.ROLE_USER, AuthorizationService.ROLE_ADMIN})
    public Response scanBuild(XrayScanBuildModel xrayScanBuildModel) {
        if (!authorizationService.canReadBuild(xrayScanBuildModel.getBuildName(), xrayScanBuildModel.getBuildNumber())) {
            throw new AuthorizationRestException(ACCESS_DENIED_MSG);
        }
        CloseableHttpResponse scanBuildResponse = null;
        Response response;
        try {
            scanBuildResponse = addonsManager.addonByType(XrayAddon.class).scanBuild(toModel(xrayScanBuildModel));
        } catch (Exception e) {
            log.debug("Error Executing scan build request: ", e);
        }
        if (scanBuildResponse == null || scanBuildResponse.getEntity() == null
                || scanBuildResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            /*  if no stream found return error */
            log.error("Scan summary report for build name {} number {} is not available, check connectivity to Xray",
                    xrayScanBuildModel.getBuildName(), xrayScanBuildModel.getBuildNumber());
            response = Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .entity("Scan summary report is not available")
                    .build();
        } else {
            try {
                /* stream response back to client */
                response = XrayResourceHelper.streamResponse(scanBuildResponse)
                        .header(NGINX_ACCEL_HEADER, "no")
                        .build();
            } catch (Exception e) {
                IOUtils.closeQuietly(scanBuildResponse);
                log.error("Caught error streaming scan build response to client: ", e);
                response = Response
                        .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                        .entity("Error executing scan build request: " + e.getMessage())
                        .build();
            }
        }
        return response;
    }

    @DELETE
    @Path("clearAllIndexTasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response clearAllIndexTasks() {
        return runService(repoServiceFactory.clearAllIndexTasks());
    }

    @GET
    @Path("license")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response license() {
        return runService(repoServiceFactory.getXrayLicense());
    }

    @GET
    @Path("{repoKey}/indexStats")
    public Response getIndexStats(@PathParam("repoKey") String repoKey) {
        return runService(repoServiceFactory.getXrayIndexStats());
    }

    /**
     * @deprecated backward compatibility, use getXrayIndexedRepoRepos instead
     */
    @Deprecated
    @GET
    @Path("repos")
    @RolesAllowed({AuthorizationService.ROLE_USER, AuthorizationService.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfigureReposIndexing() {
        return runService(configServiceFactory.getXrayConfiguredRepos());
    }

    @GET
    @Path("indexRepos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getXrayIndexedRepo() throws Exception {
        return runService(configServiceFactory.getXrayIndexedRepo());
    }

    @PUT
    @Path("indexRepos")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response selectXrayIndexedRepos(List<XrayRepoModel> repos) {
        return runService(configServiceFactory.updateXrayIndexRepos(), repos);
    }

    @GET
    @Path("nonIndexRepos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNoneXrayIndexedRepo() throws Exception {
        return runService(configServiceFactory.getNoneXrayIndexedRepo());
    }

    @POST
    @Path("setAlertIgnored")
    public Response resetRepoBlocks(@QueryParam("path") String path) {
        RepoPath repoPath = RepoPathFactory.create(path);
        try {
            if (addonsManager.addonByType(XrayAddon.class).setAlertIgnored(repoPath)) {
                return Response.ok().entity("Download will be allowed for path " + path).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity("Alert couldn't be ignored. Check logs for more information.").build();
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @POST
    @Path("cleanProperties")
    public Response cleanProperties() {
        try {
            addonsManager.addonByType(XrayAddon.class).cleanXrayPropertiesFromDB();
            return Response.ok().entity("Xray properties cleanup job has been successfully scheduled").build();
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @POST
    @Path("cleanXrayClientCaches")
    public Response cleanCaches() {
        try {
            addonsManager.addonByType(XrayAddon.class).cleanXrayClientCaches();
            return Response.ok().entity("Invalidated all Xray client caches").build();
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @POST
    @Path("allowBlockedArtifactsDownload")
    public Response allowBlockedArtifactsDownload(@QueryParam("allow") Boolean allow) {
        if (allow == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing parameter allow[true|false]").build();
        }
        if (addonsManager.addonByType(XrayAddon.class).updateAllowBlockedArtifactsDownload(allow)) {
            return Response.ok().entity("Xray configuration for allow blocked artifacts download set to: " + allow).build();
        }
        return Response.serverError().entity("Encountered error on config changes. Check logs for more information.").build();
    }

    @POST
    @Path("allowDownloadWhenUnavailable")
    public Response allowDownloadWhenUnavailable(@QueryParam("allow") Boolean allow) {
        if (allow == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing parameter allow[true|false]").build();
        }
        if (addonsManager.addonByType(XrayAddon.class).updateAllowDownloadWhenUnavailable(allow)) {
            return Response.ok().entity("Xray configuration for allow download when Xray is unavailable set to: " + allow).build();
        }
        return Response.serverError().entity("Encountered error on config changes. Check logs for more information.").build();
    }

    @POST
    @Path("setBlockUnscannedArtifactsDownloadTimeout")
    public Response setBlockUnscannedArtifactsDownloadTimeout(@QueryParam("seconds") Integer seconds) {
        if (seconds == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing query parameter 'seconds'").build();
        }
        if (addonsManager.addonByType(XrayAddon.class).setBlockUnscannedArtifactsDownloadTimeout(seconds)) {
            return Response.ok().entity("Xray configuration for block unscanned artifacts download timeout set to: " + seconds).build();
        }
        return Response.serverError().entity("Encountered error on config changes. Check logs for more information.").build();
    }

    @GET
    @Path("artifactInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response artifactInfo(@QueryParam("path") String path) {
        RepoPath repoPath = RepoPathFactory.create(path);
        try {
            XrayArtifactInfo xrayInfo = getXrayInfo(addonsManager.addonByType(XrayAddon.class).getArtifactXrayInfo(repoPath));
            return Response.ok().entity(xrayInfo).build();
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    // Notification endpoint

    /**
     * Used by Xray to notify Artifactory upon a change to repository policies,
     * which will trigger a full update from our side + propagate the notification to other nodes.
     *
     * @param internal In case True: will not propagate to other nodes (means the request came from other Artifactory
     *                 server that got the original request)
     */
    @POST
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, HaRestConstants.ROLE_HA})
    @Path("notifyPolicyChange")
    public Response createPolicyChangeNotification(@QueryParam("internal") boolean internal) {
        log.debug("Received a policy change notification, internal: {}", internal);
        if (addonsManager.addonByType(XrayAddon.class).createPolicyChangeNotification(internal)) {
            return Response.status(Response.Status.CREATED).build();
        }
        return Response.serverError().entity("Encountered error on notification. Check logs for more information.").build();
    }

    //Configuration endpoints

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createXrayConfig(XrayConfigModel xrayConfigModel) {
        return runService(configServiceFactory.createXrayConfig(), xrayConfigModel);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateXrayConfig(XrayConfigModel xrayConfigModel) {
        return runService(configServiceFactory.updateXrayConfig(), xrayConfigModel);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteXrayConfig() {
        return runService(configServiceFactory.deleteXrayConfig());
    }
}
