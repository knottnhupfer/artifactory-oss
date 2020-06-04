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

package org.artifactory.rest.common.util.build.distribution;

import org.artifactory.api.bintray.distribution.Distribution;
import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;
import org.jfrog.build.api.Build;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Dan Feldman
 */
@Component
public class BuildDistributionHelper {

    private BuildService buildService;
    private RepositoryService repositoryService;
    private BuildDistributionFilters pathFilters;

    @Autowired
    public BuildDistributionHelper(BuildService buildService, RepositoryService repositoryService, BuildDistributionFilters pathFilters) {
        this.buildService = buildService;
        this.repositoryService = repositoryService;
        this.pathFilters = pathFilters;
    }

    /**
     * Executes a build artifact search that excludes results from all distribution repos to avoid the search
     * finding artifacts that were already distributed which will cause the distribution to deploy them in their already
     * existing paths in Bintray (because they already have the bintray coordinates properties)
     */
    public void populateBuildPaths(Build build, Distribution distribution, DistributionReporter status) {
        BasicStatusHolder searchStatus = new BasicStatusHolder();
        searchStatus.setActivateLogging(!distribution.isDryRun());
        List<String> buildArtifactPaths =
                buildService.collectBuildArtifacts(build, distribution.getSourceRepos(), getAllDistRepoKeys(), searchStatus)
                        .stream()
                        .filter(Objects::nonNull)
                        .map(FileInfo::getRepoPath)
                        .filter(pathFilters.filter())
                        .map(RepoPath::toPath)
                        .collect(Collectors.toList());
        distribution.setPackagesRepoPaths(buildArtifactPaths);
        status.merge(searchStatus);
    }

    private List<String> getAllDistRepoKeys() {
        return repositoryService.getDistributionRepoDescriptors()
                .stream()
                .map(RepoDescriptor::getKey)
                .collect(Collectors.toList());
    }
}
