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

package org.artifactory.logging.version.v8;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Yinon Avraham.
 */
public class LogbackBackTracePatternLayoutConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(LogbackBackTracePatternLayoutConverter.class);
    public static final String OLD_LAYOUT_CLASS_NAME = "org.artifactory.logging.layout.BackTracePatternLayout";
    public static final String NEW_LAYOUT_CLASS_NAME = "org.jfrog.common.logging.logback.layout.BackTracePatternLayout";

    @Override
    public void convert(Document doc) {
        log.info("Starting logback conversion: changing to jfrog common logging BackTracePatternLayout.");
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        List<Element> appenders = root.getChildren("appender", ns);
        for (Element appender : appenders) {
            List<Element> encoders = appender.getChildren("encoder", ns);
            for (Element encoder : encoders) {
                List<Element> layouts = encoder.getChildren("layout", ns);
                for (Element layout : layouts) {
                    Attribute classAttribute = layout.getAttribute("class", ns);
                    if (classAttribute != null && OLD_LAYOUT_CLASS_NAME.equals(classAttribute.getValue())) {
                        classAttribute.setValue(NEW_LAYOUT_CLASS_NAME);
                    }
                }
            }
        }

        log.info("Logback conversion completed: changed to jfrog common logging BackTracePatternLayout.");
    }
}
