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

import org.artifactory.descriptor.config.CentralConfigDescriptor;

/**
 * Interface representing the flow that delivers events from event based replication publishers to the subscribers.
 *
 * @author Dan Feldman
 */
public interface ReplicationChannel {

    void handleReplicationEvents(ReplicationEventQueueWorkItem event);

    void destroy();

    void reload(CentralConfigDescriptor newDescriptor, CentralConfigDescriptor oldDescriptor);

    ChannelType getChannelType();

    String getOwner();
}
