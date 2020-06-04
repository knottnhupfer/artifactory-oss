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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.gemsview;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.gems.ArtifactGemsInfo;
import org.artifactory.addon.gems.GemsAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.gems.GemsArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.gems.GemsDependency;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.gems.GemsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GemsViewService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(GemsViewService.class);

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        GemsArtifactInfo gemsArtifactInfo = (GemsArtifactInfo) request.getImodel();
        // fetch gems info
        fetchGemsInfo(gemsArtifactInfo, response);
        //update artifactory response with model data
    }

    /**
     * fetch gems info meta data
     */
    private void fetchGemsInfo(GemsArtifactInfo gemsArtifactInfo, RestResponse response) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        GemsAddon gemsAddon = addonsManager.addonByType(GemsAddon.class);
        if (gemsAddon != null) {
            String repoKey = gemsArtifactInfo.getRepoKey();
            String path = gemsArtifactInfo.getPath();
            RepoPath repoPath = InternalRepoPathFactory
                    .create(repoKey, path);
            if(repositoryService.isVirtualRepoExist(repoPath.getRepoKey())){
                repoPath = repositoryService.getVirtualFileInfo(repoPath).getRepoPath();
            }
            // read permission checks
            if (!authorizationService.canRead(repoPath)) {
                response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
                log.error("Forbidden UI REST call from user: '{}'", authorizationService.currentUsername());
                return;
            }
            ArtifactGemsInfo gemsInfo = gemsAddon.getGemsInfo(repoPath.getRepoKey(), repoPath.getPath());
            GemsInfo artifactGemsInfo = new GemsInfo(gemsInfo, repoPath.getRepoKey(), repoPath.getPath());
            gemsArtifactInfo.clearRepoData();
            gemsArtifactInfo.setGemsInfo(artifactGemsInfo);
            List<GemsDependency> gemsDependencies = new ArrayList<>();
            gemsInfo.getDependencies().getDevelopment().forEach(dev ->
                    gemsDependencies.add(new GemsDependency(dev.getName(), dev.getRequirements(), "Development")));
            gemsInfo.getDependencies().getRuntime().forEach(runtime ->
                    gemsDependencies
                            .add(new GemsDependency(runtime.getName(), runtime.getRequirements(), "Runtime")));
            gemsArtifactInfo.setGemsDependencies(gemsDependencies);
            response.iModel(gemsArtifactInfo);

        } else {
            response.iModel(gemsArtifactInfo);
        }
    }
}
