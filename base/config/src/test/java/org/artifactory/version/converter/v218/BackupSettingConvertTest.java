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
 * @author Tamir Hadad
 */
public class BackupSettingConvertTest extends XmlConverterTest {

    private String CONFIG_XML = "/config/test/config.2.1.5.with_malformed_conan_distribution_rule.xml";

    @Test
    public void convert() throws Exception {
        assertConfig(CONFIG_XML);
    }

    private void assertConfig(String xmlPath) throws Exception {
        Document document = convertXml(xmlPath, new BackupSettingConvert());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        validateConfig(rootElement.getChild("backups", namespace), namespace);
    }

    private void validateConfig(Element backups, Namespace namespace) throws Exception {
        assertNotNull(backups, "Missing backups element");
        Element backup = backups.getChild("backup", namespace);
        assertNotNull(backup, "Missing backup element");
        assertNull(backup.getChild("excludeBuilds", backups.getNamespace()));
        validateMarkerFileContent();
    }

    private void validateMarkerFileContent() throws IOException {
        File markerFile = ArtifactoryHome.get().getCreateBackupExcludedBuildNames();
        assertTrue(markerFile.exists(), "Marker file should always exist");
        String fileContent = FileUtils.readFileToString(markerFile);
        fileContent = fileContent.replace("[", "").replace("]", "");
        assertEquals(fileContent, "backup-weekly");
        // Cleanup
        assertTrue(markerFile.delete());
    }
}
