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

package org.artifactory.logging.version.v5;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes the support log appender, and the related logger's ref to make the support log output into the
 * standard appender.
 *
 * @author Dan Feldman
 */
public class LogbackRemoveSupportLogConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(LogbackRemoveSupportLogConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting logback conversion --> removing Support log appender.");
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        root.getChildren("appender", ns).stream()
                .filter(appender -> "SUPPORT".equals(appender.getAttributeValue("name", ns)))
                .findFirst()
                .ifPresent(root::removeContent);

        root.getChildren("logger", ns).stream()
                .filter(logger -> "org.artifactory.support".equals(logger.getAttributeValue("name", ns)))
                .findFirst()
                .ifPresent(logger -> logger.removeChild("appender-ref", ns));

        log.info("Remove support appender logback conversion completed.");
    }
}