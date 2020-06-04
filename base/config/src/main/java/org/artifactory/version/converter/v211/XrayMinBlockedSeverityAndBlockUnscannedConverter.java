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

package org.artifactory.version.converter.v211;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes 'minimumBlockedSeverity' and 'blockUnscannedArtifacts' under each of the local/remote repos config section.
 *
 * @author Yuval Reches
 */
public class XrayMinBlockedSeverityAndBlockUnscannedConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(XrayMinBlockedSeverityAndBlockUnscannedConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting Xray 'minimumBlockedSeverity' and 'blockUnscannedArtifacts' removal conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        convertRepos(rootElement.getChild("localRepositories", namespace), namespace);
        convertRepos(rootElement.getChild("remoteRepositories", namespace), namespace);
        log.info("Finished Xray 'minimumBlockedSeverity' and 'blockUnscannedArtifacts' removal conversion");
    }

    private void convertRepos(Element repos, Namespace namespace) {
        if (repos != null && repos.getChildren() != null && !repos.getChildren().isEmpty()) {
            repos.getChildren().forEach(repoElement -> removeFields(repoElement, namespace));
        }
    }

    private void removeFields(Element repoElement, Namespace namespace) {
        Element xray = repoElement.getChild("xray", namespace);
        if (xray == null) {
            return;
        }
        String repoKey = repoElement.getChild("key", namespace).getText();
        Element minimumBlockedSeverity = xray.getChild("minimumBlockedSeverity", namespace);
        if (minimumBlockedSeverity != null) {
            log.debug("Removing the '{}' tag from '{}'", "minimumBlockedSeverity", repoKey);
            xray.removeChild("minimumBlockedSeverity", namespace);
        }
        Element blockUnscannedArtifacts = xray.getChild("blockUnscannedArtifacts", namespace);
        if (blockUnscannedArtifacts != null) {
            log.debug("Removing the '{}' tag from '{}'", "blockUnscannedArtifacts", repoKey);
            xray.removeChild("blockUnscannedArtifacts", namespace);
        }
    }

}
