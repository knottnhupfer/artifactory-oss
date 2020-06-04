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

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Holds the info about the latest replication that annotates a replication root item
 *
 * @author Noam Y. Tenne
 */
public class ReplicationStatus implements Serializable {

    private ReplicationStatusType status;
    private String lastCompleted;
    private List<ReplicationTarget> targets;
    private Map<String, ReplicationStatus> repositories;

    public ReplicationStatus() {
    }

    public ReplicationStatus(ReplicationStatusType status, @Nullable String lastCompleted) {
        this.status = status;
        this.lastCompleted = lastCompleted;
    }

    /**
     * @param status        general status
     * @param lastCompleted general last completed
     * @param repositories  multi-replication repositories statuses
     */
    public ReplicationStatus(ReplicationStatusType status, @Nullable String lastCompleted,
            Map<String, ReplicationStatus> repositories, List<ReplicationTarget> targets) {
        this.status = status;
        this.lastCompleted = lastCompleted;
        this.targets = targets;
        this.repositories = repositories;
    }

    public String getStatus() {
        return status.getId();
    }

    public List<ReplicationTarget> getTargets() {
        return targets;
    }

    @JsonIgnore
    public String getDisplayName() {
        return status.getDisplayName();
    }

    @JsonIgnore
    public ReplicationStatusType getType() {
        return status;
    }

    public void setStatus(String status) {
        this.status = ReplicationStatusType.findTypeById(status);
    }

    @Nullable
    public String getLastCompleted() {
        return lastCompleted;
    }

    public void setLastCompleted(@Nullable String lastCompleted) {
        this.lastCompleted = lastCompleted;
    }

    public Map<String, ReplicationStatus> getRepositories() {
        return repositories;
    }
}
