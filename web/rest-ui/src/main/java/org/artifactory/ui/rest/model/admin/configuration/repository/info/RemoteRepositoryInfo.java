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

import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.ui.utils.RegExUtils;

/**
 * @author Aviad Shikloshi
 */
public class RemoteRepositoryInfo extends RepositoryInfo {

    private String url;
    private Boolean hasEnabledReplication; //for 'run now' in grid actions
    private Boolean blackedOut;
    private Boolean offline;

    public RemoteRepositoryInfo() {
    }

    public RemoteRepositoryInfo(RemoteRepoDescriptor remoteDesc, CentralConfigDescriptor configDescriptor) {
        repoKey = remoteDesc.getKey();
        repoType = remoteDesc.getType().toString();
        url = remoteDesc.getUrl();
        RemoteReplicationDescriptor replication = configDescriptor.getRemoteReplication(repoKey);
        hasEnabledReplication = (replication != null && replication.isEnabled());
        blackedOut = remoteDesc.isBlackedOut();
        offline = remoteDesc.isOffline();
        hasReindexAction = RegExUtils.REMOTE_REPO_REINDEX_PATTERN.matcher(repoType).matches();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean isHasEnabledReplication() {
        return hasEnabledReplication;
    }

    public void setHasEnabledReplication(Boolean replication) {
        this.hasEnabledReplication = replication;
    }

    public Boolean isBlackedOut() {
        return blackedOut;
    }

    public void setBlackedOut(Boolean enabled) {
        this.blackedOut = enabled;
    }

    public Boolean getOffline() {
        return offline;
    }

    public void setOffline(Boolean offline) {
        this.offline = offline;
    }
}
