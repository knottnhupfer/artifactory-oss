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

package org.artifactory.rest.common.util;

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.exception.ForbiddenWebAppException;
import org.artifactory.security.ArtifactoryPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

/**
 * Helper that check if a user has permission on a repo. In case of virtual, it checks if a user has permission on
 * All of the virtual aggregated repos
 *
 * @author Shay Bagants
 */
@Component
public class PermissionHelper {

    @Autowired
    private RepositoryService repoService;

    @Autowired
    private AuthorizationService authService;

    public void assertPermission(String repositoryPath, ArtifactoryPermission permission) {
        assertPermission(repositoryPath, permission, false);
    }

    /**
     * Assert the user has permission on the given repositoryPath.
     *
     * @param checkOnlyDeploymentRepoForVirtual this flag affects assertion on virtual repos only.
     *                                          if true then the permission will be checked only on the default deployment repo of the virtual repo,
     *                                          else the permission will be checked on all repos under the virtual repo.
     */
    public void assertPermission(String repositoryPath, ArtifactoryPermission permission, boolean checkOnlyDeploymentRepoForVirtual) {
        RepoPath repoPath = RepoPathFactory.create(repositoryPath);
        String repoKey = repoPath.getRepoKey();
        RepoDescriptor repoDescriptor = repoService.repoDescriptorByKey(repoKey);
        if (repoDescriptor == null) {
            throw new NotFoundException(Response.status(Response.Status.NOT_FOUND).entity("Repository '" + repoKey + "' not found").build());
        }

        if (repoDescriptor.isReal()) {
            assertPermissionForRealRepo(repoPath, permission);
        } else {
            if (checkOnlyDeploymentRepoForVirtual) {
                assertPermissionForVirtualDefaultDeploymentRepo(repoPath, (VirtualRepoDescriptor)repoDescriptor, permission);
            } else {
                assertPermissionForVirtual(repoPath, permission);
            }
        }
    }

    private void assertPermissionForVirtualDefaultDeploymentRepo(RepoPath repoPath, VirtualRepoDescriptor repoDescriptor, ArtifactoryPermission permission) {
        LocalRepoDescriptor defaultDeploymentRepo = repoDescriptor.getDefaultDeploymentRepo();
        if (defaultDeploymentRepo == null) {
            throw new ForbiddenWebAppException("No local repository was configured as local deployment repository for the (" + repoDescriptor.getKey() + ") virtual repository.");
        }
        assertPermissionForRealRepo(defaultDeploymentRepo, repoPath, permission);
    }

    private void assertPermissionForVirtual(RepoPath repoPath, ArtifactoryPermission permission) {
        repoService.getVirtualResolvedLocalAndCacheDescriptors(repoPath.getRepoKey())
                .forEach(localRepoDescriptor -> assertPermissionForRealRepo(localRepoDescriptor, repoPath, permission));
    }

    private void assertPermissionForRealRepo(RepoDescriptor localRepoDescriptor, RepoPath repoPath, ArtifactoryPermission permission) {
        RepoPath pathToCheck = RepoPathFactory.create(localRepoDescriptor.getKey(), repoPath.getPath());
        assertPermissionForRealRepo(pathToCheck, permission);
    }

    private void assertPermissionForRealRepo(RepoPath repoPath, ArtifactoryPermission permission) {
        boolean hasPermission = false;
        switch (permission) {
            case READ:
                hasPermission = authService.canRead(repoPath);
                break;
            case ANNOTATE:
                hasPermission = authService.canAnnotate(repoPath);
                break;
            case DEPLOY:
                hasPermission = authService.canDeploy(repoPath);
                break;
            case DELETE:
                hasPermission = authService.canDelete(repoPath);
                break;
            case MANAGE:
                hasPermission = authService.canManage(repoPath);
                break;
        }
        if (!hasPermission) {
            throw new ForbiddenWebAppException();
        }
    }
}
