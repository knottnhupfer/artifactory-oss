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
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.config.ImportExportPathValidator;
import org.artifactory.api.config.ImportSettingsImpl;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.constant.ImportRestConstants;
import org.artifactory.api.rest.constant.RepositoriesRestConstants;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.search.ArchiveIndexer;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ConstantValues;
import org.artifactory.importexport.ImportIsDisabledException;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.exception.ForbiddenWebAppException;
import org.artifactory.sapi.common.ImportSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * @author freds
 * @author Tomer Cohen
 */
@Path(ImportRestConstants.PATH_ROOT)
@RolesAllowed(AuthorizationService.ROLE_ADMIN)
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ImportResource {
    private static final Logger log = LoggerFactory.getLogger(ImportResource.class);

    @Context
    HttpServletResponse httpResponse;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    ArchiveIndexer archiveIndexer;

    @Autowired
    private AddonsManager addonsManager;


    @GET
    @Path(ImportRestConstants.SYSTEM_PATH)
    @Produces({SystemRestConstants.MT_IMPORT_SETTINGS, MediaType.APPLICATION_JSON})
    public ImportSettingsConfigurationImpl settingsExample() {
        ImportSettingsConfigurationImpl settingsConfiguration = new ImportSettingsConfigurationImpl();
        settingsConfiguration.importPath = "/import/path";
        return settingsConfiguration;
    }

    @POST
    @Path(ImportRestConstants.SYSTEM_PATH)
    @Consumes({SystemRestConstants.MT_IMPORT_SETTINGS, MediaType.APPLICATION_JSON})
    public Response activateImport(ImportSettingsConfigurationImpl settings) {
        log.debug("Activating import {}", settings);
        if (!ConstantValues.systemImportEnabled.getBoolean()) {
            throw new ForbiddenWebAppException(ImportIsDisabledException.SYSTEM_IMPORT_IS_DISABLED_MESSAGE);
        }

        if (!ImportExportPathValidator.isValidPath(settings.importPath)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ImportSettings.INVALID_IMPORT_DIR).build();
        }
        ImportExportStreamStatusHolder holder = new ImportExportStreamStatusHolder(httpResponse);
        try {
            ImportSettings importSettings = new ImportSettingsImpl(new File(settings.importPath), holder);
            importSettings.setIncludeMetadata(settings.includeMetadata);
            importSettings.setVerbose(settings.verbose);
            importSettings.setFailFast(settings.failOnError);
            importSettings.setFailIfEmpty(settings.failIfEmpty);
            importSettings.setEnableCopySecurityAccessDir(settings.isEnableCopySecurityAccessDir); //For Aol use
            ContextHelper.get().importFrom(importSettings);
            if (!httpResponse.isCommitted() && holder.hasErrors()) {
                return Response.serverError().entity(holder.getLastError().getMessage()).build();
            }
        } catch (Exception e) {
            holder.error("Received uncaught exception", e, log);
            if (!httpResponse.isCommitted()) {
                return Response.serverError().entity(e.getMessage()).build();
            }
        }
        return Response.ok().build();
    }

    @POST
    @Path(ImportRestConstants.REPOSITORIES_PATH)
    public void importRepositories(
            //The base path to import from (may contain a single repo or multiple repos with named sub folders
            @QueryParam(RepositoriesRestConstants.PATH) String path,
            //Empty/null repo -> all
            @QueryParam(RepositoriesRestConstants.TARGET_REPO) String targetRepo,
            //Include metadata - default 1
            @QueryParam(RepositoriesRestConstants.INCLUDE_METADATA) String includeMetadata,
            //Verbose - default 0
            @QueryParam(RepositoriesRestConstants.VERBOSE) String verbose) {

        if (StringUtils.isBlank(path)) {
            throw new BadRequestException("You must provide a repository path to import from.");
        }

        if (!ConstantValues.repositoryImportEnabled.getBoolean()) {
            throw new ForbiddenWebAppException(ImportIsDisabledException.REPOSITORIES_IMPORT_IS_DISABLED_MESSAGE);
        }

        if (!ImportExportPathValidator.isValidPath(path)) {
            throw new BadRequestException(ImportSettings.INVALID_IMPORT_DIR);
        }

        ImportExportStreamStatusHolder statusHolder = new ImportExportStreamStatusHolder(httpResponse);
        String repoNameToImport = targetRepo;
        if (StringUtils.isBlank(repoNameToImport)) {
            repoNameToImport = "All repositories";
        }
        statusHolder.status("Starting Repositories Import of " + repoNameToImport + " from " + path, log);
        if (!authorizationService.isAdmin()) {
            statusHolder.error(
                    String.format("The user: '%s' is not permitted to import repositories",
                            authorizationService.currentUsername()),
                    HttpStatus.SC_FORBIDDEN, log);
            return;
        }
        CoreAddons coreAddons = addonsManager.addonByType(CoreAddons.class);
        if (coreAddons.isAol() && !coreAddons.isDashboardUser()) {
            statusHolder.error("Import repositories from server is not permitted when running on the cloud",
                    HttpStatus.SC_FORBIDDEN, log);
            return;
        }
        if (StringUtils.isEmpty(path)) {
            statusHolder.error("Source directory path may not be empty.", HttpStatus.SC_BAD_REQUEST, log);
        }

        File baseDir = new File(path);
        if (!baseDir.exists()) {
            statusHolder.error("Directory " + path + " does not exist.", HttpStatus.SC_BAD_REQUEST, log);
            return;
        }
        ImportSettingsImpl importSettings = new ImportSettingsImpl(baseDir, statusHolder);
        if (StringUtils.isNotBlank(includeMetadata)) {
            importSettings.setIncludeMetadata(Integer.parseInt(includeMetadata) == 1);
        }
        if (StringUtils.isNotBlank(verbose)) {
            importSettings.setVerbose(Integer.parseInt(verbose) == 1);
        }
        try {
            if (StringUtils.isBlank(targetRepo)) {
                repositoryService.importAll(importSettings);
            } else {
                importSettings.setIndexMarkedArchives(true);
                repositoryService.importRepo(targetRepo, importSettings);
            }
        } catch (Exception e) {
            statusHolder.error("Unable to import repository", e, log);
        }
    }
}
