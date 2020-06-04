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

package org.artifactory.version.converter.v207;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deleting an old service admin token in order to create a new access admin token (new approach)
 *
 * @author Noam Shemesh
 */
public class RemoveAccessAdminCredentialsConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(RemoveAccessAdminCredentialsConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Removing access admin token from the config file");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        String[] elementPath = new String[] { "security", "accessClientSettings" };
        String missingElement = null;
        Element parentElement = rootElement;
        Element element = null;
        for (String elementName : elementPath) {
            element = parentElement.getChild(elementName, namespace);
            if (element == null) {
                missingElement = elementName;
                break;
            }
            parentElement = element;
        }

        if (element == null) {
            log.info("Element not found: '{}'. Skipping removing of access admin token",
                    missingElement);
            return;
        }

        boolean deleted = element.removeChild("adminToken", namespace);

        log.info("Finish removing access admin token from the config file. deleted: {}", deleted ? "yes" : "no");
    }
}