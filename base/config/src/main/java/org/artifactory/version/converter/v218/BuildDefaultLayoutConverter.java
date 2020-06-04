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

package org.artifactory.version.converter.v218;

import org.artifactory.version.converter.XmlConverter;
import org.artifactory.version.converter.v160.AddonsDefaultLayoutConverterHelper;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dan Feldman
 */
public class BuildDefaultLayoutConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(BuildDefaultLayoutConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting build repository layout conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        log.debug("Adding build repository layout");
        Element repoLayoutsElement = rootElement.getChild("repoLayouts", namespace);
        addBuildDefaultLayout(repoLayoutsElement, namespace);

        log.info("build repository layout conversion done.");
    }

    private void addBuildDefaultLayout(Element repoLayoutsElement, Namespace namespace) {
        repoLayoutsElement.addContent(
                AddonsDefaultLayoutConverterHelper.getRepoLayoutElement(repoLayoutsElement, namespace,
                        "build-default",
                        "[orgPath]/[module](-[fileItegRev]).[ext]",
                        "false", null,
                        ".*",
                        ".*"));
    }
}
