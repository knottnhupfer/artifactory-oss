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

package org.artifactory.storage.db.event.service;

import org.artifactory.storage.event.EventInfo;

import java.util.List;

/**
 * Internal interface for the events service. Contains internal, experimental and test only methods. Use with caution!
 *
 * @author Yossi Shaul
 */
public interface InternalEventsService {
    /**
     * @return Ordered list of all the events. This might be a huge list. DO NOT USE IN PRODUCTION
     */
    List<EventInfo> getAllEvents();

    /**
     * Deletes all events from the event log
     */
    void deleteAll();

}
