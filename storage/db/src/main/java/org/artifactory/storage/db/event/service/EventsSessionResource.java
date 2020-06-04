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

import com.google.common.collect.Lists;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.storage.event.EventInfo;
import org.artifactory.storage.event.EventType;
import org.artifactory.storage.event.EventsService;
import org.artifactory.storage.tx.SessionEventHandler;
import org.artifactory.storage.tx.SessionResource;
import org.artifactory.storage.tx.SessionResourceAdapter;
import org.artifactory.util.RepoPathUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The events session resource is a {@link SessionResource} that receives low level storage events and stores them in
 * the events table once the transaction is committed.
 * Like any other session resource, it is attached to a single thread, lives and dies in the context of the current
 * database transaction.
 *
 * @author Yossi Shaul
 */
public class EventsSessionResource extends SessionResourceAdapter implements SessionEventHandler {

    private List<EventInfo> events = Lists.newArrayList();

    @Override
    public void beforeCommit() {
        EventsService eventsService = ContextHelper.get().beanForType(EventsService.class);
        if (!eventsService.isEnabled()) {
            return;
        }
        if (events.isEmpty()) {
            return;
        }
        long timestamp = System.currentTimeMillis();
        List<EventInfo> filteredEvents = events.stream()
                .filter(item -> !RepoPathUtils.isTrash(item.getRepoPath()))
                // change timestamp to just now - make the gap between event and its being committed in db shorter.
                .map(item -> new EventInfo(timestamp, item.getType(), item.getPath()))
                .collect(Collectors.toList());
        eventsService.appendEvents(filteredEvents);
    }

    @Override
    public void itemCreated(VfsItem item) {
        events.add(new EventInfo(System.currentTimeMillis(), EventType.create, item.getRepoPath().toPath()));
    }

    @Override
    public void itemUpdated(VfsItem item) {
        events.add(new EventInfo(System.currentTimeMillis(), EventType.update, item.getRepoPath().toPath()));
    }

    @Override
    public void itemDeleted(VfsItem item) {
        events.add(new EventInfo(System.currentTimeMillis(), EventType.delete, item.getRepoPath().toPath()));
    }

    @Override
    public void propertiesModified(VfsItem item) {
        events.add(new EventInfo(System.currentTimeMillis(), EventType.props, item.getRepoPath().toPath()));
    }

    @Override
    public void afterCompletion(boolean commit) {
    }
}
