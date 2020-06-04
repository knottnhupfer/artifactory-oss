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
import org.artifactory.addon.HaAddon;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.rest.constant.HaRestConstants;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.rest.sha2.Sha256MigrationModel;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.backup.InternalBackupService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.info.InfoWriter;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.services.system.SystemInfoService;
import org.artifactory.schedule.TaskBase;
import org.artifactory.schedule.TaskService;
import org.artifactory.security.ArtifactoryEncryptionService;
import org.artifactory.security.access.AccessService;
import org.artifactory.sha2.Sha256MigrationTaskHelper;
import org.artifactory.storage.StorageService;
import org.artifactory.storage.binstore.service.BinariesGarbageCollectorService;
import org.artifactory.storage.binstore.service.GarbageCollectorStrategy;
import org.artifactory.storage.binstore.service.InternalBinaryService;
import org.artifactory.storage.fs.service.ConfigsService;
import org.artifactory.storage.fs.service.PropertiesService;
import org.jfrog.access.common.ServiceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * User: freds Date: Aug 12, 2008 Time: 6:11:53 PM
 */
@Path(SystemRestConstants.PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, HaRestConstants.ROLE_HA})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SystemResource {

    private static final Logger log = LoggerFactory.getLogger(SystemResource.class);

    @Autowired
    private CentralConfigService centralConfigService;
    @Autowired
    private ConfigsService configsService;
    @Autowired
    private ArtifactoryEncryptionService encryptionService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private InternalBinaryService binaryStore;
    @Autowired
    private InternalBackupService backupService;
    @Autowired
    private AddonsManager addonsManager;
    @Autowired
    private AccessService accessService;
    @Autowired
    private PropertiesService propertiesService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private Sha256MigrationTaskHelper sha256MigrationTaskHelper;
    @Autowired
    private BinariesGarbageCollectorService binariesGarbageCollectorService;
    @Autowired
    private SystemInfoService systemInfoService;

    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpResponse;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getPluginSystemInfo() throws Exception {
        return new InfoWriter().getInfoString()
                + ContextHelper.get().beanForType(AddonsManager.class).addonByType(PluginsAddon.class)
                .getPluginsInfoSupportBundleDump();
    }

    @Path(SystemRestConstants.PATH_CONFIGURATION)
    public ConfigResource getConfigResource() {
        return new ConfigResource(centralConfigService, httpServletRequest);
    }

    @Path(SystemRestConstants.PATH_SECURITY)
    public SecurityResource getSecurityResource() {
        return new SecurityResource(securityService, centralConfigService, httpServletRequest, addonsManager,
                accessService);
    }

    @Path(SystemRestConstants.PATH_STORAGE)
    public StorageResource getStorageResource() {
        return new StorageResource(storageService, backupService, binaryStore, httpResponse);
    }

    @Deprecated
    @Path(SystemRestConstants.PATH_LICENSE)
    public ArtifactoryLicenseResource getLicenseResource() {
        return new ArtifactoryLicenseResource();
    }

    @Path(SystemRestConstants.PATH_NEW_LICENSES)
    public ArtifactoryLicensesResource getNewLicensesResource() {
        return new ArtifactoryLicensesResource();
    }

    @POST
    @Path(SystemRestConstants.PATH_ENCRYPT)
    @Produces(MediaType.TEXT_PLAIN)
    public Response encrypt() {
        try {
            encryptionService.encrypt();
            return Response.ok().entity("DONE").build();
        } catch (Exception e) {
            String msg = "Could not encrypt with artifactory key, due to: " + e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Path(SystemRestConstants.PATH_DECRYPT)
    @Produces(MediaType.TEXT_PLAIN)
    public Response decrypt() {
        try {
            if (!CryptoHelper.hasArtifactoryKey(ArtifactoryHome.get())) {
                return Response.status(Response.Status.CONFLICT).entity(
                        "Cannot decrypt without artifactory key file").build();
            }
            encryptionService.decrypt();
        } catch (Exception e) {
            String msg = "Could not decrypt with artifactory key, due to: " + e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.ok().entity("DONE").build();
    }

    @GET
    @Path("serverTime")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getServerTime() {
        return Response.ok().entity(Long.toString(System.currentTimeMillis())).build();
    }

    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSystemInfo() {
        SystemInfo systemInfo = systemInfoService.getSystemInfo();
        return Response.ok().entity(systemInfo).build();
    }

    @GET
    @Path("xray/status")
    @Produces(MediaType.TEXT_PLAIN)
    public Response xrayStatus() {
        XrayAddon xrayAddon = addonsManager.addonByType(XrayAddon.class);
        assertXrayConfigExist(xrayAddon);
        boolean xrayEnabled = xrayAddon.isXrayEnabled();
        String message = "Xray indexing is " + (xrayEnabled ? "unblocked" : "blocked");
        return Response.ok().entity(message).build();
    }

    @POST
    @Path("xray/block")
    @Produces(MediaType.TEXT_PLAIN)
    public Response blockXrayGlobally() {
        XrayAddon xrayAddon = addonsManager.addonByType(XrayAddon.class);
        assertXrayConfigExist(xrayAddon);
        try {
            xrayAddon.blockXrayGlobally();
        } catch (UnsupportedOperationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return Response.ok().entity("Successfully blocked Xray indexing").build();
    }

    @POST
    @Path("xray/unblock")
    @Produces(MediaType.TEXT_PLAIN)
    public Response unblockXrayGlobally() {
        XrayAddon xrayAddon = addonsManager.addonByType(XrayAddon.class);
        assertXrayConfigExist(xrayAddon);
        if (xrayAddon.isXrayEnabled()) {
            return Response.status(Response.Status.OK).entity("Xray is unblocked already").build();
        }
        try {
            xrayAddon.unblockXrayGlobally();
        } catch (UnsupportedOperationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return Response.ok().entity("Successfully unblocked Xray indexing").build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path(SystemRestConstants.PATH_VERIFY_CONNECTION)
    public Response verifyConnection(VerifyConnectionModel verifyConnection) throws Exception {
        return new OutboundConnectionVerifier().verify(verifyConnection);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("migration/sha2/start")
    public Response startSha256Migration(Sha256MigrationModel model) {
        sha256MigrationTaskHelper.assertNotAol();
        if (sha256MigrationTaskHelper.jobIsAlreadyRunning()) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity("Sha256 migration task is already running").build();
        }
        TaskBase sha2MigrationTask = sha256MigrationTaskHelper.createManualTask(model);
        try {
            if (sha256MigrationTaskHelper.shouldPropagateStartRequest(model.getForceRunOnNodeId())) {
                addonsManager.addonByType(HaAddon.class).propagateTask(sha2MigrationTask, model.getForceRunOnNodeId());
        } else {
                taskService.startTask(sha2MigrationTask, false);
            }
        } catch (IllegalStateException e) {
            log.error("Error occurred while trying to start Sha256 migration task", e);
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(e.getMessage()).build();
        }
        return Response.ok().entity("Sha256 migration scheduled to start successfully").build();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("migration/sha2/stop")
    public Response stopSha256Migration(@QueryParam("sleepIntervalMillis")long sleepIntervalMillis) {
        sha256MigrationTaskHelper.stopMigrationTask(sleepIntervalMillis);
        if (sha256MigrationTaskHelper.shouldPropagateStopRequest()) {
            addonsManager.addonByType(HaAddon.class).propagateStopSha256Migration(sleepIntervalMillis);
        }
        return Response.ok().entity("Sha256 migration scheduled to stop successfully").build();
    }

    @GET
    @Path("service_id")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getServiceId() {
        ServiceId serviceId = accessService.getArtifactoryServiceId();
        return Response.ok(serviceId.getFormattedName()).build();
    }

    @Path("propagation")
    public PropagationResource propagation() {
        return new PropagationResource(addonsManager);
    }

    @Path("postgresql")
    public PsqlResource postgresql() {
        return new PsqlResource(propertiesService, configsService);
    }

    private void assertXrayConfigExist(XrayAddon xrayAddon) {
        if (xrayAddon.isXrayConfigMissing()) {
            throw new BadRequestException("Xray config does not exist");
        }
    }

    private GarbageCollectorStrategy getStrategy(String strategy) {
        try {
            return GarbageCollectorStrategy.valueOf(strategy.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}