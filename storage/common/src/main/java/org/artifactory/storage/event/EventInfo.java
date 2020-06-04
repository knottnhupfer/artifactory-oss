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

package org.artifactory.storage.event;

import lombok.Value;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;

/**
 * Represents a modification event on the artifacts tree.
 *
 * @author Yossi Shaul
 */
@Value
public class EventInfo implements NodeEvent {
    private final long timestamp;
    private final EventType type;
    private final String path;

    /**
     * @return The repo path for this event
     */
    public RepoPath getRepoPath() {
        return RepoPathFactory.create(path);
    }

    /**
     * @return True if this event is on a folder
     */
    public boolean isFolder() {
        return getRepoPath().isFolder();
    }

    @Override
    public String toString() {
        return timestamp + "|" + type + "|" + path;
    }
}
