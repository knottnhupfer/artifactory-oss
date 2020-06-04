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
import org.artifactory.api.config.ImportSettingsImpl;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.StatusEntry;
import org.artifactory.importexport.ImportIsDisabledException;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.ui.rest.model.admin.importexport.ImportExportSettings;
import org.artifactory.util.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ImportRepositoryService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ImportRepositoryService.class);

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        if (!ConstantValues.repositoryImportEnabled.getBoolean()) {
            response
                    .error(ImportIsDisabledException.REPOSITORIES_IMPORT_IS_DISABLED_MESSAGE)
                    .responseCode(HttpStatus.SC_FORBIDDEN);
            return;
        }

        ImportExportStatusHolder status = new ImportExportStatusHolder();
        ImportExportSettings importExportSettings = (ImportExportSettings) request.getImodel();
        if (!ImportExportPathValidator.isValidPath(importExportSettings.getPath())) {
            response.error(ImportSettings.INVALID_IMPORT_DIR);
            return;
        }
        boolean isZip = importExportSettings.isZip();
        boolean isAol = addonsManager.addonByType(CoreAddons.class).isAol();
        if (isAol && !isZip) {
            response.error("Artifactory SaaS can only import repositories from a Zip file.");
            return;
        }
        String filePath = importExportSettings.getPath();
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        try {
            if (isZip) {
                // Import from zip always receives uuid, otherwise, it is invalid
                if (isValidUuid(filePath) && !filePath.startsWith("../") && !filePath.startsWith("/")) {
                    filePath = uploadDir + "/" + filePath + UploadExtractedZipService.EXTRACTED_ZIP_SUFFIX;
                } else {
                    response.error("Unable to locate the uploaded content.");
                    return;
                }
            }
            String repoKey = importExportSettings.getRepository();
            boolean importAllRepos = isImportAllRepos(repoKey);
            boolean verbose = importExportSettings.isVerbose();
            boolean excludeMetadata = importExportSettings.isExcludeMetadata();
            File folder = new File(filePath);
            folder = setImportAllRepo(filePath, importAllRepos, folder);
            status.setVerbose(verbose);
            // create import setting
            ImportSettingsImpl importSettings = new ImportSettingsImpl(folder, status);
            // import repository
            importRepository(response, status, repoKey, filePath, importAllRepos, verbose, excludeMetadata,
                    importSettings);
            // delete temp folder after import
            File fileToDelete = new File(uploadDir, filePath);
            if (fileToDelete.exists() && isZip) {
                fileToDelete.delete();
            }
            if (status.isError()) {
                response.error(status.getLastError().getMessage());
            }
        } catch (Exception e) {
            if (isZip) {
                File fileToDelete = new File(
                        uploadDir + "/" + filePath + UploadExtractedZipService.EXTRACTED_ZIP_SUFFIX);
                if (isAol) {
                    FileUtils.deleteQuietly(fileToDelete);
                } else {
                    Files.removeFile(new File(importExportSettings.getPath()));
                }
            }
            response.error(e.getMessage());
        }
    }

    /**
     * update import all flag
     *
     * @param repoKey - repo key
     * @return if true - import all repositories
     */
    private boolean isImportAllRepos(String repoKey) {
        return repoKey.equals("All Repositories");
    }

    /**
     * import repositories data
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param status              - import status
     * @param repoKey             - repository key
     * @param folderPath          - folder path
     * @param importAllRepos      - if true import to all repositories
     * @param verbose             - if true set logging verbose
     * @param excludeMetadata     - if true exclude meta data
     * @param importSettings      - import setting data
     */
    private void importRepository(RestResponse artifactoryResponse, ImportExportStatusHolder status, String repoKey,
            String folderPath, boolean importAllRepos, boolean verbose, boolean excludeMetadata,
            ImportSettingsImpl importSettings) {
        try {
            importRepository(repoKey, importAllRepos, verbose, excludeMetadata, importSettings);
            // update response feedback
            updateResponseFeedback(artifactoryResponse, status, repoKey, folderPath);
            // delete folder
            String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
            if (folderPath.startsWith(uploadDir)) {
                org.apache.commons.io.FileUtils.deleteDirectory(new File(folderPath));
            }
        } catch (Exception e) {
            status.error(e.getMessage(), log);
        }
    }

    /**
     * update response feedback with import status
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param status              - import status
     * @param repoKey             - repository key
     * @param folderPath          - folder path
     */
    private void updateResponseFeedback(RestResponse artifactoryResponse, ImportExportStatusHolder status,
            String repoKey,
            String folderPath) {
        List<StatusEntry> errors = status.getErrors();
        List<StatusEntry> warnings = status.getWarnings();
        if (!errors.isEmpty()) {
            artifactoryResponse.error(" error(s) reported during the import. ");
        } else if (!warnings.isEmpty()) {
            artifactoryResponse.warn(" warning(s) reported during the import.");
        } else {
            artifactoryResponse.info("Successfully imported '" + folderPath + "' into '" + repoKey + "'.");
        }
    }

    /**
     * import repository from folder
     *
     * @param repoKey         - repository key
     * @param importAllRepos  - if true import to all repositories
     * @param verbose         - if true set logging verbose
     * @param excludeMetadata - if true exclude meta data
     * @param importSettings  - import setting data
     */
    private void importRepository(String repoKey, boolean importAllRepos, boolean verbose, boolean excludeMetadata,
            ImportSettingsImpl importSettings) {
        updateImportSetting(verbose, excludeMetadata, importSettings);
        if (importAllRepos) {
            repositoryService.importAll(importSettings);
        } else {
            importSettings.setIndexMarkedArchives(true);
            repositoryService.importRepo(repoKey, importSettings);
        }
    }

    /**
     * udpate import setting data
     *
     * @param verbose         - if true set logging verbose
     * @param excludeMetadata - if true exclude meta data
     * @param importSettings  - import setting data
     */
    private void updateImportSetting(boolean verbose, boolean excludeMetadata, ImportSettingsImpl importSettings) {
        importSettings.setFailIfEmpty(true);
        importSettings.setVerbose(verbose);
        importSettings.setIncludeMetadata(!excludeMetadata);
    }

    /**
     * set import all repo settings
     *
     * @param importAllRepos - if true import to all repositories
     * @param folder         - folder path
     * @return folder path
     */
    private File setImportAllRepo(String folderPath, boolean importAllRepos, File folder) {
        if (importAllRepos) {
            File repositoriesExportDir = new File(folderPath, "repositories");
            if (repositoriesExportDir.isDirectory()) {
                folder = repositoriesExportDir;
            }
        }
        return folder;
    }

    private boolean isValidUuid(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (Exception e) {
            log.debug("Unable to validate uuid. {} ", e.getMessage());
            log.trace("Unable to validate uuid. {} ", e);
            return false;
        }
    }
}
