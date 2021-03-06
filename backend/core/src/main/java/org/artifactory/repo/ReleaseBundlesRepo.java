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

package org.artifactory.repo;

import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.repo.db.DbLocalRepo;
import org.artifactory.repo.service.InternalRepositoryService;

/**
 * @author Nadav Yogev
 */
public class ReleaseBundlesRepo extends DbLocalRepo<ReleaseBundlesRepoDescriptor> {

    public static final String RELEASE_BUNDLE_REPO_COPY_MOVE_ERROR_MSG =
            "Moving or Copying from and to a Release Bundles repositories are not allowed.";

    public ReleaseBundlesRepo(ReleaseBundlesRepoDescriptor descriptor,
            InternalRepositoryService repositoryService,
            DbLocalRepo<ReleaseBundlesRepoDescriptor> oldLocalRepo) {
        super(descriptor, repositoryService, oldLocalRepo);
    }
}
