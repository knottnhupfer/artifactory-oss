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

package org.artifactory.version.converter.v182;

import org.artifactory.version.converter.XmlConverter;
import org.artifactory.version.converter.v160.AddonsDefaultLayoutConverterHelper;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yinon Avraham
 */
public class ConanDefaultLayoutConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(ConanDefaultLayoutConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting the conan default repository layout conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        log.debug("Adding conan default repository layout");
        Element repoLayoutsElement = rootElement.getChild("repoLayouts", namespace);
        addConanDefaultLayout(repoLayoutsElement, namespace);

        log.info("Conan default repository layout conversion finished successfully");
    }

    private void addConanDefaultLayout(Element repoLayoutsElement, Namespace namespace) {
        repoLayoutsElement.addContent(
                AddonsDefaultLayoutConverterHelper.getRepoLayoutElement(repoLayoutsElement, namespace,
                        "conan-default",
                        "[module]/[baseRev]@[org]/[channel<[^/]+>][remainder<(?:.*)>]",
                        "false", null,
                        ".*",
                        ".*"));
    }
}
