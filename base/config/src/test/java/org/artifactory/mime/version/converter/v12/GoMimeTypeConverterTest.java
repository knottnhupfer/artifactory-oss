package org.artifactory.mime.version.converter.v12;

import org.artifactory.mime.version.converter.MimeTypeConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Nadav Yogev
 */
public class GoMimeTypeConverterTest extends MimeTypeConverterTest {

    @Test
    public void testConvert() throws Exception {
        Document document = convertXml("/org/artifactory/mime/version/mimetypes-v10.xml", new GoMimeTypeConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        List mimetypes = rootElement.getChildren("mimetype", namespace);

        Element infoType = getType(mimetypes, namespace, "application/json+info");
        assertNotNull(infoType);
        assertEquals(infoType.getAttributeValue("extensions"), "info");
        assertFalse(Boolean.valueOf(infoType.getAttributeValue("archive")));
        assertFalse(Boolean.valueOf(infoType.getAttributeValue("index")));
        assertTrue(Boolean.valueOf(infoType.getAttributeValue("viewable")));

        Element modType = getType(mimetypes, namespace, "text/plain+mod");
        assertNotNull(modType);
        assertEquals(modType.getAttributeValue("extensions"), "mod");
        assertFalse(Boolean.valueOf(modType.getAttributeValue("archive")));
        assertFalse(Boolean.valueOf(modType.getAttributeValue("index")));
        assertTrue(Boolean.valueOf(modType.getAttributeValue("viewable")));
    }
}