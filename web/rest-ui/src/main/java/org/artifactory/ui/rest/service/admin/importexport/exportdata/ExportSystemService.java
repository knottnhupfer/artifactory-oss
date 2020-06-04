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

package org.artifactory.ui.rest.service.admin.importexport.exportdata;

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.common.ImportExportStatusHolder;
import org.artifactory.api.config.ExportSettingsImpl;
import org.artifactory.api.config.ImportExportPathValidator;
import org.artifactory.api.config.ImportExportSettingsImpl;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.StatusEntry;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.ui.rest.model.admin.importexport.ImportExportSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExportSystemService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ExportSystemService.class);

    @Autowired
    RepositoryService repositoryService;
    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        CoreAddons coreAddons = addonsManager.addonByType(CoreAddons.class);
        if (coreAddons.isAol() && !coreAddons.isDashboardUser()) {
            response.error("Artifactory SaaS does not support this feature.");
            return;
        }
        final ImportExportStatusHolder status = new ImportExportStatusHolder();
        ArtifactoryContext context = ContextHelper.get();
        ImportExportSettings importExportSettings = (ImportExportSettings) request.getImodel();
        if (!ImportExportPathValidator.isValidPath(importExportSettings.getPath())) {
            response.error(ExportSettings.INVALID_EXPORT_DIR);
            return;
        }
        File exportToPath = new File(importExportSettings.getPath());
        try {
            ExportSettings settings = new ExportSettingsImpl(exportToPath, status);
            // update export settings
            updateExportSettings(importExportSettings, settings);
            // export system
            context.exportTo(settings);
            // update response export feedback
            updateResponseFeedbackAfterSystemExport(response, status, exportToPath, settings);
        } catch (Exception e) {
            updateExceptionFeedback(response, exportToPath, e);
        }
    }

    /**
     * update response with exception feedback
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param exportToPath        - export to path
     * @param e                   - exception
     */
    private void updateExceptionFeedback(RestResponse artifactoryResponse, File exportToPath, Exception e) {
        artifactoryResponse.error("Failed to export system to '" + exportToPath + "': " + e.getMessage());
        log.error("Failed to export system.", e);
    }

    /**
     * update response fee
     */
    private void updateResponseFeedbackAfterSystemExport(RestResponse artifactoryResponse,
            ImportExportStatusHolder status,
            File exportToPath, ExportSettings settings) {
        List<StatusEntry> warnings = status.getWarnings();
        if (!warnings.isEmpty()) {
            artifactoryResponse.warn(
                    warnings.size() + " warning(s) reported during the export. Please review the ");
        }
        if (status.isError()) {
            String message = status.getStatusMsg();
            Throwable exception = status.getException();
            if (exception != null) {
                message = exception.getMessage();
            }
            artifactoryResponse.error("Failed to export system to '" + exportToPath + "': " + message);
        } else {
            File exportFile = settings.getOutputFile();
            artifactoryResponse.info("Successfully exported system to '" + exportFile.getPath() + "'.");
        }
    }

    /**
     * update export settings with data from import expport model
     *
     * @param importExportSettings import export model (send from client)
     * @param settings             - export settings
     */
    private void updateExportSettings(ImportExportSettings importExportSettings, ExportSettings settings) {
        settings.setCreateArchive(importExportSettings.isCreateZipArchive());
        settings.setFailFast(false);
        settings.setVerbose(importExportSettings.isVerbose());
        settings.setFailIfEmpty(true);
        Boolean excludeMetadata = importExportSettings.isExcludeMetadata();
        settings.setIncludeMetadata(!excludeMetadata);
        settings.setExcludeBuilds(importExportSettings.isExcluudeBuillds());
        settings.setM2Compatible(importExportSettings.isCreateM2CompatibleExport());
        Boolean excludeContent = importExportSettings.isExcludeContent();
        settings.setExcludeContent(excludeContent);
        if (!excludeMetadata || !excludeContent) {
            settings.setRepositories(getAllLocalAndDistributionRepoKeys());
        }
    }

    /**
     * get all local repositories keys
     *
     * @return - list of repo keys
     */
    private List<String> getAllLocalAndDistributionRepoKeys() {
        List<String> repoKeys = Lists.newArrayList();
        repositoryService.getLocalAndCachedRepoDescriptors().forEach(descriptor -> repoKeys.add(descriptor.getKey()));
        repositoryService.getDistributionRepoDescriptors().forEach(descriptor -> repoKeys.add(descriptor.getKey()));
        repositoryService.getReleaseBundlesRepoDescriptors().forEach(descriptor -> repoKeys.add(descriptor.getKey()));
        return repoKeys;
    }
}
