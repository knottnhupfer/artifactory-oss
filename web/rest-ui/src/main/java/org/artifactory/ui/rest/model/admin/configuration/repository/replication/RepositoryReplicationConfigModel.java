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

package org.artifactory.ui.rest.model.admin.configuration.repository.replication;

import org.artifactory.rest.common.model.RestModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.remote.RemoteReplicationConfigModel;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LocalReplicationConfigModel.class, name = "localReplication"),
        @JsonSubTypes.Type(value = RemoteReplicationConfigModel.class, name = "remoteReplication")
})
public interface RepositoryReplicationConfigModel extends RestModel {

    Boolean isEnabled();

    void setEnabled(Boolean enabled);

    String getCronExp();

    void setCronExp(String cronExp);

    String getPathPrefix();

    void setPathPrefix(String pathPrefix);

    Boolean isSyncDeletes();

    void setSyncDeletes(Boolean syncDeletes);

    Boolean isEnableEventReplication();

    void setEnableEventReplication(Boolean enableEventReplication);
}
