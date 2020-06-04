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

package org.artifactory.addon.replication.event;

import org.artifactory.api.repo.WorkItem;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author nadavy
 */
public class ReplicationEventQueueWorkItem extends WorkItem {

    private List<ReplicationEventQueueItem> events;
    private String repoKey;

    public ReplicationEventQueueWorkItem() {
    }

    public ReplicationEventQueueWorkItem(@Nonnull String repoKey, @Nonnull List<ReplicationEventQueueItem> events) {
        this.repoKey = repoKey;
        this.events = events;
    }

    public ReplicationEventQueueWorkItem(String repoKey) {
        this.repoKey = repoKey;
    }

    @JsonIgnore
    @Nonnull
    @Override
    public String getUniqueKey() {
        return getRepoKey();
    }

    public List<ReplicationEventQueueItem> getEvents() {
        return events;
    }

    public String getRepoKey() {
        return repoKey;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return events == null || events.isEmpty();
    }

    public void setEvents(List<ReplicationEventQueueItem> events) {
        this.events = events;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReplicationEventQueueWorkItem)) {
            return false;
        }
        ReplicationEventQueueWorkItem that = (ReplicationEventQueueWorkItem) o;
        return getRepoKey() != null ? getRepoKey().equals(that.getRepoKey()) : that.getRepoKey() == null;
    }

    @Override
    public int hashCode() {
        return getRepoKey() != null ? getRepoKey().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "{events=" + events + ", repoKey='" + repoKey + '}';
    }
}
