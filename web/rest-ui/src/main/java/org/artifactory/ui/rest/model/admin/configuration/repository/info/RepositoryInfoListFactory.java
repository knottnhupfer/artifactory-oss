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

package org.artifactory.ui.rest.model.admin.configuration.repository.info;

import com.google.common.collect.Lists;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;

import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.repo.RepoDetailsType.*;

/**
 * @author Aviad Shikloshi
 */
public class RepositoryInfoListFactory {

    public static List<RepositoryInfo> createRepositoryInfo(String repoType, CentralConfigService configService,
            RepositoryService repositoryService) {
        List<RepositoryInfo> repoInfo;
        switch (repoType) {
            case LOCAL_REPO:
                repoInfo = repositoryService.getLocalRepoDescriptors().stream()
                        .map(repoDesc -> new LocalRepositoryInfo(repoDesc, configService))
                        .collect(Collectors.toList());
                break;
            case REMOTE_REPO:
                List<RemoteRepoDescriptor> remoteRepoDescriptorList = repositoryService.getRemoteRepoDescriptors();
                repoInfo = remoteRepoDescriptorList.stream()
                        .map(repoDesc -> new RemoteRepositoryInfo(repoDesc, configService.getDescriptor()))
                        .collect(Collectors.toList());
                break;
            case VIRTUAL_REPO:
                List<VirtualRepoDescriptor> virtualRepoDescriptorList = repositoryService.getVirtualRepoDescriptors();
                repoInfo = virtualRepoDescriptorList.stream()
                        .map(VirtualRepositoryInfo::new)
                        .collect(Collectors.toList());
                break;
            case DISTRIBUTION_REPO:
                repoInfo = repositoryService.getDistributionRepoDescriptors().stream()
                        .map(DistributionRepositoryInfo::new)
                        .collect(Collectors.toList());
                repoInfo.addAll(repositoryService.getReleaseBundlesRepoDescriptors().stream()
                        .map(ReleaseBundlesRepositoryInfo::new)
                        .collect(Collectors.toList()));
                break;
            default:
                repoInfo = Lists.newArrayList();
        }
        return repoInfo;
    }

    private RepositoryInfoListFactory() {
    }

}
