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

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yinon Avraham.
 */
public class  AccessTokenSettingsRenameToAccessClientSettingsConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(AccessTokenSettingsRenameToAccessClientSettingsConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting to convert AccessTokenSettings to AccessClientSettings");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        log.debug("Looking for the 'accessTokenSettings' element under 'root > security'");
        String[] elementPath = new String[] { "security", "accessTokenSettings" };
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
            log.info("Element not found: '{}'. Skipping conversion of AccessTokenSettings to AccessClientSettings",
                    missingElement);
            return;
        }
        element.setName("accessClientSettings");
        log.info("Finished to convert AccessTokenSettings to AccessClientSettings");
    }

}
