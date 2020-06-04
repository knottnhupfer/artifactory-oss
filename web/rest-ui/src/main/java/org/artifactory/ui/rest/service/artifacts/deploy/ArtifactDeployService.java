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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.addon.ha.propagation.uideploy.UIDeployPropagationResult;
import org.artifactory.api.artifact.UnitInfo;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.repo.DeployService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.request.ArtifactoryRequestBase;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.deploy.UploadArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.deploy.UploadedArtifactInfo;
import org.artifactory.ui.utils.TreeUtils;
import org.artifactory.util.Files;
import org.jfrog.storage.binstore.exceptions.BinaryRejectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ArtifactDeployService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ArtifactDeployService.class);

    private static final Pattern UI_ERR_MSG = Pattern.compile("\\{\".*\"\\:\"(.*)\"\\}");

    @Autowired
    private DeployService deployService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        UploadArtifactInfo uploadArtifactInfo = (UploadArtifactInfo) request.getImodel();
        String handlingNodeId = uploadArtifactInfo.getHandlingNode();
        log.debug("The handling node is {}", (handlingNodeId != null ? handlingNodeId : "this instance"));
        if (shouldPropagateRequestToHandlingNode(handlingNodeId)) {
            log.debug("Propagating request to: {}", handlingNodeId);
            // Reset handling node coming in from request payload so we don't loop
            uploadArtifactInfo.setHandlingNode(null);
            UIDeployPropagationResult otherNodeResponse = addonsManager.addonByType(HaCommonAddon.class).
                    propagateUiUploadRequest(handlingNodeId, uploadArtifactInfo.toString());
            int statusCode = otherNodeResponse.getStatusCode();
            if (statusCode < HttpStatus.SC_OK || statusCode >= HttpStatus.SC_MULTIPLE_CHOICES) {
                String errMsg = getErrorMessage(otherNodeResponse, handlingNodeId);
                response.error(errMsg).responseCode(statusCode);
            } else {
                response.iModel(otherNodeResponse.getContent());
            }
        } else {
            String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
            deploy(uploadArtifactInfo, uploadDir, response);
        }
    }
    /**
     * deploy file to repository
     *
     * @param uploadArtifactInfo  - upload artifact info
     * @param uploadDir           - upload temp folder
     * @param artifactoryResponse - encapsulate data require for response
     */
    private void deploy(UploadArtifactInfo uploadArtifactInfo, String uploadDir, RestResponse artifactoryResponse) {
        String fileName = uploadArtifactInfo.getFileName();
        fileName = new File(fileName).getName();
        String repoKey = uploadArtifactInfo.getRepoKey();
        File file = new File(uploadDir, fileName);
        RepoBaseDescriptor repoDescriptor = getRepoDescriptor(repoKey);
        if (repoDescriptor == null) {
            artifactoryResponse.error("No such repository " + repoKey);
            return;
        }
        try {
            doDeployment(uploadArtifactInfo, file, repoDescriptor);
            // Don't extract the unitInfo path before the doDeployment, because the 'doDeployment' might modify the unitInfo path (remove the matrix params if any)
            String artifactPath = uploadArtifactInfo.getUnitInfo().getPath();
            UploadedArtifactInfo uploadedArtifactInfo = new UploadedArtifactInfo(
                    shouldProvideTreeLink(repoDescriptor, artifactPath), repoDescriptor.getKey(), artifactPath);
            artifactoryResponse.iModel(uploadedArtifactInfo);
        } catch (RepoRejectException  | BinaryRejectedException e) {
            log.error(e.toString());
            log.debug("Failed to deploy file.", e);
            String err = DeployUtil.getDeployError(uploadArtifactInfo.getFileName(), repoKey, e);
            artifactoryResponse.error(err);
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }

    private void doDeployment(UploadArtifactInfo uploadArtifactInfo, File file, RepoBaseDescriptor repoDescriptor) throws RepoRejectException {
        UnitInfo unitInfo = uploadArtifactInfo.getUnitInfo();
        Properties properties = parseMatrixParams(unitInfo);
        // deploy file with pom
        if (uploadArtifactInfo.isPublishUnitConfigFile()) {
            deployService.deploy(repoDescriptor, unitInfo, file, uploadArtifactInfo.getUnitConfigFileContent(),
                    true, false, properties);
            if (repoDescriptor instanceof LocalRepoDescriptor) {
                deletePomFile(unitInfo, (LocalRepoDescriptor) repoDescriptor);
            }
        } else {
            deployService.deploy(repoDescriptor, unitInfo, file, properties);
        }
    }

    private RepoBaseDescriptor getRepoDescriptor(String repoKey) {
        RepoBaseDescriptor repoDescriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoKey);
        if (repoDescriptor == null) {
            repoDescriptor = repositoryService.virtualRepoDescriptorByKey(repoKey);
        }
        return repoDescriptor;
    }

    private boolean shouldProvideTreeLink(RepoBaseDescriptor repoDescriptor, String artifactPath) {
        return TreeUtils.shouldProvideTreeLink(repoDescriptor, artifactPath);
    }

    /**
     * get uploaded file Properties
     *
     * @param unitInfo - unit info - debian / artifact
     */
    private Properties parseMatrixParams(UnitInfo unitInfo) {
        Properties props;
        props = (Properties) InfoFactoryHolder.get().createProperties();
        String targetPathFieldValue = unitInfo.getPath();
        int matrixParamStart = targetPathFieldValue.indexOf(Properties.MATRIX_PARAMS_SEP);
        if (matrixParamStart > 0) {
            ArtifactoryRequestBase.processMatrixParams(props, targetPathFieldValue.substring(matrixParamStart));
            updateUnitInfo(unitInfo);
        }
        return props;
    }

    /**
     * update unit info path after removing path param
     */
    private void updateUnitInfo(UnitInfo unitInfo) {
        String[] splitedPath = unitInfo.getPath().split(";");
        if (splitedPath.length > 0) {
            unitInfo.setPath(splitedPath[0]);
        }
    }

    private void deletePomFile(UnitInfo unitInfo, LocalRepoDescriptor localRepoDescriptor) {
        RepoPath repoPath = InternalRepoPathFactory.create(localRepoDescriptor.getKey(), unitInfo.getPath());
        MavenArtifactInfo mavenArtifactInfo = (MavenArtifactInfo) unitInfo;
        RepoPath pomPath = InternalRepoPathFactory.create(repoPath.getParent(),
                mavenArtifactInfo.getArtifactId() + "-" + mavenArtifactInfo.getVersion() + ".pom");
        Files.removeFile(new File(pomPath.getPath()));
    }

    /**
     * An upload request may have already been served by another node, which just deployed the file to it's temp dir.
     * If this is not the same node we need to propagate the request to it since the uploaded file is in it's local
     * filesystem
     */
    private boolean shouldPropagateRequestToHandlingNode(String handlingNodeId) {
        HaCommonAddon haAddon = addonsManager.addonByType(HaCommonAddon.class);
        return haAddon.isHaEnabled()
                && StringUtils.isNotBlank(handlingNodeId)
                && !haAddon.getCurrentMemberServerId().equals(handlingNodeId);
    }

    private String getErrorMessage(UIDeployPropagationResult result, String handlingNodeId) {
        String errMsg = null;
        try {
            String rawMessage = JacksonReader.bytesAsTree(result.getContent().getBytes()).findValue("message").asText();
            Matcher matcher = UI_ERR_MSG.matcher(rawMessage);
            if (matcher.find()) {
                errMsg = matcher.group(1);
            }
        } catch (Exception e) {
            log.debug("Failed to parse UI error message coming back from node " + handlingNodeId, e);
        }
        return errMsg != null ? errMsg : "Deployment propagation error: " +
                (result != null ? result.getErrorMessage() : "Failed to parse response from node " + handlingNodeId);
    }
}