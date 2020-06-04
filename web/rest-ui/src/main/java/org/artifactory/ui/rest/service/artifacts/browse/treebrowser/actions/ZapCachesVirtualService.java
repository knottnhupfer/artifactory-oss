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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions;

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.StatusHolder;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.ZapArtifact;
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
public class ZapCachesVirtualService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(ZapCachesVirtualService.class);

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ZapArtifact zapArtifact = (ZapArtifact) request.getImodel();
        String repoKey = zapArtifact.getRepoKey();
        String path = zapArtifact.getPath();
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        // delete permission checks
        if (!authorizationService.canDelete(repoPath)) {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            log.error("Forbidden UI REST call from user: '{}'", authorizationService.currentUsername());
            return;
        }
        // un-deploy virtual
        StatusHolder statusHolder = repositoryService.undeploy(repoPath, false);
        // update response status
        updateResponseStatus(response, repoKey, statusHolder);
    }

    /**
     * update response with zap caches results
     *
     * @param response     - encapsulate data related to response
     * @param repoKey      - repo key
     * @param statusHolder - zap caches action status holder
     */
    private void updateResponseStatus(RestResponse response, String repoKey, StatusHolder statusHolder) {
        if (!statusHolder.isError()) {
            response.info("The caches of '" + repoKey + "' have been successfully zapped.");
        } else {
            String message = "Could not zap caches for the virtual repository '" + repoKey + "': " +
                    statusHolder.getStatusMsg() + "";
            response.error(message);
        }
    }
}
