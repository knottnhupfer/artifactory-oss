package org.artifactory.logging.version.v13;

import org.artifactory.logging.converter.LogbackConverterUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Yuval Reches
 */
public class ConanV2MigrationLogsConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(ConanV2MigrationLogsConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting logback conversion --> Adding Conan V2 migration logs.");

        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        try {
            LogbackConverterUtils.addAppender(rootElement, namespace, CONAN_V2_APPENDER_NAME, CONAN_V2_APPENDER_CONTENT);
            LogbackConverterUtils.addLogger(rootElement, namespace, CONAN_V2_LOGGER_NAME, CONAN_V2_LOGGER_CONTENT);
        } catch (IOException | JDOMException e) {
            log.debug("Error occluded during converting logback.xml: {}", e);
            return;
        }

        log.info("Conan V2 migration logs conversion completed.");
    }

    private static final String CONAN_V2_LOGGER_NAME = "org.artifactory.addon.conan.migration.ConanV2MigrationJob";
    private static final String CONAN_V2_APPENDER_NAME = "CONAN_V2_MIGRATION";

    private static final String CONAN_V2_APPENDER_CONTENT =
            "    <appender name=\"CONAN_V2_MIGRATION\" class=\"ch.qos.logback.core.rolling.RollingFileAppender\">\n" +
                    "        <File>${artifactory.home}/logs/conan_v2_migration.log</File>\n" +
                    "        <encoder class=\"ch.qos.logback.core.encoder.LayoutWrappingEncoder\">\n" +
                    "            <layout class=\"org.jfrog.common.logging.logback.layout.BackTracePatternLayout\">\n" +
                    "                <pattern>%date ${artifactory.contextId}[%thread] [%-5p] \\(%-20c{3}:%L\\) - %m%n</pattern>\n" +
                    "            </layout>\n" +
                    "        </encoder>\n" +
                    "        <rollingPolicy class=\"ch.qos.logback.core.rolling.FixedWindowRollingPolicy\">\n" +
                    "            <FileNamePattern>${artifactory.home}/logs/conan_v2_migration.%i.log</FileNamePattern>\n" +
                    "            <maxIndex>13</maxIndex>\n" +
                    "        </rollingPolicy>\n" +
                    "        <triggeringPolicy class=\"ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy\">\n" +
                    "            <MaxFileSize>10MB</MaxFileSize>\n" +
                    "        </triggeringPolicy>\n" +
                    "    </appender>";

    private static final String CONAN_V2_LOGGER_CONTENT =
            "    <logger name=\"org.artifactory.addon.conan.migration.ConanV2MigrationJob\" additivity=\"false\">\n" +
                    "        <level value=\"info\"/>\n" +
                    "        <appender-ref ref=\"CONAN_V2_MIGRATION\"/>\n" +
                    "    </logger>";
}
