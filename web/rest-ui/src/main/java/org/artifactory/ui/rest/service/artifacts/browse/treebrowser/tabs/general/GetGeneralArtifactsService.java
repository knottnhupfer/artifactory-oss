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

import org.apache.http.HttpStatus;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.VirtualRepoItem;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.RestGeneralTab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetGeneralArtifactsService implements RestService {

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private RepositoryService repoService;

    @Autowired
    private RepositoryBrowsingService browsingService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RestGeneralTab generalTab = (RestGeneralTab) request.getImodel();
        RepoPath repoPath = generalTab.retrieveRepoPath();
        if (repoService.virtualRepoDescriptorByKey(repoPath.getRepoKey()) != null) {
            VirtualRepoItem item = browsingService.getVirtualRepoItem(repoPath);
            if (item != null && item.getItemInfo() != null) {
                if (item.getItemInfo().getRepoPath() == null) {
                    response.error("Unauthorized").responseCode(HttpStatus.SC_FORBIDDEN);
                    return;
                }
            }
            generalTab.populateGeneralData(item);
        } else {
            if (!authorizationService.canRead(repoPath)) {
                response.error("Unauthorized").responseCode(HttpStatus.SC_FORBIDDEN);
                return;
            }
            generalTab.populateGeneralData();
        }
        // populate  general tab data
        // update response data
        response.iModel(generalTab);
    }
}
