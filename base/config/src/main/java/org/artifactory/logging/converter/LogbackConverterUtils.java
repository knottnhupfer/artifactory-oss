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

package org.artifactory.logging.converter;

import org.apache.commons.lang.StringUtils;
import org.artifactory.util.StringInputStream;
import org.artifactory.util.XmlUtils;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Shay Bagants
 * @author Dan Feldman
 */
public class LogbackConverterUtils {
    private static final Logger log = LoggerFactory.getLogger(LogbackConverterUtils.class);

    private LogbackConverterUtils() {
        // private constructor for utility class
    }

    /**
     * Appends {@param appenderContent} with {@param appenderName} to the end of the appenders list in the xml
     */
    public static void addAppender(Element root, Namespace ns, String appenderName, String appenderContent) throws IOException, JDOMException {
        List<Element> appenders = root.getChildren("appender", ns);
        for (Element element : appenders) {
            if (element.getAttributeValue("name", ns).equals(appenderName)) {
                log.info("{} log appender already exists in logback.xml, skipping conversion", appenderName);
                return;
            }
        }
        Element appender = getElement(appenderContent);
        root.addContent(root.indexOf(appenders.get(appenders.size() - 1)) + 1, new Text("\n    "));
        root.addContent(root.indexOf(appenders.get(appenders.size() - 1)) + 2, appender);
    }

    /**
     * Appends {@param loggerContent} with {@param loggerName} to the end of the loggers list in the xml
     */
    public static void addLogger(Element root, Namespace ns, String loggerName, String loggerContent) throws IOException, JDOMException {
        List<Element> loggers = root.getChildren("logger", ns);
        for (Element logger : loggers) {
            if (StringUtils.equals(logger.getAttributeValue("name", ns), loggerName)) {
                log.info("Logger: '{}' config already exists in logback.xml, skipping conversion", loggerName);
                return;
            }
        }
        Element logger = getElement(loggerContent);
        root.addContent(root.indexOf(loggers.get(loggers.size() - 1)) + 1, new Text("\n    "));
        root.addContent(root.indexOf(loggers.get(loggers.size() - 1)) + 2, logger);
    }

    /**
     * Return Element object from String
     */
    public static Element getElement(String input) throws IOException, JDOMException {
        SAXBuilder builder = XmlUtils.createSaxBuilder();
        try (InputStream stream = new StringInputStream(input)) {
            Document doc = builder.build(stream);
            Element root = doc.getRootElement();
            return root.detach();
        }
    }
}
