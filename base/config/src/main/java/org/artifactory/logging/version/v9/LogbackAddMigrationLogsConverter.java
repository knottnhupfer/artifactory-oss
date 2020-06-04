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

package org.artifactory.logging.version.v9;

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
 * @author Dan Feldman
 */
public class LogbackAddMigrationLogsConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(LogbackAddMigrationLogsConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting logback conversion --> Adding migration logs.");

        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();
        addAppenderIfNeeded(root, ns, SHA2_APPENDER_NAME, SHA2_APPENDER_CONTENT);
        addAppenderIfNeeded(root, ns, PATH_CHECKSUM_APPENDER_NAME, PATH_CHECKSUM_APPENDER_CONTENT);
        addLoggerIfNeeded(root, ns, SHA2_LOGGER_NAME, SHA2_LOGGER_CONTENT);
        addLoggerIfNeeded(root, ns, PATH_CHECKSUM_LOGGER_NAME, PATH_CHECKSUM_LOGGER_CONTENT);
        log.info("Migration logs conversion completed.");
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
        log.error(err + e.getMessage());
        log.debug(err, e);
    }

    private static final String SHA2_LOGGER_NAME = "org.artifactory.storage.jobs.migration.sha256migration.Sha256MigrationJob";
    private static final String SHA2_APPENDER_NAME = "SHA256_MIGRATION";

    private static final String PATH_CHECKSUM_LOGGER_NAME = "org.artifactory.storage.jobs.migration.pathchecksum.RepoPathChecksumMigrationJob";
    private static final String PATH_CHECKSUM_APPENDER_NAME = "PATH_CHECKSUM_MIGRATION";

    private final String SHA2_APPENDER_CONTENT =
            "    <appender name=\"SHA256_MIGRATION\" class=\"ch.qos.logback.core.rolling.RollingFileAppender\">\n"+
            "        <File>${artifactory.home}/logs/sha256_migration.log</File>\n"+
            "        <encoder class=\"ch.qos.logback.core.encoder.LayoutWrappingEncoder\">\n"+
            "            <layout class=\"org.jfrog.common.logging.logback.layout.BackTracePatternLayout\">\n"+
            "                <pattern>%date ${artifactory.contextId}[%thread] [%-5p] \\(%-20c{3}:%L\\) - %m%n</pattern>\n"+
            "            </layout>\n"+
            "        </encoder>\n"+
            "        <rollingPolicy class=\"ch.qos.logback.core.rolling.FixedWindowRollingPolicy\">\n"+
            "            <FileNamePattern>${artifactory.home}/logs/sha256_migration.%i.log.zip</FileNamePattern>\n"+
            "            <maxIndex>13</maxIndex>\n"+
            "        </rollingPolicy>\n"+
            "        <triggeringPolicy class=\"ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy\">\n"+
            "            <MaxFileSize>10MB</MaxFileSize>\n"+
            "        </triggeringPolicy>\n"+
            "    </appender>";

    private final String SHA2_LOGGER_CONTENT =
            "    <logger name=\"org.artifactory.storage.jobs.migration.sha256.Sha256MigrationJob\" additivity=\"false\">\n" +
            "        <level value=\"info\"/>\n" +
            "        <appender-ref ref=\"SHA256_MIGRATION\"/>\n" +
            "    </logger>";

    private final String PATH_CHECKSUM_APPENDER_CONTENT =
            "    <appender name=\"PATH_CHECKSUM_MIGRATION\" class=\"ch.qos.logback.core.rolling.RollingFileAppender\">\n" +
            "        <File>${artifactory.home}/logs/path_checksum_migration.log</File>\n" +
            "        <encoder class=\"ch.qos.logback.core.encoder.LayoutWrappingEncoder\">\n" +
            "            <layout class=\"org.jfrog.common.logging.logback.layout.BackTracePatternLayout\">\n" +
            "                <pattern>%date ${artifactory.contextId}[%thread] [%-5p] \\(%-20c{3}:%L\\) - %m%n</pattern>\n" +
            "            </layout>\n" +
            "        </encoder>\n" +
            "        <rollingPolicy class=\"ch.qos.logback.core.rolling.FixedWindowRollingPolicy\">\n" +
            "            <FileNamePattern>${artifactory.home}/logs/path_checksum_migration.%i.log.zip</FileNamePattern>\n" +
            "            <maxIndex>13</maxIndex>\n" +
            "        </rollingPolicy>\n" +
            "        <triggeringPolicy class=\"ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy\">\n" +
            "            <MaxFileSize>10MB</MaxFileSize>\n" +
            "        </triggeringPolicy>\n" +
            "    </appender>";

    private final String PATH_CHECKSUM_LOGGER_CONTENT =
            "    <logger name=\"org.artifactory.storage.jobs.migration.pathchecksum.RepoPathChecksumMigrationJob\" additivity=\"false\">\n"+
            "        <level value=\"info\"/>\n"+
            "        <appender-ref ref=\"PATH_CHECKSUM_MIGRATION\"/>\n"+
            "    </logger>";
}
