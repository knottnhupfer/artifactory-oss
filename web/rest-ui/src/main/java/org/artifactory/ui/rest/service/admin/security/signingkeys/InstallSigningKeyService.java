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

package org.artifactory.ui.rest.service.admin.security.signingkeys;

import com.google.common.base.Joiner;
import org.apache.commons.io.FileUtils;
import org.artifactory.addon.common.gpg.GpgKeyStore;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.signingkey.SignKey;
import org.artifactory.ui.rest.model.utils.FileUpload;
import org.artifactory.ui.utils.MultiPartUtils;
import org.artifactory.util.Files;
import org.artifactory.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class InstallSigningKeyService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(InstallSigningKeyService.class);

    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    private GpgKeyStore gpgKeyStore;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        boolean isPublic = Boolean.valueOf(request.getQueryParamByKey("public"));
        List<String> fileNames = new ArrayList<>();
        // save key file to temp folder
        saveKeyFileToTempFolder(request, fileNames, response);
        // install signing key
        installSigningKey(response, isPublic, fileNames, request.getServletRequest());
    }

    /**
     * install signing key
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param isPublic            - if true public key
     * @param fileNames           - files name
     */
    private void installSigningKey(RestResponse artifactoryResponse, boolean isPublic, List<String> fileNames,
            HttpServletRequest request) {
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        File file = new File(uploadDir, fileNames.get(0));
        try {
            String key = FileUtils.readFileToString(file);
                onInstallKey(key, isPublic);
                String publicKeyDownloadTarget = getKeyLink(request);
                SignKey signKey = new SignKey(publicKeyDownloadTarget);
            Files.removeFile(file);
            artifactoryResponse.iModel(signKey);
                if (isPublic) {
                    artifactoryResponse.info("Public key is installed");
                } else {
                    artifactoryResponse.info("Private key is installed");
                }
            // delete temp key
            Files.removeFile(file);
        } catch (Exception e) {
            log.error(e.toString());
            artifactoryResponse.error(e.toString());
        }
    }

    /**
     * save key file to temp folder
     *
     * @param artifactoryRequest - encapsulate data related to request
     * @param fileNames          - file names
     */
    private void saveKeyFileToTempFolder(ArtifactoryRestRequest artifactoryRequest, List<String> fileNames, RestResponse response) {
        FileUpload uploadFile = (FileUpload) artifactoryRequest.getImodel();
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        MultiPartUtils.createTempFolderIfNotExist(uploadDir);
        // save file to temp folder
        try {
            MultiPartUtils.saveFileDataToTemp(centralConfigService, uploadFile.getFormDataMultiPart(), uploadDir,
                    fileNames, false);
        }catch (Exception e){
            response.error(e.getMessage());
        }
    }

    private String getKeyLink(HttpServletRequest request) {
        return Joiner.on('/').join(HttpUtils.getServletContextUrl(request),
                "api", "gpg", "key/public");
    }

    private void onInstallKey(String key, boolean isPublic) throws Exception {
        try {
            if (isPublic) {
                gpgKeyStore.savePublicKey(key);
            } else {
                gpgKeyStore.savePrivateKey(key);
            }
        } catch (Exception e) {
            log.error(e.toString());
            String keyType = (isPublic) ? "public Key" : "privateKey";
            throw new Exception("failed to save " + keyType);
        }
    }
}
