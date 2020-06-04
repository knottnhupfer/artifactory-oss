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

/**
 * @author nadavy
 */
public class RemoteReplicationEventQueueItem implements ReplicationEventQueueItem {

    private String repoKey;
    private String path;
    private ReplicationEventType eventType;

    public static RemoteReplicationEventQueueItem EMPTY = new RemoteReplicationEventQueueItem("", "", ReplicationEventType.EMPTY);

    public RemoteReplicationEventQueueItem() {
    }

    public RemoteReplicationEventQueueItem(String repoKey, String path, ReplicationEventType eventType) {
        this.repoKey = repoKey;
        this.path = path;
        this.eventType = eventType;
    }

    public RemoteReplicationEventQueueItem(ReplicationEventQueueItem event) {
        this.repoKey = event.getRepoKey();
        this.path = event.getPath();
        this.eventType = event.getEventType();
    }

    @Override
    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public ReplicationEventType getEventType() {
        return eventType;
    }

    public void setEventType(ReplicationEventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "RemoteReplicationEventQueueItem{" +
                ", path='" + path + '\'' +
                ", eventType=" + eventType +
                '}';
    }
}
