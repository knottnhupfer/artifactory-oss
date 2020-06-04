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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.conda;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.conda.CondaAddon;
import org.artifactory.addon.conda.CondaMetadataInfo;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.conda.CondaArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.conda.CondaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * Service for fetching the Conda package metadata for the Info tab in the UI
 *
 * @author Uriah Levy
 * @author Dudi Morad
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CondaViewService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(CondaViewService.class);

    private AuthorizationService authorizationService;

    private RepositoryService repositoryService;

    private AddonsManager addonsManager;

    @Autowired
    public CondaViewService(AuthorizationService authorizationService, RepositoryService repositoryService,
            AddonsManager addonsManager) {
        this.authorizationService = authorizationService;
        this.repositoryService = repositoryService;
        this.addonsManager = addonsManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        CondaArtifactInfo requestCondaArtifactInfo = (CondaArtifactInfo) request.getImodel();
        RepoPath repoPath = RepoPathFactory
                .create(requestCondaArtifactInfo.getRepoKey(), requestCondaArtifactInfo.getPath());
        if (repositoryService.isVirtualRepoExist(repoPath.getRepoKey())) {
            repoPath = repositoryService.getVirtualFileInfo(repoPath).getRepoPath();
        }
        // read permission
        if (!authorizationService.canRead(repoPath)) {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            if (log.isErrorEnabled()) {
                log.error("Forbidden UI REST call from user: '{}'", authorizationService.currentUsername());
                return;
            }
        }
        CondaArtifactInfo responseCondaArtifactInfo = getCondaMetaData(requestCondaArtifactInfo, repoPath,
                addonsManager);
        // Update response with model data
        if (responseCondaArtifactInfo != null) {
            response.iModel(responseCondaArtifactInfo);
        }
    }

    CondaArtifactInfo getCondaMetaData(CondaArtifactInfo condaArtifactInfo, RepoPath repoPath,
            AddonsManager addonsManager) {
        CondaAddon condaAddon = addonsManager.addonByType(CondaAddon.class);
        if (condaAddon != null) {
            CondaMetadataInfo condaMetadata = condaAddon.getCondaMetadataToUiModel(repoPath);
            CondaInfo condaInfo = condaArtifactInfo.getCondaInfo();
            condaInfo.setBuild(condaMetadata.getBuild());
            condaInfo.setPlatform(condaMetadata.getPlatform());
            condaInfo.setArch(condaMetadata.getArch());
            condaInfo.setBuildNumber(condaMetadata.getBuildNumber());
            condaInfo.setDepends(condaMetadata.getDepends());
            condaInfo.setLicense(condaMetadata.getLicense());
            condaInfo.setName(condaMetadata.getName());
            condaInfo.setNoarch(condaMetadata.getNoarch());
            condaInfo.setVersion(condaMetadata.getVersion());
            condaInfo.setLicenseFamily(condaMetadata.getLicenseFamily());
            condaInfo.setFeatures(condaMetadata.getFeatures());
            condaInfo.setTrackFeatures(condaMetadata.getTrackFeatures());
        }
        return condaArtifactInfo;
    }

}
