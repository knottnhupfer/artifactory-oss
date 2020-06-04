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

import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;

/**
 * @author Dan Feldman
 */
public class DistributionRepositoryInfo extends RepositoryInfo {

    private String visibility;

    public DistributionRepositoryInfo() {
    }

    public DistributionRepositoryInfo(DistributionRepoDescriptor descriptor) {
        repoKey = descriptor.getKey();
        repoType = descriptor.getType().toString();
        hasReindexAction = false;
        visibility = descriptor.isDefaultNewRepoPrivate() ? "Private" : "Public";
    }

    public String getVisibility() {
        return visibility;
    }
}
