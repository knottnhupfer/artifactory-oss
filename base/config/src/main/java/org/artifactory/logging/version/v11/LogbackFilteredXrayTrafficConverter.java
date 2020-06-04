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

package org.artifactory.logging.version.v11;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.artifactory.logging.converter.LogbackConverterUtils.addAppender;
import static org.artifactory.logging.converter.LogbackConverterUtils.addLogger;

/**
 * Adds Xray filtered traffic logs
 *
 * @author Tamir Hadad
 */
public class LogbackFilteredXrayTrafficConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(LogbackFilteredXrayTrafficConverter.class);

    public static final String XRAY_FILTERED_APPENDER_NAME = "XRAY_FILTERED_TRAFFIC";
    public static final String XRAY_FILTERED_LOGGER_NAME = "org.artifactory.traffic.XrayFilteredTrafficLogger";

    @Override
    public void convert(Document doc) {
        log.debug("Starting logback conversion --> Adding Xray filtered logs.");
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();
        addAppenderIfNeeded(root, ns, XRAY_FILTERED_APPENDER_NAME, XRAY_FILTERED_APPENDER_CONTENT);
        addLoggerIfNeeded(root, ns, XRAY_FILTERED_LOGGER_NAME, XRAY_FILTERED_LOGGER_CONTENT);
        log.debug("Migration logs conversion completed.");
    }

    private void addAppenderIfNeeded(Element root, Namespace ns, String appenderName, String appenderContent) {
        try {
            addAppender(root, ns, appenderName, appenderContent);
        } catch (IOException | JDOMException e) {
            logError(e, appenderName);
        }
    }

    private void addLoggerIfNeeded(Element root, Namespace ns, String loggerName, String loggerContent) {
        try {
            addLogger(root, ns, loggerName, loggerContent);
        } catch (IOException | JDOMException e) {
            logError(e, loggerName);
        }
    }

    private void logError(Exception e, String elementName) {
        String err = "Error adding the '" + elementName + "' element to logback.xml: ";
        log.error("{} {}" , err , e.getMessage());
        log.debug(err, e);
    }

    private final String XRAY_FILTERED_APPENDER_CONTENT =
            "    <appender name=\"" + XRAY_FILTERED_APPENDER_NAME + "\"" +
                    " class=\"ch.qos.logback.core.rolling.RollingFileAppender\">\n" +
                    "        <File>${artifactory.home}/logs/xray_filtered_traffic</File>\n" +
                    "        <encoder>\n" +
                    "            <pattern>%message%n</pattern>\n" +
                    "        </encoder>\n" +
                    "        <rollingPolicy class=\"org.artifactory.traffic.policy.TrafficTimeBasedRollingPolicy\">\n" +
                    "            <FileNamePattern>${artifactory.home}/logs/xray_filtered_traffic.%d{yyyyMMdd}</FileNamePattern>\n" +
                    "        </rollingPolicy>\n" +
                    "    </appender>";

    private final String XRAY_FILTERED_LOGGER_CONTENT =
            "    <logger name=\"" + XRAY_FILTERED_LOGGER_NAME + "\" additivity=\"false\">\n" +
                    "        <level value=\"info\"/>\n" +
                    "        <appender-ref ref=\"" + XRAY_FILTERED_APPENDER_NAME + "\"/>\n" +
                    "    </logger>";
}
