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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.bower;

import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.bower.BowerAddon;
import org.artifactory.addon.bower.BowerMetadataInfo;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.bower.BowerArtifactInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BowerViewService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(BowerViewService.class);

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BowerArtifactInfo bowerArtifactInfo = (BowerArtifactInfo) request.getImodel();
        String path = bowerArtifactInfo.getPath();
        String repoKey = bowerArtifactInfo.getRepoKey();
        RepoPath repoPath = RepoPathFactory.create(repoKey, path);
        if(repositoryService.isVirtualRepoExist(repoPath.getRepoKey())){
            repoPath = repositoryService.getVirtualFileInfo(repoPath).getRepoPath();
        }
        if (authorizationService.canRead(repoPath)) {
            // get bower meta data model
            BowerArtifactInfo bowerArtifactMetadata = getBowerArtifactInfoModel(repoPath.getPath(), repoPath.getRepoKey());
            // update response with model
            response.iModel(bowerArtifactMetadata);
        } else {
            response.responseCode(HttpStatus.SC_FORBIDDEN);
            log.error("Forbidden UI REST call from user: '{}'", authorizationService.currentUsername());
        }
    }

    /**
     * get bower artifact info metadata model
     *
     * @param path    - ppath
     * @param repoKey - repo key
     * @return bower meta data artifact info
     */
    private BowerArtifactInfo getBowerArtifactInfoModel(String path, String repoKey) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        BowerAddon bowerAddon = addonsManager.addonByType(BowerAddon.class);
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        FileInfo fileInfo = repositoryService.getFileInfo(repoPath);
        BowerMetadataInfo bowerMetadata = bowerAddon.getBowerMetadata(fileInfo.getRepoPath());
        if (bowerMetadata != null) {
            return new BowerArtifactInfo(bowerMetadata);
        }
        return null;
    }
}
