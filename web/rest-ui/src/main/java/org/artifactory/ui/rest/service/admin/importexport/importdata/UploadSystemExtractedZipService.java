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
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.importexport.ImportExportSettings;
import org.artifactory.ui.rest.model.utils.FileUpload;
import org.artifactory.ui.utils.MultiPartUtils;
import org.artifactory.util.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UploadSystemExtractedZipService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(UploadSystemExtractedZipService.class);

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        List<String> fileNames = new ArrayList<>();
        FileUpload uploadFile = (FileUpload) request.getImodel();
        try {
            String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
            MultiPartUtils.createTempFolderIfNotExist(uploadDir);
            // save file to temp folder
            MultiPartUtils.saveFileDataToTemp(centralConfigService, uploadFile.getFormDataMultiPart(), uploadDir,
                    fileNames, false);
            // extract file to temp folder
            String importFromFolder = fileNames.get(0) + "_extract";
            String pathname = uploadDir + "/" + importFromFolder;
            File extractedDirectory = new File(pathname);
            FileUtils.deleteDirectory(extractedDirectory);
            FileUtils.forceMkdir(extractedDirectory);
            try {
                File sourceArchive = new File(uploadDir + "/" + fileNames.get(0));
                ZipUtils.extract(sourceArchive, extractedDirectory);
                // delete zip file
                sourceArchive.delete();
            } catch (Exception e) {
                String message = "Failed to extract file " + fileNames.get(0);
                response.error(message);
                log.error(message, e);
                return;
            }
            ImportExportSettings importExportSettings = new ImportExportSettings(fileNames.get(0) + "_extract");

            response.iModel(importExportSettings);
        } catch (Exception e) {
            response.error(e.getMessage());
        }
    }
}
