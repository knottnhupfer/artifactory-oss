/*
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

package org.artifactory.ui.rest.service.artifacts.deploy;

import org.artifactory.api.artifact.UnitInfo;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.DeployService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.deploy.UploadArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.deploy.UploadedArtifactInfo;
import org.artifactory.ui.utils.TreeUtils;
import org.artifactory.util.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.File;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ArtifactDeployBundleService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ArtifactDeployBundleService.class);

    @Autowired
    DeployService deployService;

    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        // get upload model
        if(request.getImodel() == null) {
            response.responseCode(SC_BAD_REQUEST).error("Request missing body");
            return;
        }
        UploadArtifactInfo uploadArtifactInfo = (UploadArtifactInfo) request.getImodel();
        String repoKey = uploadArtifactInfo.getRepoKey();
        // deploy bundle
        deployBundle(response, uploadDir, uploadArtifactInfo, repoKey);
    }

    /**
     * deploy bundle and update response
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param uploadDir           - temp folder
     * @param uploadArtifactInfo  - upload artifact info
     * @param repoKey             - repo key
     */
    private void deployBundle(RestResponse artifactoryResponse, @Nonnull String uploadDir, UploadArtifactInfo uploadArtifactInfo,
            String repoKey) {
        try {
            LocalRepoDescriptor localRepoDescriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoKey);
            UnitInfo unitInfo = uploadArtifactInfo.getUnitInfo();
            if(!validateInput(artifactoryResponse, uploadArtifactInfo, localRepoDescriptor, unitInfo)) {
                return;
            }
            BasicStatusHolder statusHolder = new BasicStatusHolder();

            // deploy file to repository
            File bundleFile = new File(uploadDir, uploadArtifactInfo.getFileName());
            String path = unitInfo.getPath();
            String prefixPath = path == null ? "" : path;
            deployService.deployBundle(bundleFile, localRepoDescriptor, statusHolder, false, prefixPath, null);

            // update feedback message
            updateFeedbackMsg(artifactoryResponse, statusHolder);
            // delete tmp file
            Files.removeFile(bundleFile);
            UploadedArtifactInfo uploadedArtifactInfo = new UploadedArtifactInfo(
                    TreeUtils.shouldProvideTreeLink(localRepoDescriptor, prefixPath),
                    localRepoDescriptor.getKey(), prefixPath);
            artifactoryResponse.iModel(uploadedArtifactInfo);

        } catch (Exception e) {
            artifactoryResponse.error(e.getMessage());
            log.error(e.toString());
            artifactoryResponse.error(DeployUtil.getDeployError(uploadArtifactInfo.getFileName(), repoKey, e));
        }
    }

    private boolean validateInput(RestResponse artifactoryResponse, UploadArtifactInfo uploadArtifactInfo,
            LocalRepoDescriptor localRepoDescriptor, UnitInfo unitInfo) {
        if(uploadArtifactInfo.getFileName() == null){
            artifactoryResponse.responseCode(SC_BAD_REQUEST).error("File name not found");
            return false;
        }
        if(localRepoDescriptor == null){
            artifactoryResponse.responseCode(SC_BAD_REQUEST).error("Repository doesn't exist");
            return false;
        }
        if(unitInfo == null){
            artifactoryResponse.error("Missing artifact data");
            return false;
        }
        return true;
    }

    /**
     * update error and warn feedback msg
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param statusHolder        - msg status holder
     */
    private void updateFeedbackMsg(RestResponse artifactoryResponse, BasicStatusHolder statusHolder) {
        if (statusHolder.hasErrors()) {
            artifactoryResponse.error(statusHolder.getErrors().get(0).getMessage());
        } else if (statusHolder.hasWarnings()) {
            artifactoryResponse.warn(statusHolder.getWarnings().get(0).getMessage());
        } else {
            artifactoryResponse.info(statusHolder.getStatusMsg());
        }
    }
}
