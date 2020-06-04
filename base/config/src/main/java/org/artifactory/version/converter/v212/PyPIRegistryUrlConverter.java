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

package org.artifactory.version.converter.v212;

import org.apache.commons.lang.StringUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds 'pyPIRegistryUrl' under each of the PyPI remote repos config section.
 * The url of the repo becomes the registryUrl as well by default.
 * (New remote PyPI repos will have a different URLs)
 *
 * @author Yuval Reches
 */
public class PyPIRegistryUrlConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(PyPIRegistryUrlConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting PyPI 'pyPIRegistryUrl' conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        convertRepos(rootElement.getChild("remoteRepositories", namespace), namespace);
        log.info("Finished PyPI 'pyPIRegistryUrl' conversion");
    }

    private void convertRepos(Element repos, Namespace namespace) {
        if (repos != null && repos.getChildren() != null && !repos.getChildren().isEmpty()) {
            repos.getChildren().stream()
                    .filter(repoElement -> StringUtils.equals(repoElement.getChildText("type", namespace), "pypi"))
                    .forEach(repoElement -> addField(repoElement, namespace));
        }
    }

    private void addField(Element repoElement, Namespace namespace) {
        String repoKey = repoElement.getChildText("key", namespace);
        String url = repoElement.getChildText("url", namespace);
        Element registryUrl = new Element("pyPIRegistryUrl", namespace).setText(url);

        Element pyPI = repoElement.getChild("pypi", namespace);
        // Case the configuration doesn't exist
        if (pyPI == null) {
            log.debug("Repo '{}' doesn't have 'pypi' configuration. Adding pyPI config", repoKey);
            createNewPyPIConfiguration(repoElement, namespace, registryUrl);
            return;
        }
        // Case that the configuration already exists --> only add registryUrl to the end of pypi element
        log.debug("Repo '{}' has 'pypi' configuration. Adding pyPIRegistryUrl field to it", repoKey);
        addRegistryUrlToPyPIConfiguration(registryUrl, pyPI);
    }

    private void addRegistryUrlToPyPIConfiguration(Element registryUrl, Element pyPI) {
        int lastLocation = findLastLocation(pyPI, "packagesContextPath", "indexContextPath");
        pyPI.addContent(lastLocation + 1, new Text("\n            "));
        pyPI.addContent(lastLocation + 2, registryUrl);
    }

    private void createNewPyPIConfiguration(Element repoElement, Namespace namespace, Element registryUrl) {
        Element pyPI = new Element("pypi", namespace);
        pyPI.addContent(new Text("\n                "));
        pyPI.addContent(registryUrl);
        // Finding the suitable location in the remote repo element for adding the PyPI element
        int lastLocation = findLocationToInsert(repoElement);
        repoElement.addContent(lastLocation + 1, new Text("\n            "));
        repoElement.addContent(lastLocation + 2, pyPI);
    }

    /**
     * Finding the lowest available element in the repo that the registryUrl should be after.
     * We send a list of fields in a reversed order. First element is the last element in the repo config we look for.
     * (As not all the fields are required, some may not appear. Url is the first required field.)
     */
    private int findLocationToInsert(Element repo) {
        return findLastLocation(repo,
                "nuget",
                "rejectInvalidJars",
                "remoteRepoLayoutRef",
                "listRemoteFolderItems",
                "synchronizeProperties",
                "shareConfiguration",
                "unusedArtifactsCleanupPeriodHours",
                "remoteRepoChecksumPolicyType",
                "missedRetrievalCachePeriodSecs",
                "assumedOfflinePeriodSecs",
                "retrievalCachePeriodSecs",
                "fetchSourcesEagerly",
                "fetchJarsEagerly",
                "storeArtifactsLocally",
                "hardFail",
                "offline",
                "url");
    }
}

