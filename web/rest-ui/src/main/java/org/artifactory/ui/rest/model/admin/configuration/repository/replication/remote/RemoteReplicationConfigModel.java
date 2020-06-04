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

package org.artifactory.ui.rest.model.admin.configuration.repository.replication.remote;

import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.RepositoryReplicationConfigModel;
import org.codehaus.jackson.annotate.JsonTypeName;

import static org.artifactory.repo.config.RepoConfigDefaultValues.*;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
@JsonTypeName("remote")
public class RemoteReplicationConfigModel implements RepositoryReplicationConfigModel {

    protected Boolean enabled = DEFAULT_REMOTE_REPLICATION_ENABLED;
    protected String cronExp;
    protected Boolean syncDeletes = DEFAULT_REPLICATION_SYNC_DELETES;
    protected Boolean enableEventReplication = DEFAULT_EVENT_REPLICATION;
    protected Boolean syncProperties = DEFAULT_SYNC_PROPERTIES;
    protected String pathPrefix;

    @Override
    public Boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getCronExp() {
        return cronExp;
    }

    @Override
    public void setCronExp(String cronExp) {
        this.cronExp = cronExp;
    }

    @Override
    public Boolean isEnableEventReplication() {
        return enableEventReplication;
    }

    @Override
    public void setEnableEventReplication(Boolean enableEventReplication) {
        this.enableEventReplication = enableEventReplication;
    }

    @Override
    public Boolean isSyncDeletes() {
        return syncDeletes;
    }

    @Override
    public void setSyncDeletes(Boolean syncDeletes) {
        this.syncDeletes = syncDeletes;
    }

    public Boolean isSyncProperties() {
        return syncProperties;
    }

    public void setSyncProperties(Boolean syncProperties) {
        this.syncProperties = syncProperties;
    }

    @Override
    public String getPathPrefix() {
        return pathPrefix;
    }

    @Override
    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
