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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.builds;

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.build.BuildAddon;
import org.artifactory.addon.build.artifacts.ProducedBy;
import org.artifactory.addon.build.artifacts.UsedBy;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.search.SearchService;
import org.artifactory.build.BuildRun;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.builds.BuildsArtifactInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetArtifactBuildsService implements RestService {

    private RepositoryService repositoryService;
    private SearchService searchService;
    private AddonsManager addonsManager;

    @Autowired
    public GetArtifactBuildsService(RepositoryService repositoryService, SearchService searchService, AddonsManager addonsManager) {
        this.repositoryService = repositoryService;
        this.searchService = searchService;
        this.addonsManager = addonsManager;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String path = request.getQueryParamByKey("path");
        String repoKey = request.getQueryParamByKey("repoKey");
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        if(repositoryService.isVirtualRepoExist(repoPath.getRepoKey())){
            repoPath = repositoryService.getVirtualFileInfo(repoPath).getRepoPath();
        }
        FileInfo buildFileInfo = getFileInfo(repoPath);
        String sha1 = buildFileInfo.getSha1();
        String sha2 = buildFileInfo.getSha2();
        String md5 = buildFileInfo.getMd5();
        List<BuildRun> dependencyBuilds = getDependencyBuilds(sha1, sha2, md5);
        List<BuildRun> producedByBuilds = getArtifactBuilds(sha1, sha2, md5);
        List<ProducedBy> producedBy = Lists.newArrayList();
        List<UsedBy> usedBy = Lists.newArrayList();
        addonsManager.addonByType(BuildAddon.class).populateArtifactBuildInfo(buildFileInfo, dependencyBuilds, producedByBuilds, producedBy, usedBy);
        updateResponseData(response, producedBy, usedBy);
    }
    /**
     * update response with artifact build info
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param producedByList       - produce by data list
     * @param usedByList          - used by data list
     */
    private void updateResponseData(RestResponse artifactoryResponse, List<ProducedBy> producedByList, List<UsedBy> usedByList) {
        BuildsArtifactInfo buildsArtifactInfo = new BuildsArtifactInfo(producedByList, usedByList);
        artifactoryResponse.iModel(buildsArtifactInfo);
    }

    /**
     * get File info by repo path
     *
     * @param repoPath - repo path
     * @return file info instance
     */
    private FileInfo getFileInfo(RepoPath repoPath) {
        ItemInfo fileInfo = repositoryService.getItemInfo(repoPath);
        return (FileInfo) fileInfo;
    }

    private List<BuildRun> getArtifactBuilds(String sha1, String sha2, String md5) {
        return Lists.newArrayList(searchService.findBuildsByArtifactChecksum(sha1, sha2, md5));
    }

    private List<BuildRun> getDependencyBuilds(String sha1, String sha2, String md5) {
        return Lists.newArrayList(searchService.findBuildsByDependencyChecksum(sha1, sha2, md5));
    }
}
