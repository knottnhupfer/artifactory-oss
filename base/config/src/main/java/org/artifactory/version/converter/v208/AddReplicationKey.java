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

package org.artifactory.version.converter.v208;

import org.artifactory.util.IdUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Noam Shemesh
 */
public class AddReplicationKey implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(AddReplicationKey.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting config conversion: Add replication key to all replications ");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        processReplications(rootElement.getChild("localReplications", namespace));
        processReplications(rootElement.getChild("remoteReplications", namespace));

        log.info("Finish config conversion of replication key append");
    }

    private void processReplications(Element replications) {
        if (replications != null && !replications.getChildren().isEmpty()) {
            Set<String> convertedKeys = new HashSet<>();
            List<Element> children = replications.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                processReplication(convertedKeys, children.get(i));
            }
        }
    }

    private void processReplication(Set<String> convertedKeys, Element replication) {
        Element replicationKey = new Element("replicationKey", replication.getNamespace());
        String repoKey = replication.getChildText("repoKey", replication.getNamespace());
        String url = replication.getChildText("url", replication.getNamespace());
        String convertedKey = IdUtils.createReplicationKey(repoKey, url);
        if (convertedKeys.contains(convertedKey)) {
            log.warn("Duplicate replication key {} found. Duplicated element will be deleted.", convertedKey);
            replication.detach();
            return;
        }

        convertedKeys.add(convertedKey);
        replicationKey.setText(convertedKey);
        int lastLocation = findLastLocation(replication, "enableEventReplication");
        replication.addContent(lastLocation + 1, new Text("\n            "));
        replication.addContent(lastLocation + 2, replicationKey);
    }
}
