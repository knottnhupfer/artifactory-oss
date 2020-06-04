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
import org.jfrog.client.util.PathUtils;

/**
 * @author Tomer Mayost
 */
public class ReplicationTarget {
    private String url;
    private String repoKey;
    private ReplicationStatusType status;
    private String lastCompleted;

    public ReplicationTarget(String fullRepoPath, ReplicationStatus status) {
        this.repoKey = PathUtils.getLastPathElement(fullRepoPath);
        this.url = fullRepoPath;
        this.status = status.getType();
        this.lastCompleted = status.getLastCompleted();
    }

    public String getUrl() {
        return url;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public String getStatus() {
        return status.getId();
    }

    public String getLastCompleted() {
        return lastCompleted;
    }

    @JsonIgnore
    public ReplicationStatusType getType() {
        return status;
    }

    public void setLastCompleted(String lastCompleted) {
        this.lastCompleted = lastCompleted;
    }

    public void setStatus(String status) {
        this.status = ReplicationStatusType.findTypeById(status);;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }


    @JsonIgnore
    public Boolean replicationStatusByFullRepoPathExists(String fullRepoPath) {
        return this.url.equals(fullRepoPath);
    }
}
