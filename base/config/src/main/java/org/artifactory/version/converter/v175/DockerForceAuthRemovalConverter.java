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

package org.artifactory.version.converter.v175;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shay Bagants
 */
public class DockerForceAuthRemovalConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(DockerForceAuthRemovalConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting docker force authentication removal conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        convertRepos(rootElement.getChild("localRepositories", namespace), namespace);
        convertRepos(rootElement.getChild("remoteRepositories", namespace), namespace);
        convertRepos(rootElement.getChild("virtualRepositories", namespace), namespace);
        convertRepos(rootElement.getChild("distributionRepositories", namespace), namespace);
        log.info("Finished docker force authentication removal conversion");
    }

    private void convertRepos(Element repos, Namespace namespace) {
        if (repos != null && repos.getChildren() != null && !repos.getChildren().isEmpty()) {
            repos.getChildren().forEach(repoElement -> removeForceAuthTag(repoElement, namespace));
        }
    }

    private void removeForceAuthTag(Element repoElement, Namespace namespace) {
        Element forceDockerAuth = repoElement.getChild("forceDockerAuthentication", namespace);
        if (forceDockerAuth != null) {
            String repoKey = repoElement.getChild("key", namespace).getText();
            log.debug("Removing the '{}' tag from '{}'", "forceDockerAuthentication", repoKey);
            repoElement.removeChild("forceDockerAuthentication", namespace);
        }
    }
}
