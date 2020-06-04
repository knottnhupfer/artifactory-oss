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

package org.artifactory.api.rest.replication;

import org.artifactory.api.rest.restmodel.IModel;

import java.io.Serializable;
import java.util.List;

/**
 * @author Chen Keinan
 */
public class MultipleReplicationConfigRequest implements Serializable, IModel {

    private String cronExp;
    private Boolean enableEventReplication;
    private List<ReplicationConfigRequest> replications;

    public List<ReplicationConfigRequest> getReplications() {
        return replications;
    }

    public void setReplications(
            List<ReplicationConfigRequest> replications) {
        this.replications = replications;
    }

    public String getCronExp() {
        return cronExp;
    }

    public void setCronExp(String cronExp) {
        this.cronExp = cronExp;
    }

    public Boolean isEnableEventReplication() {
        return enableEventReplication;
    }

    public void setEnableEventReplication(Boolean enableEventReplication) {
        this.enableEventReplication = enableEventReplication;
    }
}
