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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.cranview;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.cran.CranAddon;
import org.artifactory.addon.cran.CranMetadataInfo;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.cran.CranArtifactInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * Service for fetching the Helm metadata for the Info tab in the UI
 *
 *  @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CranViewService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(CranViewService.class);

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        CranArtifactInfo cranArtifactInfo = (CranArtifactInfo) request.getImodel();
        RepoPath repoPath = RepoPathFactory.create(cranArtifactInfo.getRepoKey(), cranArtifactInfo.getPath());
        if (repositoryService.isVirtualRepoExist(repoPath.getRepoKey())) {
            repoPath = repositoryService.getVirtualFileInfo(repoPath).getRepoPath();
        }
        // read permission checks
        if (!authorizationService.canRead(repoPath)) {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            log.error("Forbidden UI REST call from user: '{}'", authorizationService.currentUsername());
            return;
        }
        // get Cran Metadata
        CranArtifactInfo cranMetaData = getCranMetaData(cranArtifactInfo, repoPath);
        // Update response with model data
        if (cranMetaData != null) {
            response.iModel(cranMetaData);
        }
    }

    /**
     * @return Cran Metadata model
     */
    private CranArtifactInfo getCranMetaData(CranArtifactInfo cranArtifactInfo, RepoPath repoPath) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        CranAddon cranAddon = addonsManager.addonByType(CranAddon.class);
        if (cranAddon != null) {
            CranMetadataInfo cranMetadata = cranAddon.getCranMetadataToUiModel(repoPath);
            cranArtifactInfo.setCranInfo(cranMetadata.getCranInfo());
            cranArtifactInfo.setCranDependencies(cranMetadata.getDependencies());
            cranArtifactInfo.setCranImports(cranMetadata.getImports());
            cranArtifactInfo.setCranSuggests(cranMetadata.getSuggests());
        }
        return cranArtifactInfo;
    }
}
