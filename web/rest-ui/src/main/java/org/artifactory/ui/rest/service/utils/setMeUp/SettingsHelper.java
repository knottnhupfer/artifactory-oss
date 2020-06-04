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

package org.artifactory.ui.rest.service.utils.setMeUp;

import com.google.common.collect.Lists;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lior Hasson
 */
public class SettingsHelper {

    /**
     * Returns a list of virtual repositories that a readable by the current user
     *
     * @return Readable virtual repository definitions
     */
    public static List<RepoDescriptor> getReadableVirtualRepoDescriptors(RepositoryService repositoryService,
            AuthorizationService authorizationService) {

        List<RepoDescriptor> readableDescriptors = Lists.newLinkedList();

        List<VirtualRepoDescriptor> virtualRepoDescriptors = repositoryService.getVirtualRepoDescriptors();

        readableDescriptors.addAll(virtualRepoDescriptors.stream()
                        .filter(virtualRepo -> isVirtualRepoReadable(null, virtualRepo, authorizationService))
                        .collect(Collectors.toList())
        );
        return readableDescriptors;
    }

    /**
     * Returns a list of Remote repositories that a readable by the current user
     *
     * @return Readable Remote repository definitions
     */
    public static List<RepoDescriptor> getReadableLocalRepoDescriptors(RepositoryService repositoryService,
            AuthorizationService authorizationService) {

        List<RepoDescriptor> readableDescriptors = Lists.newLinkedList();

        List<LocalRepoDescriptor> RemoteRepoDescriptors = repositoryService.getLocalRepoDescriptors();

        readableDescriptors.addAll(RemoteRepoDescriptors.stream()
                        .filter(remoteRepo ->
                                isLocalRepoReadable(remoteRepo, authorizationService))
                        .collect(Collectors.toList())
        );

        return readableDescriptors;
    }

    /**
     * Determine if the current user has read permissions on the given virtual
     *
     * @param parentVirtualRepo     Parent virtual repo if exists. Null if not
     * @param virtualRepoDescriptor Virtual repo to test
     * @return True if is readable. False if not
     */
    public static boolean isVirtualRepoReadable(VirtualRepoDescriptor parentVirtualRepo,
            VirtualRepoDescriptor virtualRepoDescriptor, AuthorizationService authorizationService) {
        List<RepoDescriptor> aggregatedRepos = virtualRepoDescriptor.getRepositories();
        for (RepoDescriptor aggregatedRepo : aggregatedRepos) {

            if (validateReadable(parentVirtualRepo, aggregatedRepo, authorizationService)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if the current user has read permissions on the given Local
     *
     * @param localRepoDescriptor   Local repo to test
     * @return True if is readable. False if not
     */
    public static boolean isLocalRepoReadable(LocalRepoDescriptor localRepoDescriptor,
            AuthorizationService authorizationService) {
        return validateReadable(null, localRepoDescriptor, authorizationService);

    }

    public static boolean validateReadable(VirtualRepoDescriptor parentVirtualRepo, RepoDescriptor aggregatedRepo,
            AuthorizationService authorizationService) {
        String key = aggregatedRepo.getKey();
        if (aggregatedRepo instanceof HttpRepoDescriptor && aggregatedRepo.getType().isMavenGroup()) {
            return authorizationService.canRead(InternalRepoPathFactory.repoRootPath(key + "-cache"));
        } else if ((aggregatedRepo instanceof VirtualRepoDescriptor) &&
                aggregatedRepo.getType().isMavenGroup() &&
                ((parentVirtualRepo == null) || !aggregatedRepo.equals(parentVirtualRepo))) {
            VirtualRepoDescriptor virtualRepo = ((VirtualRepoDescriptor) aggregatedRepo);
            return isVirtualRepoReadable(virtualRepo, virtualRepo, authorizationService);
        } else {
            return authorizationService.canRead(InternalRepoPathFactory.repoRootPath(key))
                    && aggregatedRepo.getType().isMavenGroup();
        }
    }
}
