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

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * Test for {@link EventBasedRemoteReplicationConverter}
 * - enableEventReplication moved from the local/remote replication descriptors to the base replication descriptor
 * - socketTimeoutMillis has been removed from remote replication descriptor
 *
 * @author nadavy
 */
public class EventBasedRemoteReplicationConverterTest  extends XmlConverterTest {

    private Namespace namespace;

    @Test
    public void convert() throws Exception {
        Document document = convertXml("/config/test/config.2.0.4.with_local_and_remote_replications.xml",
                new EventBasedRemoteReplicationConverter());
        Element rootElement = document.getRootElement();
        namespace = rootElement.getNamespace();
        rootElement.getChild("remoteReplications", namespace).getChildren()
                .forEach(this::testRemoteReplication);
        List<Element> localReplications = rootElement.getChild("localReplications", namespace).getChildren();
        testEnableReplicationLocation(localReplications.get(0), "true");
    }

    private void testRemoteReplication(Element remoteReplication) {
        testEnableReplicationLocation(remoteReplication, "false");
        int socketTimeoutMillisIndex = findLastLocation(remoteReplication, "socketTimeoutMillis");
        assertTrue(socketTimeoutMillisIndex == -1, "Found socketTimeoutMillis in remote replication descriptor");
    }

    private void testEnableReplicationLocation(Element replication, String expected) {
        int repoKeyIndex = findLastLocation(replication, "repoKey");
        int enableEventReplicationIndex = findLastLocation(replication, "enableEventReplication");
        assertTrue(enableEventReplicationIndex - repoKeyIndex == 2, "enableEventReplication hasn't moved");
        Element enableEventReplication = replication.getChild("enableEventReplication", namespace);
        assertTrue(enableEventReplication.getValue().equals(expected));
    }

    private int findLastLocation(Element parent, String... elements) {
        for (String element : elements) {
            Element child = parent.getChild(element, parent.getNamespace());
            if (child != null) {
                return parent.indexOf(child);
            }
        }
        return -1;
    }
}
