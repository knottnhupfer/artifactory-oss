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

package org.artifactory.addon.ha.message;

import org.artifactory.fs.WatcherInfo;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author mamo
 */
public interface WatchesHaMessage extends HaMessage {

    @JsonTypeName("watchesAddEvent")
    public class AddWatch extends HaBaseMessage implements WatchesHaMessage {
        private long nodeId;
        private String username;
        private long watchingSinceTime;

        public AddWatch() {
            super("");
        }

        public AddWatch(String publishingMemberId, long nodeId, WatcherInfo watchInfo) {
            super(publishingMemberId);
            this.nodeId = nodeId;
            this.username = watchInfo.getUsername();
            this.watchingSinceTime = watchInfo.getWatchingSinceTime();
        }

        public long getNodeId() {
            return nodeId;
        }

        public void setNodeId(long nodeId) {
            this.nodeId = nodeId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public long getWatchingSinceTime() {
            return watchingSinceTime;
        }

        public void setWatchingSinceTime(long watchingSinceTime) {
            this.watchingSinceTime = watchingSinceTime;
        }

        @Override
        public int hashCode() {
            int result = (int) (nodeId ^ (nodeId >>> 32));
            result = 31 * result + (username != null ? username.hashCode() : 0);
            result = 31 * result + (int) (watchingSinceTime ^ (watchingSinceTime >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            AddWatch addWatch = (AddWatch) o;

            if (nodeId != addWatch.nodeId) {
                return false;
            }
            if (watchingSinceTime != addWatch.watchingSinceTime) {
                return false;
            }
            return username != null ? username.equals(addWatch.username) : addWatch.username == null;
        }
    }

    @JsonTypeName("watchesDeleteAllEvent")
    public class DeleteAllWatches extends HaBaseMessage implements WatchesHaMessage {
        private String repoKey;
        private String path;
        private boolean folder;

        public DeleteAllWatches() {
            super("");
        }

        public DeleteAllWatches(String repoKey, String path, boolean folder, String publishingMemberId) {
            super(publishingMemberId);
            this.repoKey = repoKey;
            this.path = path;
            this.folder = folder;
        }

        public String getRepoKey() {
            return repoKey;
        }

        public void setRepoKey(String repoKey) {
            this.repoKey = repoKey;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isFolder() {
            return folder;
        }

        public void setFolder(boolean folder) {
            this.folder = folder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DeleteAllWatches that = (DeleteAllWatches) o;

            if (folder != that.folder) {
                return false;
            }
            if (repoKey != null ? !repoKey.equals(that.repoKey) : that.repoKey != null) {
                return false;
            }
            return path != null ? path.equals(that.path) : that.path == null;
        }

        @Override
        public int hashCode() {
            int result = repoKey != null ? repoKey.hashCode() : 0;
            result = 31 * result + (path != null ? path.hashCode() : 0);
            result = 31 * result + (folder ? 1 : 0);
            return result;
        }
    }

    @JsonTypeName("watchesDeleteUserWatchesEvent")
    public class DeleteUserWatches extends HaBaseMessage implements WatchesHaMessage {
        private String repoKey;
        private String path;
        private String username;
        private boolean folder;

        public DeleteUserWatches() {
            super("");
        }

        public DeleteUserWatches(String repoKey, String path, boolean folder, String username,
                String publishingMemberId) {
            super(publishingMemberId);
            this.repoKey = repoKey;
            this.path = path;
            this.folder = folder;
            this.username = username;
        }

        public String getRepoKey() {
            return repoKey;
        }

        public void setRepoKey(String repoKey) {
            this.repoKey = repoKey;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isFolder() {
            return folder;
        }

        public void setFolder(boolean folder) {
            this.folder = folder;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DeleteUserWatches that = (DeleteUserWatches) o;

            if (folder != that.folder) {
                return false;
            }
            if (repoKey != null ? !repoKey.equals(that.repoKey) : that.repoKey != null) {
                return false;
            }
            if (path != null ? !path.equals(that.path) : that.path != null) {
                return false;
            }
            return username != null ? username.equals(that.username) : that.username == null;
        }

        @Override
        public int hashCode() {
            int result = repoKey != null ? repoKey.hashCode() : 0;
            result = 31 * result + (path != null ? path.hashCode() : 0);
            result = 31 * result + (username != null ? username.hashCode() : 0);
            result = 31 * result + (folder ? 1 : 0);
            return result;
        }
    }

    @JsonTypeName("watchesDeleteAllUserWatchesEvent")
    public class DeleteAllUserWatches extends HaBaseMessage implements WatchesHaMessage {
        private String username;

        public DeleteAllUserWatches() {
            super("");
        }

        public DeleteAllUserWatches(String username, String publishingMemberId) {
            super(publishingMemberId);
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DeleteAllUserWatches that = (DeleteAllUserWatches) o;

            return username != null ? username.equals(that.username) : that.username == null;
        }

        @Override
        public int hashCode() {
            return username != null ? username.hashCode() : 0;
        }
    }
}
