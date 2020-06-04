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

import com.google.common.collect.ImmutableList;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.repo.RepoPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

/**
 * @author Dan Feldman
 */
@Component
public class BuildDistributionFilters {

    private ImmutableList<BuildPathsDistributionFilter> pathFilters = ImmutableList.of(new DockerBuildPathsDistributionFilter());

    private RepositoryService repoService;

    @Autowired
    public BuildDistributionFilters(RepositoryService repoService) {
        this.repoService = repoService;
    }

    public Predicate<RepoPath> filter() {
        return path -> {
            for (BuildPathsDistributionFilter pathFilter : pathFilters) {
                if (!pathFilter.filter(path, repoService)) {
                    return false;
                }
            }
            return true;
        };
    }
}
