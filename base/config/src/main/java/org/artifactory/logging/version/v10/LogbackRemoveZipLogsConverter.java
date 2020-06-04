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

package org.artifactory.logging.version.v10;

import org.apache.commons.lang.StringUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Remove zip functionality from rolling appenders.
 *
 * @author Yossi Shaul
 */
public class LogbackRemoveZipLogsConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(LogbackRemoveZipLogsConverter.class);

    @Override
    public void convert(Document doc) {
        log.debug("Starting logback conversion --> Removing zip from rolling appenders.");
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        List<Element> appenders = root.getChildren("appender", ns);
        appenders.forEach(a -> removeZip(a, ns));
        log.debug("Migration logs conversion completed.");
    }

    private void removeZip(Element a, Namespace ns) {
        Element rollingPolicy = a.getChild("rollingPolicy", ns);
        if (rollingPolicy != null) {
            Element fileNamePattern = rollingPolicy.getChild("FileNamePattern", ns);
            if (fileNamePattern != null) {
                String pattern = fileNamePattern.getText();
                if (pattern.endsWith(".zip")) {
                    fileNamePattern.setText(StringUtils.removeEnd(pattern, ".zip"));
                }
            }
        }
    }
}
