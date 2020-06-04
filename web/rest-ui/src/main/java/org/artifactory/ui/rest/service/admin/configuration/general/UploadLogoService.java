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

package org.artifactory.ui.rest.service.admin.configuration.general;

import org.apache.commons.io.FileUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.utils.FileUpload;
import org.artifactory.ui.utils.MultiPartUtils;
import org.jfrog.config.wrappers.FileEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.UUID;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UploadLogoService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(UploadLogoService.class);

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            File tempWorkingDir = ContextHelper.get().getArtifactoryHome().getTempWorkDir();
            String tempFileName = UUID.randomUUID().toString();
            // save file to logo folder
            saveFileToTempLocation(request, tempWorkingDir, tempFileName);
            File tempLogoFile = new File(tempWorkingDir, tempFileName);
            boolean fakeImage = isImageFake(tempLogoFile);
            if (fakeImage) {
                tempLogoFile.delete();
                response.error("Invalid file type");
                return;
            } else {
                String logoDir = ContextHelper.get().getArtifactoryHome().getLogoDir().getAbsolutePath();
                File finalLogoFile = new File(logoDir, "logo");
                boolean exists = finalLogoFile.exists();
                java.nio.file.Files.move(tempLogoFile.toPath(), finalLogoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                try {
                    ContextHelper.get().getConfigurationManager().forceFileChanged(finalLogoFile,"artifactory.ui.",
                            exists ? FileEventType.MODIFY : FileEventType.CREATE);
                } catch (Exception e){
                    log.debug("Failed to propagate logo change",e);
                    log.warn("Failed to propagate logo change");
                }
                response.info("Logo Uploaded Successfully");
            }
        } catch (Exception e) {
            log.error("The cause for uploading file failure is: " + e.getMessage(), e);
            response.error("error uploading file to server");
        }
    }

    /**
     * save logo to logo folder
     *
     * @param artifactoryRequest - encapsulate data related to request
     */
    private void saveFileToTempLocation(ArtifactoryRestRequest artifactoryRequest, File tempWorkingDir, String tempFileName) {
        FileUpload fileUpload = (FileUpload) artifactoryRequest.getImodel();
        if (!tempWorkingDir.exists()) {
            try {
                FileUtils.forceMkdir(tempWorkingDir);
            } catch (IOException e) {
                log.error("Failed to create temp directory");
                log.debug("Failed to create temp directory:" ,e);
            }
        }
        MultiPartUtils.saveSpecificFile(centralConfigService, fileUpload.getFormDataMultiPart(), tempWorkingDir.getAbsolutePath(),
                tempFileName);
    }

    /**
     * check if the image has fake format , its not a real image
     * this check done to eliminate security issue
     */
    private boolean isImageFake(File file) throws Exception {
        boolean isFakeImage = false;
        ImageInputStream imageInputStream = null;
        try {
            Path path = Paths.get(file.getCanonicalPath());
            byte[] data = Files.readAllBytes(path);
            imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(data));
            Iterator<ImageReader> iter = ImageIO.getImageReaders(imageInputStream);
            if (!iter.hasNext()) {
                isFakeImage = true;
            }
        } finally {
            if (imageInputStream != null) {
                try {
                    imageInputStream.close();
                } catch (IOException e) {
                    throw new IOException(e);
                }
            }
        }
        return isFakeImage;
    }
}
