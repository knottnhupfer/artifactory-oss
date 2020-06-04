package org.artifactory.version.converter.v218;

import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.*;

/**
 * @author Yuval Reches
 */
public class AnonAccessToBuildsConverterTest extends XmlConverterTest {

    private String CONFIG_XML = "/config/test/config.2.1.0.xray_configs.xml";
    private String CONFIG_XML_NO_ANON_ACCESS_TO_BUILDS = "/config/test/config.2.1.1.without_anonAccessToBuilds.xml";
    private String CONFIG_XML_NO_SECURITY = "/config/test/config.2.1.1.without_security.xml";

    @Test
    public void convertWithSecurity() throws Exception {
        assertConfig(CONFIG_XML);
    }

    @Test
    public void convertWithSecurityNoAnonAccessToBuildsField() throws Exception {
        assertConfig(CONFIG_XML_NO_ANON_ACCESS_TO_BUILDS);
    }

    @Test
    public void convertWithNoSecurity() throws Exception {
        assertConfig(CONFIG_XML_NO_SECURITY);
    }

    private void assertConfig(String xmlPath) throws Exception {
        Document document = convertXml(xmlPath, new AnonAccessToBuildsConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        validateConfig(rootElement.getChild("security", namespace), namespace);
    }

    private void validateConfig(Element security, Namespace namespace)
            throws IOException {
        assertNotNull(security, "Security element should have been created");
        assertNull(security.getChild("anonAccessToBuildInfosDisabled", namespace));
        assertEquals(security.getChildText("buildGlobalBasicReadAllowed", security.getNamespace()), "true");
        assertEquals(security.getChildText("buildGlobalBasicReadForAnonymous", security.getNamespace()), "true");
        validateMarkerFileContent();
    }

    private void validateMarkerFileContent() throws IOException {
        File markerFile = ArtifactoryHome.get().getCreateDefaultBuildPermissionMarkerFile();
        assertTrue(markerFile.exists(), "Marker file should always exist");
        String fileContent = FileUtils.readFileToString(markerFile);
        assertEquals(fileContent, "false");
        // Cleanup for next test
        assertTrue(markerFile.delete());
    }

}
