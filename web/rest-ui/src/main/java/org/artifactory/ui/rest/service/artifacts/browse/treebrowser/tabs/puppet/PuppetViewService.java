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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.puppet;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.puppet.PuppetAddon;
import org.artifactory.addon.puppet.PuppetInfo;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.puppet.PuppetArtifactInfoModel;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.puppet.PuppetDependencyInfoModel;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.puppet.PuppetKeywordInfoModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by jainishshah on 10/20/16.
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PuppetViewService implements RestService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        PuppetArtifactInfoModel puppetArtifactInfoModel = (PuppetArtifactInfoModel) request.getImodel();
        // fetch puppet meta data
        fetchPuppetMetaData(response, puppetArtifactInfoModel);
    }

    /**
     * fetch npm meta data
     *  @param artifactoryResponse - encapsulate data require for response
     * @param puppetArtifactInfoModel     - npm artifact info
     */
    private void fetchPuppetMetaData(RestResponse artifactoryResponse, PuppetArtifactInfoModel puppetArtifactInfoModel) {
        String repoKey = puppetArtifactInfoModel.getRepoKey();
        String path = puppetArtifactInfoModel.getPath();
        RepoPath repoPath = RepoPathFactory.create(repoKey, path);
        if(repositoryService.isVirtualRepoExist(repoPath.getRepoKey())){
            repoPath = repositoryService.getVirtualFileInfo(repoPath).getRepoPath();
        }
        /// get puppet add on
        PuppetAddon puppetAddon = addonsManager.addonByType(PuppetAddon.class);
        if (puppetAddon != null) {
            // get puppet meta data
            PuppetInfo puppetInfo = puppetAddon.getPuppetInfo(repoPath);
            puppetArtifactInfoModel.clearRepoData();
            puppetArtifactInfoModel.setPuppetInfo(puppetInfo);
            puppetArtifactInfoModel.setPuppetKeywords(keywordsToList(puppetInfo));
            puppetArtifactInfoModel.setPuppetDependencies(dependenciesToList(puppetInfo.getDependencies()));
            puppetInfo.setKeywords(null);
            puppetInfo.setDependencies(null);
            artifactoryResponse.iModel(puppetArtifactInfoModel);
        }
    }

    private List<PuppetKeywordInfoModel> keywordsToList(PuppetInfo puppetInfo) {
        return Optional.ofNullable(puppetInfo.getKeywords())
                .map(keywords -> keywords.stream()
                        .map(PuppetKeywordInfoModel::new).collect(Collectors.toList()))
                .orElse(null);
    }

    private List<PuppetDependencyInfoModel> dependenciesToList(Map<String, String> dependencies) {
        return Optional.ofNullable(dependencies)
                .map(dependencyMap -> dependencyMap.entrySet().stream()
                        .map(entry -> new PuppetDependencyInfoModel(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList()))
                .orElse(null);
    }

}
