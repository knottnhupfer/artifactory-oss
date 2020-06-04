package org.artifactory.logging.version.v13;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

/**
 * @author Yuval Reches
 */
@Test
public class ConanV2MigrationLogsConverterTest extends XmlConverterTest {

    private static final String CONAN_V2_LOGGER_NAME = "org.artifactory.addon.conan.migration.ConanV2MigrationJob";
    private static final String CONAN_V2_APPENDER_NAME = "CONAN_V2_MIGRATION";

    @Test
    public void addAppenderAndLoggers() throws Exception {
        Document doc = convertXml("/org/artifactory/logging/version/v12/before_binstore_logger_activation_logback.xml", new ConanV2MigrationLogsConverter());
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        assertAppenderExists(root, ns, CONAN_V2_APPENDER_NAME);
        assertLoggerExists(root, ns, CONAN_V2_LOGGER_NAME);
    }
}
