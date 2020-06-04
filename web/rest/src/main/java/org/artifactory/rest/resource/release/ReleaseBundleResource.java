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
package org.artifactory.rest.resource.release;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.api.rest.constant.HaRestConstants;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.rest.distribution.bundle.models.BundleTransactionResponse;
import org.artifactory.api.rest.distribution.bundle.models.BundleVersionsResponse;
import org.artifactory.api.rest.distribution.bundle.models.BundlesResponse;
import org.artifactory.api.rest.distribution.bundle.models.CloseTransactionStatusResponse;
import org.artifactory.api.rest.release.ReleaseBundleRequest;
import org.artifactory.api.rest.release.ReleaseBundleResult;
import org.artifactory.api.rest.release.ReleaseBundlesConfigModel;
import org.artifactory.api.rest.release.SourceReleaseBundleRequest;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.bundle.BundleType;
import org.jfrog.security.util.Pair;
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
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author Shay Bagants
 */
@Path(SystemRestConstants.JFROG_RELEASE)
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReleaseBundleResource {
    private static final Logger log = LoggerFactory.getLogger(ReleaseBundleResource.class);
    private static final String APPLICATION_JOSE = "application/jose";
    private final AddonsManager addonsManager;

    @Autowired
    AuthorizationService authService;

    @Autowired
    public ReleaseBundleResource(AddonsManager addonsManager) {
        this.addonsManager = addonsManager;
    }

    @Path("bundle")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER, HaRestConstants.ROLE_HA})
    @POST
    public Response assembleBundleFromAql(ReleaseBundleRequest bundleRequest, @QueryParam("includeMetaData") boolean includeMetaData) {
        if (authService.isAnonymous()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        ReleaseBundleResult releaseBundleResult = releaseBundleAddon.executeReleaseBundleRequest(bundleRequest, includeMetaData);
        return Response.ok().entity(releaseBundleResult).build();
    }

    @Path(SystemRestConstants.BUNDLE_TRANSACTION)
    @Consumes(APPLICATION_JOSE)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(AuthorizationService.ROLE_ADMIN)
    @POST
    public Response initiateBundleTransaction(String signedJwsBundle) throws IOException {
        log.debug("init bundle transaction for {}", signedJwsBundle);
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        BundleTransactionResponse response = releaseBundleAddon.createBundleTransaction(signedJwsBundle);
        return Response.status(HttpStatus.SC_CREATED).entity(response).build();
    }

    @OPTIONS
    @Path("store")
    @Produces("text/plain")
    public Response storeOptions() {
        return Response.status(HttpStatus.SC_OK)
                .header("Allow", "OPTIONS,PUT")
                .entity("OPTIONS, PUT")
                .build();
    }

    @Path("store")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(AuthorizationService.ROLE_ADMIN)
    @PUT
    public Response storeBundle(SourceReleaseBundleRequest bundleRequest)
            throws IOException, ParseException, SQLException {
        log.debug("Store bundle request: {}", bundleRequest);
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        Pair<String, Integer> pathAndStatus = releaseBundleAddon.storeBundle(bundleRequest);
        HashMap<Object, Object> entity = Maps.newHashMap();
        entity.put("bundle_path", pathAndStatus.getFirst());
        return Response.status(pathAndStatus.getSecond()).type(MediaType.APPLICATION_JSON).entity(entity).build();
    }

    @Path(SystemRestConstants.BUNDLE_TRANSACTION_CLOSE + "/{transaction_path: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(AuthorizationService.ROLE_ADMIN)
    @POST
    public Response closeBundleTransaction(@PathParam("transaction_path") String transactionPath) {
        log.debug("Close bundle transaction for {}", transactionPath);
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        releaseBundleAddon.closeBundleTransaction(transactionPath);
        return Response.ok(Collections.EMPTY_MAP).build();
    }

    @Path(SystemRestConstants.BUNDLE_TRANSACTION_CLOSE_ASYNC + "/{transaction_path: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(AuthorizationService.ROLE_ADMIN)
    @POST
    public Response closeBundleTransactionAsync(@PathParam("transaction_path") String transactionPath, @QueryParam("sync_wait_time_secs") Integer syncWaitTimeSecs) {
        log.debug("Close bundle transaction async for {} with syncWaitTimeSecs of {}", transactionPath, syncWaitTimeSecs);
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        CloseTransactionStatusResponse statusResponse = releaseBundleAddon.closeBundleTransactionAsync(transactionPath, syncWaitTimeSecs);
        return Response.ok(statusResponse).build();
    }

    @Path(SystemRestConstants.BUNDLE_CHECK_CLOSE_STATUS + "/{transaction_path: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(AuthorizationService.ROLE_ADMIN)
    @GET
    public Response checkCloseBundleStatus(@PathParam("transaction_path") String transactionPath) {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        CloseTransactionStatusResponse statusResponse = releaseBundleAddon.checkCloseTransactionStatus(transactionPath);
        return Response.ok(statusResponse).build();
    }

    @Path("bundles")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    @GET
    public Response getBundles(@QueryParam("type") String type) {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        BundlesResponse allBundles = releaseBundleAddon.getAllBundles(BundleType.fromType(type));
        return Response.ok().entity(allBundles).build();
    }

    @Path("bundles/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    @GET
    public Response getBundles(@PathParam("name") String bundleName, @QueryParam("type") String type) {
        BundleType bundleType = BundleType.fromType(type);
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        BundleVersionsResponse bundleVersionsResponse = releaseBundleAddon.getBundleVersions(bundleName, bundleType);
        return Response.ok().entity(bundleVersionsResponse).build();
    }

    @Path("bundles/{name}/{version}")
    @Produces({MediaType.APPLICATION_JSON, APPLICATION_JOSE})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    @GET
    public Response getBundles(@PathParam("name") String bundleName, @PathParam("version") String bundleVersion,
                               @QueryParam("format") String format, @QueryParam("type") String type) {
        BundleType bundleType = BundleType.fromType(type);
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        if (StringUtils.isNotBlank(format) && "jws".equalsIgnoreCase(format)) {
            String signedJwsBundle = releaseBundleAddon.getBundleSignedJws(bundleName, bundleVersion, bundleType);
            return Response.ok().entity(signedJwsBundle).type(APPLICATION_JOSE).build();
        }
        String bundleJson = releaseBundleAddon.getBundleJson(bundleName, bundleVersion, bundleType);
        return Response.ok().entity(bundleJson).build();
    }

    @Path("bundles/{name}/{version}/status")
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    @GET
    public Response getBundleStatus(@PathParam("name") String bundleName, @PathParam("version") String bundleVersion,
                                    @QueryParam("type") String type) {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        String bundleStatus = releaseBundleAddon.getBundleStatus(bundleName, bundleVersion, BundleType.fromType(type));
        return Response.ok().entity(bundleStatus).build();
    }

    @Path("bundles/{name}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    @DELETE
    public Response deleteBundle(@PathParam("name") String bundleName, @PathParam("version") String bundleVersion,
                                 @QueryParam("include_content") boolean includeContent) {
        log.debug("Delete bundle request {}/{}", bundleName, bundleVersion);
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        releaseBundleAddon.deleteReleaseBundle(bundleName, bundleVersion, BundleType.TARGET, includeContent);
        return Response.ok().entity(Collections.emptyMap()).build();
    }

    @Path("bundles/source/{name}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
    @DELETE
    public Response deleteSourceBundle(@PathParam("name") String bundleName, @PathParam("version") String bundleVersion) {
        log.debug("Delete source bundle request {}/{}", bundleName, bundleVersion);
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        releaseBundleAddon.deleteReleaseBundle(bundleName, bundleVersion, BundleType.SOURCE, true);
        return Response.ok().entity(Collections.emptyMap()).build();
    }

    @Path("bundles/config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(AuthorizationService.ROLE_ADMIN)
    @PUT
    public Response setReleaseBundlesConfig(ReleaseBundlesConfigModel releaseBundlesConfig) {
        log.debug("bundle config request {}", releaseBundlesConfig);
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        releaseBundleAddon.setReleaseBundlesConfig(releaseBundlesConfig);
        return Response.status(HttpStatus.SC_ACCEPTED).entity("Successfully updated release bundles config").build();
    }

    @Path("bundles/config")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(AuthorizationService.ROLE_ADMIN)
    @GET
    public Response getReleaseBundlesConfig() {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        ReleaseBundlesConfigModel releaseBundlesConfig = releaseBundleAddon.getReleaseBundlesConfig();
        return Response.status(HttpStatus.SC_OK).entity(releaseBundlesConfig).build();
    }
}
