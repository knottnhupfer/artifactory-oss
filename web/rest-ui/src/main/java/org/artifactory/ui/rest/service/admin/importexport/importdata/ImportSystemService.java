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

package org.artifactory.ui.rest.service.admin.importexport.importdata;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.common.ImportExportStatusHolder;
import org.artifactory.api.config.ImportExportPathValidator;
import org.artifactory.api.config.ImportExportSettingsImpl;
import org.artifactory.api.config.ImportSettingsImpl;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.StatusEntry;
import org.artifactory.importexport.ImportIsDisabledException;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.ui.rest.model.admin.importexport.ImportExportSettings;
import org.artifactory.util.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ImportSystemService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(ImportSystemService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        if (!ConstantValues.systemImportEnabled.getBoolean()) {
            response
                    .error(ImportIsDisabledException.SYSTEM_IMPORT_IS_DISABLED_MESSAGE)
                    .responseCode(HttpStatus.SC_FORBIDDEN);
            return;
        }

        if (addonsManager.addonByType(CoreAddons.class).isAol()) {
            response.error("System import is not available on Artifactory online.");
            return;
        }
        ImportExportSettings importExportSettings = (ImportExportSettings) request.getImodel();
        if (!ImportExportPathValidator.isValidPath(importExportSettings.getPath())) {
            response.error(ImportSettings.INVALID_IMPORT_DIR);
            return;
        }
        ImportExportStatusHolder status = new ImportExportStatusHolder();
        boolean isZip = importExportSettings.isZip();
        File importFromFolder = null;
        File importFromPath = null;
        try {
            String pathname = importExportSettings.getPath();
            importFromFolder = new File(pathname);
            String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
            if (isZip) {
                importFromPath = new File(uploadDir, importFromFolder.getName() + "_extract");
                ZipUtils.extract(importFromFolder, importFromPath);
            } else {
                importFromPath = new File(pathname);
            }
            // return if path not exist
            if (!importFromPath.exists()) {
                updateResponseFeedback(response, importFromPath);
                return;
            }
            // return if folder path do not contain files or cannot be accessed
            if (importFromPath.isDirectory()) {
                if (isInvalidFolder(response, importFromPath)) {
                    return;
                }
                importFromFolder = importFromPath;
            }
            // import system data
            List<StatusEntry> warnings = importSystem(status, importExportSettings, importFromFolder);
            // update response with post import warnings
            updateResponsePostImport(response, status, importFromPath, warnings);
        } catch (Exception e) {
            response.error("Failed to perform system import from '" + importFromPath + "'. " + e.getMessage());
            log.error("Failed to import system. ", e);
        } finally {
            cleanFolderAndStatus(status, importFromFolder, importFromPath, isZip);
        }
    }

    /**
     * delete folder and clean status
     *
     * @param status           - import status
     * @param importFromFolder - import folder
     * @param importFromPath   - import folder path
     */
    private void cleanFolderAndStatus(ImportExportStatusHolder status, File importFromFolder, File importFromPath,
            boolean isZip) {
        if (importFromPath != null && isZip(importFromPath)) {
            //Delete the extracted dir
            try {
                if (importFromFolder != null && isZip) {
                    FileUtils.deleteDirectory(importFromFolder);
                }
            } catch (IOException e) {
                log.warn("Failed to delete export directory: " +
                        importFromFolder, e);
            }
        }
        status.reset();
    }

    private void updateResponsePostImport(RestResponse artifactoryResponse, ImportExportStatusHolder status,
            File importFromPath, List<StatusEntry> warnings) {
        if (!warnings.isEmpty()) {
            artifactoryResponse
                    .warn("Warnings have been produced during the import. Please review the logs for further information.");
        }
        List<String> errors = new ArrayList<>();
        if (status.isError()) {
            int errorCount = status.getErrors().size();
            if (errorCount > 1) {
                String msg = errorCount + " errors occurred while importing system from '" + importFromPath +
                        "'. Check logs for more information.";
                status.getErrors().forEach(error -> errors.add(error.getMessage()));
                artifactoryResponse.error(msg);
            } else {
                String msg = "Error while importing system from '" + importFromPath + "': " + status.getStatusMsg();
                artifactoryResponse.errors(errors);
                log.error(msg);
            }
        } else {
            artifactoryResponse.info("Successfully imported system from '" + importFromPath + "'.");
        }
    }

    /**
     * import system data
     *
     * @param status               - import status
     * @param importExportSettings - data import model - hold imports flags
     * @param importFromFolder     - import folder path
     */
    private List<StatusEntry> importSystem(ImportExportStatusHolder status, ImportExportSettings importExportSettings,
            File importFromFolder) {
        status.status("Importing from directory...", log);
        boolean verbose = importExportSettings.isVerbose();
        boolean excludeMetadata = importExportSettings.isExcludeMetadata();
        boolean excludeContent = importExportSettings.isExcludeContent();
        ArtifactoryContext context = ContextHelper.get();
        // update import system flags data
        ImportSettingsImpl importSettings = new ImportSettingsImpl(importFromFolder, status);
        importSettings.setFailFast(false);
        importSettings.setFailIfEmpty(true);
        importSettings.setVerbose(verbose);
        importSettings.setIncludeMetadata(!excludeMetadata);
        importSettings.setExcludeContent(excludeContent);
        // import system data
        context.importFrom(importSettings);
        return status.getWarnings();
    }

    /**
     * Check if the folder can be read and has files in it
     */
    private boolean isInvalidFolder(RestResponse artifactoryResponse, File importFromPath) {
        if (importFromPath != null) {
            String[] fileList = importFromPath.list();
            if (fileList == null || fileList.length == 0) {
                {
                    String errorMessage = "Directory '" + importFromPath + "' is empty or cannot be accessed.";
                    artifactoryResponse.error(errorMessage);
                    log.error("Failed to import system: {}", errorMessage);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * update response feedback
     *
     * @param artifactoryResponse - encapsulate
     * @param importFromPath      - import folder path
     */
    private void updateResponseFeedback(RestResponse artifactoryResponse, File importFromPath) {
        artifactoryResponse.error("Specified location '" + importFromPath +
                "' does not exist.");
    }

    /**
     * check if file is zip or not
     *
     * @param file - file to upload
     * @return if true file is zip
     */
    private boolean isZip(File file) {
        return file.getName().toLowerCase(Locale.ENGLISH).endsWith(".zip_extract");
    }
}
