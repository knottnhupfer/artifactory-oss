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
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.support.ArtifactorySupportBundleConfig;
import org.artifactory.addon.support.SupportAddon;
import org.artifactory.api.rest.constant.HaRestConstants;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.download.FolderDownloadException;
import org.artifactory.rest.exception.MissingRestAddonException;
import org.artifactory.rest.resource.support.SupportResource;
import org.artifactory.rest.response.JerseyArtifactoryResponse;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.security.access.AccessService;
import org.jfrog.support.common.core.exceptions.BundleGenerationException;
import org.jfrog.support.rest.model.manifest.NodeManifest;
import org.jfrog.support.rest.model.manifest.NodeManifestBundleInfo;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Objects;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.artifactory.api.rest.constant.SystemRestConstants.PATH_SUPPORT_BUNDLE;
import static org.artifactory.api.rest.constant.SystemRestConstants.PATH_SUPPORT_BUNDLES;
import static org.artifactory.support.core.bundle.SupportBundleService.SUPPORT_BUNDLE_TIMESTAMP_PATTERN;
import static org.artifactory.support.manifest.NodeManifestFactory.newNodeManifest;
import static org.artifactory.util.HttpUtils.getServletContextUrl;

/**
 * @author Ori Dar
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(SystemRestConstants.PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, HaRestConstants.ROLE_HA})
public class SupportBundleResource {

    private static final Logger log = LoggerFactory.getLogger(SupportResource.class);

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private AccessService accessService;

    @Context
    private HttpServletRequest httpServletRequest;

    @POST
    @Path(PATH_SUPPORT_BUNDLE)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateServiceBundle(ArtifactorySupportBundleConfig bundleConfig) {
        if (isEmpty(bundleConfig.getId())) {
            bundleConfig.setId(DateTime.now().toString(SUPPORT_BUNDLE_TIMESTAMP_PATTERN));
        }
        if (bundleConfig.getCreated() == null) {
            bundleConfig.setCreated(Calendar.getInstance().getTime());
        }
        return generateServiceBundle(bundleConfig.getId(), bundleConfig);
    }

    @PUT
    @Path(PATH_SUPPORT_BUNDLE + "/{bundleId: .+}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateServiceBundle(@PathParam("bundleId") String bundleId, ArtifactorySupportBundleConfig bundleInfo) {
        log.debug("Producing support bundle according to configuration: {}", bundleInfo);
        bundleInfo.setId(bundleId);
        String result;
        try {
            result = addonsManager.addonByType(SupportAddon.class)
                    .generateSupportBundle(bundleInfo, getServletContextUrl(httpServletRequest));
        } catch (IOException e) {
            log.error("Error serializing bundleInfo '{}'", bundleInfo);
            return internalError();
        } catch (BundleGenerationException e) {
            return Response.status(e.getResult().getStatusCode()).build();
        }
        catch (UnsupportedOperationException e) {
            log.trace(e.getMessage());
            throw new MissingRestAddonException();
        }
        return Response.ok()
                .entity(result)
                .build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    @Path(PATH_SUPPORT_BUNDLE + "/currentnode")
    public Response generateSupportBundleForCurrentNode(ArtifactorySupportBundleConfig bundleConfig) {
        if (isEmpty(bundleConfig.getId())) {
            bundleConfig.setId(DateTime.now().toString(SUPPORT_BUNDLE_TIMESTAMP_PATTERN));
        }
        String responseBody;
        try {
            responseBody = addonsManager.addonByType(SupportAddon.class).generateClusterNodeSupportBundle(bundleConfig);
        } catch (Exception e) {
            return Response.status(500).build();
        }
        return Response.ok(responseBody).build();
    }

    @GET
    @Path(PATH_SUPPORT_BUNDLE + "/{bundleId: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response supportBundleInfo(@PathParam("bundleId") String bundleId) {
        String bundleInfo;
        try {
            bundleInfo = addonsManager.addonByType(SupportAddon.class)
                    .getBundleInfo(searchBy(bundleId), getServletContextUrl(httpServletRequest));
        } catch (RepositoryRuntimeException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } catch (UnsupportedOperationException e) {
            throw new MissingRestAddonException();
        }

        if (Objects.isNull(bundleInfo)) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.status(Status.OK).entity(bundleInfo).build();
    }

    @DELETE
    @Path(PATH_SUPPORT_BUNDLE + "/{bundleId: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBundle(@PathParam("bundleId") String bundleId) {
        SupportAddon supportAddon = addonsManager.addonByType(SupportAddon.class);
        try {
            boolean found = supportAddon.deleteBundle(bundleId);
            return Response.status(found ? Status.NO_CONTENT : Status.NOT_FOUND).build();
        } catch (UnsupportedOperationException e) {
            throw new MissingRestAddonException();
        }
    }

    @GET
    @Path(PATH_SUPPORT_BUNDLE + "/{bundleId: .+}/archive")
    public Response getBundleArchive(@PathParam("bundleId") String bundleId) {
        Response response;
        try {
            JerseyArtifactoryResponse res = new JerseyArtifactoryResponse();
            res.setStatus(HttpStatus.SC_OK);
            SupportAddon supportAddon = addonsManager.addonByType(SupportAddon.class);
                res.sendStreamWithDelegation(supportAddon.downloadBundleArchive(bundleId));
            response = res.build();
        } catch (FolderDownloadException fde) {
            log.debug("Failed to send stream to client: ", fde);
            response = Response.status(fde.getCode()).entity("Failed to download bundle: " + fde.getMessage()).build();
        } catch (Exception e) {
            log.debug("Failed to send stream to client: ", e);
            response = Response.status(HttpStatus.SC_BAD_REQUEST).entity("Failed to download bundle: " + e.getMessage()).build();
        }

        return response;
    }

    @GET
    @Path(PATH_SUPPORT_BUNDLES)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listBundles() {
        SupportAddon supportAddon = addonsManager.addonByType(SupportAddon.class);
        try {
            final String result = supportAddon.listBundles();
            return  Response.status(Status.OK).entity(result).build();
        } catch (UnsupportedOperationException e) {
            throw new MissingRestAddonException();
        }
    }

    // The placeholder used to conveniently retrieve the ArtifactorySupportBundleConfig
    private NodeManifest searchBy(String bundleId) {
        final NodeManifestBundleInfo placeholder = new NodeManifestBundleInfo
                (bundleId, StringUtils.EMPTY, StringUtils.EMPTY, ZonedDateTime.now(), "");
        return newNodeManifest(placeholder, accessService.getArtifactoryServiceId());
    }

    private Response internalError() {
        return Response
                .status(Status.INTERNAL_SERVER_ERROR)
                .entity("Support content collection has failed, see 'support.log' for more details")
                .build();
    }
}
