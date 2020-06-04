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

package org.artifactory.component;

import org.artifactory.api.component.ComponentDetails;
import org.artifactory.api.component.ComponentDetailsFetcher;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.repo.RepoPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Gal Ben Ami
 */
@Component
public class ComponentDetailsFetcherImpl implements ComponentDetailsFetcher {

    @Autowired
    private Map<String, RepoComponentDetailsFetcher> componentDetailsFetchers;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public ComponentDetails calcComponentDetails(RepoPath repoPath) {
        if (!repoPath.isFile()) {
            throw new IllegalArgumentException("File not found:" + repoPath);
        }
        String repoKey = repoPath.getRepoKey();
        RepoDescriptor repoDescriptor = repositoryService.repoDescriptorByKey(repoKey);
        String type = repoDescriptor.getType().getType();
        RepoComponentDetailsFetcher fetcher = componentDetailsFetchers.get(type + "ComponentDetailsFetcher");
        if (fetcher == null) {
            return ComponentDetails.getDefaultComponentDetails();
        }
        return fetcher.calcComponentDetails(repoPath);
    }
}
