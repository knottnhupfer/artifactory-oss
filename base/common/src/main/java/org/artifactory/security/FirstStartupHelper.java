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

package org.artifactory.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.artifactory.addon.build.BuildAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;

import java.util.Set;

import static org.artifactory.repo.config.RepoConfigDefaultValues.EXAMPLE_REPO_KEY;

/**
 * A helper that provides information about Artifactory's state - is it the first time Artifactory is starting
 *
 * @author Yuval Reches
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FirstStartupHelper {

    public static boolean onlyDefaultReposExist(CentralConfigDescriptor descriptor) {
        Set<String> allLocalRepoKeys = descriptor.getLocalRepositoriesMap().keySet();
        RepoPath exampleRepoRoot = RepoPathFactory.create(EXAMPLE_REPO_KEY, "");
        RepoPath buildInfoRepoRoot = RepoPathFactory.create(BuildAddon.BUILD_INFO_REPO_NAME, "");

        // only example and build-info repos exist and they're empty
        return descriptor.getRemoteRepositoriesMap().size() == 0
                && descriptor.getVirtualRepositoriesMap().size() == 0
                && descriptor.getDistributionRepositoriesMap().size() == 0
                && allLocalRepoKeys.size() == 2
                && allLocalRepoKeys.stream().allMatch(repoKey -> BuildAddon.BUILD_INFO_REPO_NAME.equals(repoKey) || EXAMPLE_REPO_KEY.equals(repoKey))
                && repoService().getArtifactCount(exampleRepoRoot) == 0
                && repoService().getArtifactCount(buildInfoRepoRoot) == 0;
    }

    //this is here because it has to be passed all the way from CentralConfigService and I don't want to create (yet) another cycle
    private static RepositoryService repoService() {
        return ContextHelper.get().getRepositoryService();
    }

}
