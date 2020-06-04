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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions.distribution;

import org.apache.http.HttpStatus;
import org.artifactory.api.bintray.distribution.Distribution;
import org.artifactory.api.bintray.distribution.Distributor;
import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.aql.util.AqlSearchablePath;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.model.artifact.BaseArtifact;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.rest.common.model.distribution.DistributionResponseBuilder.doResponse;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DistributeArtifactService implements RestService {

    @Autowired
    private Distributor distributor;

    @Autowired
    private RepositoryService repoService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        boolean isDryRun = Boolean.parseBoolean(request.getQueryParamByKey("dryRun"));
        boolean async = Boolean.parseBoolean(request.getQueryParamByKey("async"));
        boolean publish = Boolean.parseBoolean(request.getQueryParamByKey("publish"));
        boolean overrideExistingFiles = Boolean.parseBoolean(request.getQueryParamByKey("overrideExistingFiles"));
        String targetRepo = request.getQueryParamByKey("targetRepo");
        BaseArtifact artifact = (BaseArtifact) request.getImodel();
        RepoPath artifactRepoPath = RepoPathFactory.create(artifact.getRepoKey() + "/" + artifact.getPath());
        if (!repoService.exists(artifactRepoPath)) {
            response.error("No such path " + artifactRepoPath.toPath()).responseCode(HttpStatus.SC_NOT_FOUND);
            return;
        }
        Distribution distribution = new Distribution();
        if (repoService.getItemInfo(artifactRepoPath).isFolder()) {
            getPathsForCurrentFolderAndSubFolders(artifactRepoPath).forEach(distribution::addPath);
        } else {
            distribution.addPath(artifactRepoPath.toPath());
        }
        distribution.setTargetRepo(targetRepo);
        distribution.setAsync(async);
        distribution.setDryRun(isDryRun);
        distribution.setPublish(publish);
        distribution.setOverrideExistingFiles(overrideExistingFiles);
        DistributionReporter status = distributor.distribute(distribution);
        String performedOn = "The requested paths";
        doResponse(response, performedOn, distribution, status);
    }

    private List<String> getPathsForCurrentFolderAndSubFolders(RepoPath artifactRepoPath) {
        return AqlUtils.getSearchablePathForCurrentFolderAndSubfolders(artifactRepoPath).stream()
                .map(AqlSearchablePath::toRepoPath)
                .map(RepoPath::toPath)
                .collect(Collectors.toList());
    }
}
