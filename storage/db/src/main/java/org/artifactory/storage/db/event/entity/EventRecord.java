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

package org.artifactory.storage.db.event.entity;

import lombok.Value;
import org.artifactory.storage.event.EventType;

/**
 * Represents a record in the events table.
 *
 * @author Yossi Shaul
 */
@Value
public class EventRecord implements Comparable<EventRecord> {
    private long eventId;
    private final long timestamp;
    private final EventType type;
    /**
     * Path of the item that received the event. Paths end with '/' represents a directory
     */
    private final String path;

    /**
     * Compare two events by timestamp and event id. Event id is used only if the timestamp is equal
     */
    @Override
    public int compareTo(EventRecord o) {
        if (this.getTimestamp() != o.getTimestamp()) {
            return Long.compare(this.getTimestamp(), o.getTimestamp());
        }
        return Long.compare(this.getEventId(), o.getEventId());
    }
}
