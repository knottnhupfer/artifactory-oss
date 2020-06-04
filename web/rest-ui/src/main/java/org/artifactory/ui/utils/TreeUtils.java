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

package org.artifactory.ui.utils;

import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.SnapshotVersionBehavior;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;

import static org.artifactory.mime.MavenNaming.isNonUniqueSnapshot;
import static org.artifactory.mime.MavenNaming.isUniqueSnapshot;

/**
 * @author Noam Y. Tenne
 */
public class TreeUtils {

    /**
     * Indicates whether a link to the tree item of the deployed artifact should be provided. Links should be
     * provided if deploying a snapshot file to repository with different snapshot version policy.
     *
     * @param artifactPath The artifact deploy path
     * @return True if should provide the link
     */
    public static boolean shouldProvideTreeLink(RepoBaseDescriptor repo, String artifactPath) {
        if (repo instanceof LocalRepoDescriptor) {
            return shouldProvideTreeLinkOnLocalRepo((LocalRepoDescriptor) repo, artifactPath);
        } else if (repo instanceof VirtualRepoDescriptor) {
            LocalRepoDescriptor defaultDeploymentRepo = ((VirtualRepoDescriptor) repo).getDefaultDeploymentRepo();
            if (defaultDeploymentRepo != null) {
                return shouldProvideTreeLinkOnLocalRepo(defaultDeploymentRepo, artifactPath);
            }
        }
        return false;
    }

    private static boolean shouldProvideTreeLinkOnLocalRepo(LocalRepoDescriptor repo, String artifactPath) {
        SnapshotVersionBehavior repoSnapshotBehavior = repo.getSnapshotVersionBehavior();
        boolean uniqueToNonUnique = isUniqueSnapshot(artifactPath)
                && SnapshotVersionBehavior.NONUNIQUE.equals(repoSnapshotBehavior);
        boolean nonUniqueToUniqueRepo = isNonUniqueSnapshot(artifactPath)
                && SnapshotVersionBehavior.UNIQUE.equals(repoSnapshotBehavior);
        return !uniqueToNonUnique && !nonUniqueToUniqueRepo;
    }
}
