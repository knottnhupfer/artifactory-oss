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

import java.util.List;
import java.util.stream.Stream;

/**
 * A business service interface to interact with the tree events.
 *
 * @author Yossi Shaul
 */
public interface EventsService {
    /**
     * @return True if node events recording is enabled
     */
    boolean isEnabled();

    /**
     * @return Count of all events in the log
     */
    long getEventsCount();

    /**
     * List of ordered events to add
     *
     * @param events List of ordered events to create
     */
    void appendEvents(List<EventInfo> events);

    /**
     * @param timestamp The timestamp to get events after
     * @param repoKey   The repository key or events to get
     * @return Ordered list of events happened since the input timestamp (not inclusive) on the repo key
     */
    List<EventInfo> getEventsAfter(long timestamp, String repoKey);

    /**
     * @param timestamp The timestamp to get events after
     * @return Ordered list of events happened since the input timestamp (not inclusive)
     */
    List<EventInfo> getEventsAfter(long timestamp);

    List<EventInfo> getEventsByTimestamp(long timestamp);

    /**
     * @param timestamp The timestamp to get events since
     * @param repoKey   The repository key or events to get
     * @return Ordered list of events happened since the input timestamp on the repo key. Including events happened at
     * the same time as the input timestamp
     */
    List<EventInfo> getEventsAfterInclusive(long timestamp, String repoKey);

    List<EventInfo> getEventsAfterInclusive(long timestamp);

    Stream<EventInfo> getEventsStreamAfterInclusive(long timestamp, String repoKey);

    /**
     * @return The timestamp in millis of the first recorded event
     */
    long getFirstEventTimestamp();

    /**
     * @return The timestamp in millis of the last recorded event
     */
    long getLastEventLogTimestamp();

    /**
     * fetch all events created after timestamp that correspond to this repoKey
     * @param timestamp - events since this time
     * @param repoKey   - events under this repo
     * @return a stream of events objects
     */
    Stream<EventInfo> getEventsSince(long timestamp, String repoKey);

    /**
     * Get all the events newer from the from and older than to
     *
     * @param from Beginning of the interval non-inclusive timestamp in millis
     * @param to End of the interval non-inclusive timestamp in millis
     * @return Events newer than the given from and older then the to ordered by timestamp
     */
    List<EventInfo> getEventsInterval(long from, long to, int limit, String repoKey);

    Stream<EventInfo> getEventsSince(long timestamp);

    void deleteRange(long startInclusive, long endExclusive);
}
