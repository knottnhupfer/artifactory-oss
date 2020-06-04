package org.artifactory.mime.version.converter.v13;

import org.artifactory.mime.version.converter.MimeTypeConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Omri Ziv
 */
public class CondaMimeTypeConverterTest extends MimeTypeConverterTest {

    @Test
    public void testConvert() throws Exception {
        Document document = convertXml("/org/artifactory/mime/version/mimetypes-v12.xml", new CondaMimeTypeConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        List<Element> mimetypes = rootElement.getChildren("mimetype", namespace);

        Element element = getType(mimetypes, namespace, "application/x-conda");
        assertNotNull(element);
        assertEquals(element.getAttributeValue("extensions"), "conda");
        assertTrue(Boolean.parseBoolean(element.getAttributeValue("archive")));
        assertTrue(Boolean.parseBoolean(element.getAttributeValue("index")));
        assertEquals(element.getAttributeValue("css"), "conda");
    }
}