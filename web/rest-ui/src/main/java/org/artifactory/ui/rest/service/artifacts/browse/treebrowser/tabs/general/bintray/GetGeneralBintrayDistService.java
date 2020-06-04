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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general.bintray;

import org.artifactory.api.bintray.distribution.DistributionService;
import org.artifactory.api.bintray.distribution.model.BintrayDistInfoModel;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.artifact.BaseArtifact;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.bintray.BintrayDistUIModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetGeneralBintrayDistService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetGeneralBintrayDistService.class);

    @Autowired
    private DistributionService distributionService;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BaseArtifact baseArtifact = (BaseArtifact) request.getImodel();
        String repoKey = baseArtifact.getRepoKey();
        String path = baseArtifact.getPath();
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        if(repositoryService.isVirtualRepoExist(repoPath.getRepoKey())){
            repoPath = repositoryService.getVirtualFileInfo(repoPath).getRepoPath();
        }
        try {
            BintrayDistInfoModel model = distributionService.buildInfoModel(repoPath);
            if (model == null) {
                response.iModel(new BintrayDistUIModel()); // show by default on false
            } else {
                response.iModel(new BintrayDistUIModel(model));
            }
        } catch (IOException e) {
            String msg = "Error while parsing Bintray response: " + e.getMessage();
            response.error(msg);
            response.iModel(new BintrayDistUIModel(msg));
            log.error(msg, e);
        }
    }
}
