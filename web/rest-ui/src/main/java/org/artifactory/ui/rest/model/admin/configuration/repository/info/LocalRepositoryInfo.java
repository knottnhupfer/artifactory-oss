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

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.replication.ReplicationBaseDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.ui.utils.RegExUtils;

/**
 * @author Aviad Shikloshi
 */
public class LocalRepositoryInfo extends RepositoryInfo {

    private Boolean replications;

    public LocalRepositoryInfo() {
    }

    public LocalRepositoryInfo(LocalRepoDescriptor repoDescriptor, CentralConfigService configService) {
        repoKey = repoDescriptor.getKey();
        repoType = repoDescriptor.getType().toString();
        // checks if there is at least one enabled replication
        replications = (configService.getDescriptor().getMultiLocalReplications(repoKey).stream()
                .anyMatch(ReplicationBaseDescriptor::isEnabled));
        hasReindexAction = RegExUtils.LOCAL_REPO_REINDEX_PATTERN.matcher(repoType).matches();
    }

    public Boolean getReplications() {
        return replications;
    }

    public void setReplications(Boolean replications) {
        this.replications = replications;
    }
}
