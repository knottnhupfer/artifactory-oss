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

package org.artifactory.version.converter.v204;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reorder the replications config descriptor to match the new artifactory schema
 * - enable event replication moved from the local/remote replication descriptors to the base replication descriptor
 * - socketTimeoutMillis has been removed from remote replication descriptor as it's not needed
 *
 * @author nadavy
 */
public class EventBasedRemoteReplicationConverter  implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(EventBasedRemoteReplicationConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting conversion: Move 'Enable event replication' flag, off by default, to all replication configs ");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element localReplications = rootElement.getChild("localReplications", namespace);
        if (localReplications != null && !localReplications.getChildren().isEmpty()) {
            localReplications.getChildren().forEach(this::enableEventReplication);
        }
        Element remoteReplications = rootElement.getChild("remoteReplications", namespace);
        if (remoteReplications != null && !remoteReplications.getChildren().isEmpty()) {
            remoteReplications.getChildren().forEach(this::handleRemoteReplication);
        }
    }

    /**
     * Move 'enableEventReplication' and delete 'socketTimeoutMillis' from the remote replication descriptor
     */
    private void handleRemoteReplication(Element replication) {
        enableEventReplication(replication);
        removeContentFromElement(replication, "socketTimeoutMillis");
    }

    /**
     * Move 'enableEventReplication' from the local replication descriptor to the replication base descriptor
     */
    private void enableEventReplication(Element replication) {
        Content removedContent = removeContentFromElement(replication, "enableEventReplication");
        Element enableEventReplication = new Element("enableEventReplication", replication.getNamespace());
        enableEventReplication.setText((removedContent == null) ? "false" : removedContent.getValue());
        int lastLocation = findLastLocation(replication, "repoKey");
        replication.addContent(lastLocation + 1, new Text("\n            "));
        replication.addContent(lastLocation + 2, enableEventReplication);
    }

    /**
     * Removes an element from the replication root element, and the following new line (if exists)
     */
    private Content removeContentFromElement(Element replication, String elementToRemove) {
        int eventReplicationIndex = findLastLocation(replication, elementToRemove);
        if (eventReplicationIndex != -1) {
            Content content = replication.removeContent(eventReplicationIndex);
            try {
                replication.removeContent(eventReplicationIndex + 1);
                return content;
            } catch (IndexOutOfBoundsException ignored) {
                // if the deleted element is the last in the root replication elements, no new line deletion is needed
            }
        }
        return null;
    }
}
