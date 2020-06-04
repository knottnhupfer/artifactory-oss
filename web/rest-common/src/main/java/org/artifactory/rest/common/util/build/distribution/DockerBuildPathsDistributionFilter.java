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

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.RepoPath;

import static org.artifactory.mime.DockerNaming.MANIFEST_FILENAME;

/**
 * Allows implementation-specific (i.e. package-specific) filtration of build artifacts
 *
 * @author Dan Feldman
 */
public class DockerBuildPathsDistributionFilter implements BuildPathsDistributionFilter {

    @Override
    public boolean filter(RepoPath path, RepositoryService repoService) {
        RepoDescriptor repo = repoService.repoDescriptorByKey(path.getRepoKey());
        if (repo != null && RepoType.Docker.equals(repo.getType())) {
            return path.getPath().endsWith(MANIFEST_FILENAME);
        }
        return true;
    }
}
