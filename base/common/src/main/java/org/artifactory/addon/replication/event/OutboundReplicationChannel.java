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

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author nadavy
 */
public interface OutboundReplicationChannel extends ReplicationChannel {

    /**
     * Used by the {@link javax.ws.rs.core.StreamingOutput} that's returned to the replicating end.
     * This stream is being read by {@link org.artifactory.addon.replication.core.remote.event.InboundReplicationChannel.IncomingStreamReader}
     */
    void write(OutputStream outputStream);

    String getNextEventAsJson();

    void putEvent(ReplicationEventQueueWorkItem eventQueueItem) throws IOException;

    void destroy();

    String getUuid();

    String getRepoKey();
}
