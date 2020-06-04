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

import java.util.List;
import java.util.Set;

/**
 * Interface for the event based replication inbound and outbound managers
 *
 * @author Nadav Yogev
 */
public interface ReplicationChannelManager extends ReplicationChannelListener {

    void handleReplicationEvents(ReplicationEventQueueWorkItem event);

    /**
     * Handles events from another node in the cluster
     */
    void handlePropagatedRemoteReplicationEvents(ReplicationEventQueueWorkItem events);

    void reload(CentralConfigDescriptor newDescriptor, CentralConfigDescriptor oldDescriptor);

    /**
     * Closes all channels - clustered or streamed, of the manager
     */
    void destroy();

    /**
     * Returns a list of repo keys that have a channel of a given channel type
     */
    List<String> getNodeChannels(ChannelType channelType);

    /**
     * Returns true if there are any replication channels for a given repo key
     */
    boolean hasRemoteReplicationForRepo(String repoKey);

    /**
     * Returns a set of all the remote event based replication event subscribers to the given repo, empty set if not exist
     */
    Set<String> getRemoteRepoSubscribers(String repoKey);
}
