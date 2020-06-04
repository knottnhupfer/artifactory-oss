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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.npmview;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.npm.NpmAddon;
import org.artifactory.addon.npm.NpmMetadataInfo;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.npm.NpmArtifactInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NpmViewService implements RestService<NpmArtifactInfo> {

    private static final Logger log = LoggerFactory.getLogger(NpmViewService.class);

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest<NpmArtifactInfo> request, RestResponse response) {
        NpmArtifactInfo npmArtifactInfo = request.getImodel();
        // fetch npm meta data
        fetchNpmMetaData(request, response, npmArtifactInfo);
    }

    /**
     * fetch npm meta data
     *
     * @param artifactoryRequest  - encapsulate data relate to request
     * @param artifactoryResponse - encapsulate data require for response
     * @param npmArtifactInfo     - npm artifact info
     */
    private void fetchNpmMetaData(ArtifactoryRestRequest<NpmArtifactInfo> artifactoryRequest, RestResponse<NpmArtifactInfo> artifactoryResponse,
                                  NpmArtifactInfo npmArtifactInfo) {
        String repoKey = npmArtifactInfo.getRepoKey();
        String path = npmArtifactInfo.getPath();
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        if(repositoryService.isVirtualRepoExist(repoPath.getRepoKey())){
            repoPath = repositoryService.getVirtualFileInfo(repoPath).getRepoPath();
        }
        // read permission checks
        if (!authorizationService.canRead(repoPath)) {
            artifactoryResponse.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            log.error("Forbidden UI REST call from user: '{}'", authorizationService.currentUsername());
            return;
        }
        NpmArtifactInfo npmArtifactInfoModel = artifactoryRequest.getImodel();
        /// get npm add on
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        NpmAddon npmAddon = addonsManager.addonByType(NpmAddon.class);

        if (npmAddon == null) {
            return;
        }
        // get npm meta data
        NpmMetadataInfo npmMetaDataInfo = npmAddon.getNpmMetaDataInfo(repoPath);
        if (npmMetaDataInfo == null) {
            return;
        }
        npmArtifactInfo.setNpmDependencies(npmMetaDataInfo.getNpmDependencies());
        npmArtifactInfo.setNpmInfo(npmMetaDataInfo.getNpmInfo());
        npmArtifactInfo.clearRepoData();
        artifactoryResponse.iModel(npmArtifactInfoModel);
    }
}
