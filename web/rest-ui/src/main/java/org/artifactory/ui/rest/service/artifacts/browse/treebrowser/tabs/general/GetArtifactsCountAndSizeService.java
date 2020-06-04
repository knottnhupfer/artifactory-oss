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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general;

import com.google.common.collect.ImmutableMap;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.storage.FolderSummeryInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.BaseInfo;
import org.jfrog.storage.common.StorageUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Aviad Shikloshi
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetArtifactsCountAndSizeService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetArtifactsCountAndSizeService.class);

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BaseInfo baseInfo = (BaseInfo) request.getImodel();
        try {
            RepoPath repoPath = RepoPathFactory.create(baseInfo.getRepositoryPath());
            FolderSummeryInfo folderSummeryInfo = repositoryService.getArtifactCountAndSize(repoPath);
            response.iModel(ImmutableMap.of("artifactsCount", folderSummeryInfo.getFileCount(), "artifactSize",
                    StorageUnit.toReadableString(folderSummeryInfo.getFolderSize())));
        } catch (Exception e) {
            log.error("Error while counting and calculating the size of artifacts .", e);
            response.error("Unable to count and calculate the size of artifacts.");
        }
    }
}
