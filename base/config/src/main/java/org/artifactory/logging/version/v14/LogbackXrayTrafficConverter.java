package org.artifactory.logging.version.v14;

import org.apache.commons.lang.StringUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static org.artifactory.logging.converter.LogbackConverterUtils.addAppender;
import static org.artifactory.logging.converter.LogbackConverterUtils.addLogger;
import static org.artifactory.logging.version.v11.LogbackFilteredXrayTrafficConverter.XRAY_FILTERED_APPENDER_NAME;
import static org.artifactory.logging.version.v11.LogbackFilteredXrayTrafficConverter.XRAY_FILTERED_LOGGER_NAME;

/**
 * @author Inbar Tal
 */
public class LogbackXrayTrafficConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(LogbackXrayTrafficConverter.class);

    @Override
    public void convert(Document doc) {
        log.debug("Starting logback conversion --> Adding Xray traffic logs.");
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        removeAppenderIfExists(root, ns, XRAY_FILTERED_APPENDER_NAME);
        removeLoggerIfExists(root, ns, XRAY_FILTERED_LOGGER_NAME);

        addAppenderIfNeeded(root, ns, XRAY_TRAFFIC_APPENDER_NAME, XRAY_APPENDER_CONTENT);
        addLoggerIfNeeded(root, ns, XRAY_TRAFFIC_LOGGER_NAME, XRAY_LOGGER_CONTENT);
    }

    private void removeAppenderIfExists(Element root, Namespace ns, String appenderName) {
        List<Element> appenders = root.getChildren("appender", ns);
        for (Element element : appenders) {
            if (element.getAttributeValue("name", ns).equals(appenderName)) {
                log.info("removing old appender: {}", appenderName);
                root.removeContent(element);
                return;
            }
        }
    }

    private void removeLoggerIfExists(Element root, Namespace ns, String loggerName) {
        List<Element> loggers = root.getChildren("logger", ns);
        for (Element logger : loggers) {
            if (StringUtils.equals(logger.getAttributeValue("name", ns), loggerName)) {
                log.info("Removing old logger: {}", loggerName);
                root.removeContent(logger);
                return;
            }
        }
    }

    private void addAppenderIfNeeded(Element root, Namespace ns, String appenderName, String appenderContent) {
        try {
            addAppender(root, ns, appenderName, appenderContent);
        } catch (IOException | JDOMException e) {
            logError(e, appenderName);
        }
    }

    private void logError(Exception e, String elementName) {
        String err = "Error adding the '" + elementName + "' element to logback.xml: ";
        log.error("{} {}" , err , e.getMessage());
        log.debug(err, e);
    }

    private void addLoggerIfNeeded(Element root, Namespace ns, String loggerName, String loggerContent) {
        try {
            addLogger(root, ns, loggerName, loggerContent);
        } catch (IOException | JDOMException e) {
            logError(e, loggerName);
        }
    }

    private static final String XRAY_TRAFFIC_APPENDER_NAME = "XRAY_TRAFFIC";
    private static final String XRAY_TRAFFIC_LOGGER_NAME = "org.artifactory.traffic.XrayTrafficLogger";

    private static final String XRAY_APPENDER_CONTENT =
            "    <appender name=\"" + XRAY_TRAFFIC_APPENDER_NAME + "\"" +
                    " class=\"ch.qos.logback.core.rolling.RollingFileAppender\">\n" +
                    "        <File>${artifactory.home}/logs/xray_traffic</File>\n" +
                    "        <encoder>\n" +
                    "            <pattern>%message%n</pattern>\n" +
                    "        </encoder>\n" +
                    "        <rollingPolicy class=\"org.artifactory.traffic.policy.TrafficTimeBasedRollingPolicy\">\n" +
                    "            <FileNamePattern>${artifactory.home}/logs/xray_traffic.%d{yyyyMMdd}</FileNamePattern>\n" +
                    "        </rollingPolicy>\n" +
                    "    </appender>";

    private static final String XRAY_LOGGER_CONTENT =
            "    <logger name=\"" + XRAY_TRAFFIC_LOGGER_NAME + "\" additivity=\"false\">\n" +
                    "        <level value=\"info\"/>\n" +
                    "        <appender-ref ref=\"" + XRAY_TRAFFIC_APPENDER_NAME + "\"/>\n" +
                    "    </logger>";
}
