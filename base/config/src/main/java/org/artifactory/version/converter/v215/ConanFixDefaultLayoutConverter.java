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

package org.artifactory.version.converter.v215;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.artifactory.util.RepoLayoutUtils.CONAN_DEFAULT_NAME;

/**
 * @author Inbar Tal
 */
public class ConanFixDefaultLayoutConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(ConanFixDefaultLayoutConverter.class);
    private static final int NAME_INDEX = 1;
    private static final int ARTIFACT_PATH_PATTERN_INDEX = 3;

    @Override
    public void convert(Document doc) {
        log.info("Starting conan fix default repository layout conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element repoLayoutsElement = rootElement.getChild("repoLayouts", namespace);
        List<Element> repoLayouts = repoLayoutsElement.getChildren();

        for (Element layout : repoLayouts) {
            if (layout.getContent() != null && layout.getContent().get(NAME_INDEX) != null
                    && CONAN_DEFAULT_NAME.equals(layout.getContent().get(NAME_INDEX).getValue())) {
                log.debug("Found conan-default layout");
                layout.setContent(ARTIFACT_PATH_PATTERN_INDEX, createFixedArtifactPathPattern(namespace));
                log.debug("Fixed conan-default layout successfully");
            }
        }
        log.info("Conan fix default repository layout conversion finished successfully");
    }

    private Element createFixedArtifactPathPattern(Namespace namespace) {
        Element artifactPathPatternElement = new Element("artifactPathPattern", namespace);
        artifactPathPatternElement.setText("[org]/[module]/[baseRev]/[channel<[^/]+>][remainder<(?:.*)>].[ext]");
        return artifactPathPatternElement;
    }
}




