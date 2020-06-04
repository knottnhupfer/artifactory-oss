package org.artifactory.logging.version.v14;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import static org.artifactory.logging.version.v11.LogbackFilteredXrayTrafficConverter.XRAY_FILTERED_APPENDER_NAME;
import static org.artifactory.logging.version.v11.LogbackFilteredXrayTrafficConverter.XRAY_FILTERED_LOGGER_NAME;
import static org.testng.Assert.assertFalse;

/**
 * @author Inbar Tal
 */
public class LogbackXrayTrafficConverterTest extends XmlConverterTest {

    private static final String XRAY_TRAFFIC_APPENDER_NAME = "XRAY_TRAFFIC";
    private static final String XRAY_TRAFFIC_LOGGER_NAME = "org.artifactory.traffic.XrayTrafficLogger";

    @Test
    public void addAppenderAndLoggerWithoutOldXrayAppenderAndLogger() throws Exception {
        Document doc = convertXml("/org/artifactory/logging/version/v11/before_xray_filtered_traffic_logback.xml",
                new LogbackXrayTrafficConverter());
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        assertRemoveOldAndAddNewAppender(root, ns, XRAY_FILTERED_APPENDER_NAME, XRAY_TRAFFIC_APPENDER_NAME);
        assertRemoveOldAndAddNewLogger(root, ns, XRAY_FILTERED_LOGGER_NAME, XRAY_TRAFFIC_LOGGER_NAME);
    }

    @Test
    public void addAppenderAndLoggerWithOldXrayAppenderAndLogger() throws Exception {
        Document doc = convertXml("/org/artifactory/logging/version/v14/before_xray_traffic_logback.xml",
                new LogbackXrayTrafficConverter());
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        assertRemoveOldAndAddNewAppender(root, ns, XRAY_FILTERED_APPENDER_NAME, XRAY_TRAFFIC_APPENDER_NAME);
        assertRemoveOldAndAddNewLogger(root, ns, XRAY_FILTERED_LOGGER_NAME, XRAY_TRAFFIC_LOGGER_NAME);
    }

    private void assertRemoveOldAndAddNewAppender(Element root, Namespace ns, String oldAppenderName,
            String newAppenderName) {
        assertAppenderNotExist(root, ns, oldAppenderName);
        assertAppenderExists(root, ns, newAppenderName);
    }

    private void assertRemoveOldAndAddNewLogger(Element root, Namespace ns, String oldLoggerName,
            String newloggerName) {
        assertLoggerNotExist(root, ns, oldLoggerName);
        assertLoggerExists(root, ns, newloggerName);
    }

    private void assertAppenderNotExist(Element root, Namespace ns, String appenderName) {
        assertFalse(root.getChildren("appender", ns).stream()
                        .anyMatch(appender -> appender.getAttributeValue("name", ns).equals(appenderName)),
                "Appender '" + appenderName + "' found after conversion");
    }

    private void assertLoggerNotExist(Element root, Namespace ns, String loggerName) {
        assertFalse(root.getChildren("logger", ns).stream()
                        .anyMatch(appender -> appender.getAttributeValue("name", ns).equals(loggerName)),
                "logger '" + loggerName + "' found after conversion");
    }
}
