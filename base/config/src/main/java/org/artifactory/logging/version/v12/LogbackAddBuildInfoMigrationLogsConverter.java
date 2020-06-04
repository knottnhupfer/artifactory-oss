package org.artifactory.logging.version.v12;

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
 * @author Inbar Tal
 */
public class LogbackAddBuildInfoMigrationLogsConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(LogbackAddBuildInfoMigrationLogsConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting logback conversion --> Adding build info migration logs.");

        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();
        addAppenderIfNeeded(root, ns, BUILD_INFO_APPENDER_NAME, BUILD_INFO_APPENDER_CONTENT);
        addLoggerIfNeeded(root, ns, BUILD_INFO_LOGGER_NAME, BUILD_INFO_LOGGER_CONTENT);
        log.info("Build info migration logs conversion completed.");
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

    private static final String BUILD_INFO_LOGGER_NAME = "org.artifactory.storage.jobs.migration.buildinfo.BuildInfoMigrationJob";
    private static final String BUILD_INFO_APPENDER_NAME = "BUILD_INFO_MIGRATION";

    private static final String BUILD_INFO_APPENDER_CONTENT =
            "    <appender name=\"BUILD_INFO_MIGRATION\" class=\"ch.qos.logback.core.rolling.RollingFileAppender\">\n"+
                    "        <File>${artifactory.home}/logs/build_info_migration.log</File>\n"+
                    "        <encoder class=\"ch.qos.logback.core.encoder.LayoutWrappingEncoder\">\n"+
                    "            <layout class=\"org.jfrog.common.logging.logback.layout.BackTracePatternLayout\">\n"+
                    "                <pattern>%date ${artifactory.contextId}[%thread] [%-5p] \\(%-20c{3}:%L\\) - %m%n</pattern>\n"+
                    "            </layout>\n"+
                    "        </encoder>\n"+
                    "        <rollingPolicy class=\"ch.qos.logback.core.rolling.FixedWindowRollingPolicy\">\n"+
                    "            <FileNamePattern>${artifactory.home}/logs/build_info_migration.%i.log.zip</FileNamePattern>\n"+
                    "            <maxIndex>13</maxIndex>\n"+
                    "        </rollingPolicy>\n"+
                    "        <triggeringPolicy class=\"ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy\">\n"+
                    "            <MaxFileSize>10MB</MaxFileSize>\n"+
                    "        </triggeringPolicy>\n"+
                    "    </appender>";

    private static final String BUILD_INFO_LOGGER_CONTENT =
            "    <logger name=\"org.artifactory.storage.jobs.migration.buildinfo.BuildInfoMigrationJob\" additivity=\"false\">\n" +
                    "        <level value=\"info\"/>\n" +
                    "        <appender-ref ref=\"BUILD_INFO_MIGRATION\"/>\n" +
                    "    </logger>";
}
