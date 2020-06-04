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

package org.artifactory.ui.rest.service.admin.security.signingkeys.keystore;

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.signingkey.KeyStore;
import org.artifactory.ui.rest.model.utils.FileUpload;
import org.artifactory.ui.utils.MultiPartUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AddKeyStoreService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(AddKeyStoreService.class);

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        List<String> fileNames = new ArrayList<>();
        String password = request.getQueryParamByKey("pass");
        // save keystore to temp folder
        saveKeyFileToTempFolder(request, fileNames, response);
        // load keystore to server
        loadKeyStore(response, fileNames, password);
    }

    /**
     * load KeyStore
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param fileNames           - key store name
     * @param password            - key store pass
     */
    private void loadKeyStore(RestResponse artifactoryResponse, List<String> fileNames, String password) {
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        String keyName = fileNames.get(0);
        File file = new File(uploadDir, keyName);
        List<String> aliasList = getAliasesList(file, password, artifactoryResponse);
        if (aliasList != null) {
            KeyStore key = new KeyStore(true, aliasList, keyName, password);
            artifactoryResponse.iModel(key);
            artifactoryResponse.info("Key Pair uploaded successfully");
        }
    }

    private List<String> getAliasesList(File file, String password, RestResponse artifactoryResponse) {
        try {
            java.security.KeyStore keyStore = getKeyStore(file, password);
            List<String> aliasList = Lists.newArrayList();
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                aliasList.add(aliases.nextElement());
            }
            return aliasList;
        } catch (Exception e) {
            String errorMessage = "Can't import aliases from key store file " + file.getName() + ": " + e.getMessage();
            log.error(errorMessage);
            log.debug(errorMessage, e);
            artifactoryResponse.error(errorMessage);
            return null;
        }
    }

    /**
     * save key file to temp folder
     *
     * @param artifactoryRequest - encapsulate data related to request
     * @param fileNames          - file names
     */
    private void saveKeyFileToTempFolder(ArtifactoryRestRequest artifactoryRequest, List<String> fileNames,
            RestResponse response) {
        FileUpload uploadFile = (FileUpload) artifactoryRequest.getImodel();
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        MultiPartUtils.createTempFolderIfNotExist(uploadDir);
        // save file to temp folder
        try {
            MultiPartUtils.saveFileDataToTemp(centralConfigService, uploadFile.getFormDataMultiPart(), uploadDir,
                    fileNames, false);
        } catch (Exception e) {
            response.error(e.getMessage());
        }
    }

    /**
     * @param file     - key store
     * @param password - key store password
     * @return - Key store model
     */
    private java.security.KeyStore getKeyStore(File file, String password) {
        ArtifactWebstartAddon artifactWebstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
        return artifactWebstartAddon.loadKeyStore(file, password);
    }
}
