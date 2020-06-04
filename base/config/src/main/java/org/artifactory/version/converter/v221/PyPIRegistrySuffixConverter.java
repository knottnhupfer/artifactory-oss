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

package org.artifactory.version.converter.v221;

import org.apache.commons.lang.StringUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds 'repositorySuffix' under each of the PyPI remote repos config section.
 *
 * @author Nadav Yogev
 */
public class PyPIRegistrySuffixConverter implements XmlConverter {

    public static final String DEFAULT_PYPI_SUFFIX = "simple";

    private static final Logger log = LoggerFactory.getLogger(PyPIRegistrySuffixConverter.class);

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
        Element repositorySuffix = new Element("repositorySuffix", namespace).setText(DEFAULT_PYPI_SUFFIX);

        Element pyPI = repoElement.getChild("pypi", namespace);

        log.debug("Repo '{}' has 'pypi' configuration. Adding repositorySuffix field to it", repoKey);
        addRegistryUrlToPyPIConfiguration(repositorySuffix, pyPI);
    }

    private void addRegistryUrlToPyPIConfiguration(Element repositorySuffix, Element pyPI) {
        if (pyPI == null) {
            return;
        }
        int lastLocation = findLastLocation(pyPI, "pyPIRegistryUrl");
        pyPI.addContent(lastLocation + 1, new Text("\n            "));
        pyPI.addContent(lastLocation + 2, repositorySuffix);
    }
}

