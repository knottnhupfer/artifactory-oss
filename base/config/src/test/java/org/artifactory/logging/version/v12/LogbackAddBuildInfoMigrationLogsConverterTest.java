package org.artifactory.logging.version.v12;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

/**
 * @author Inbar Tal
 */
@Test
public class LogbackAddBuildInfoMigrationLogsConverterTest extends XmlConverterTest {

    private static final String BUILD_INFO_APPENDER_NAME = "BUILD_INFO_MIGRATION";
    private static final String BUILD_INFO_LOGGER_NAME = "org.artifactory.storage.jobs.migration.buildinfo.BuildInfoMigrationJob";

    @Test
    public void addAppendersAndLoggers() throws Exception {
        Document doc = convertXml("/org/artifactory/logging/version/v12/before_binstore_logger_activation_logback.xml", new LogbackAddBuildInfoMigrationLogsConverter());
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        assertAppenderExists(root, ns, BUILD_INFO_APPENDER_NAME);
        assertLoggerExists(root, ns, BUILD_INFO_LOGGER_NAME);
    }
}
