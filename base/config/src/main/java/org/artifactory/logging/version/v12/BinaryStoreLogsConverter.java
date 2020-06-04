package org.artifactory.logging.version.v12;

import org.artifactory.logging.converter.LogbackConverterUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BinaryStoreLogsConverter implements XmlConverter {
    private Logger log = LoggerFactory.getLogger(BinaryStoreLogsConverter.class);
    private static final String BINARY_STORE_LOGGER_NAME = "org.jfrog.storage.binstore.servlet.security.BinaryStoreAccessLogger";
    private static final String BINARY_STORE_APPENDER_NAME = "BINARY_STORE";


    private final String BINARY_STORE_APPENDER_CONTENT =
            "    <appender name=\"BINARY_STORE\" class=\"ch.qos.logback.core.rolling.RollingFileAppender\">\n" +
                    "        <File>${artifactory.home}/logs/binarystore.log</File>\n" +
                    "        <encoder class=\"ch.qos.logback.core.encoder.LayoutWrappingEncoder\">\n" +
                    "            <layout class=\"org.jfrog.common.logging.logback.layout.BackTracePatternLayout\">\n" +
                    "                <pattern>%date ${artifactory.contextId}[%thread] [%-5p] \\(%-20c{3}:%L\\) - %message%n</pattern>\n" +
                    "            </layout>\n" +
                    "        </encoder>\n" +
                    "        <rollingPolicy class=\"ch.qos.logback.core.rolling.FixedWindowRollingPolicy\">\n" +
                    "            <FileNamePattern>${artifactory.home}/logs/binarystore.%i.log.zip</FileNamePattern>\n" +
                    "            <maxIndex>13</maxIndex>\n" +
                    "        </rollingPolicy>\n" +
                    "        <triggeringPolicy class=\"ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy\">\n" +
                    "            <MaxFileSize>10MB</MaxFileSize>\n" +
                    "        </triggeringPolicy>\n" +
                    "    </appender>";


    private final String BINARY_STORE_LOGGER_CONTENT =
            "    <logger name=\"org.jfrog.storage.binstore.servlet.security.BinaryStoreAccessLogger\" additivity=\"false\">\n" +
                    "        <level value=\"info\"/>\n" +
                    "        <appender-ref ref=\"BINARY_STORE\"/>\n" +
                    "    </logger>";


    @Override
    public void convert(Document doc) {
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        try {
            LogbackConverterUtils.addAppender(rootElement, namespace, BINARY_STORE_APPENDER_NAME, BINARY_STORE_APPENDER_CONTENT);
            LogbackConverterUtils.addLogger(rootElement, namespace, BINARY_STORE_LOGGER_NAME, BINARY_STORE_LOGGER_CONTENT);
        } catch (IOException | JDOMException e) {
            log.debug("During editing logback.xml error accord : {}", e);
        }

    }
}
