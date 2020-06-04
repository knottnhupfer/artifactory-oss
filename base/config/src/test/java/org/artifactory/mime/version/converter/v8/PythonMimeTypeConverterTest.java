/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.mime.version.converter.v8;

import org.artifactory.mime.version.converter.MimeTypeConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Tests {@link PythonMimeTypeConverter}.
 *
 * @author Yinon Avraham
 */
public class PythonMimeTypeConverterTest extends MimeTypeConverterTest {

    @Test
    public void testConvert() throws Exception {
        Document document = convertXml("/org/artifactory/mime/version/mimetypes-v4.xml",
                new PythonMimeTypeConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        List mimetypes = rootElement.getChildren("mimetype", namespace);
        // check python type
        Element pythonType = getType(mimetypes, namespace, "text/x-python");
        assertNotNull(pythonType);
        assertTrue(Boolean.valueOf(pythonType.getAttributeValue("viewable")));
        assertEquals(pythonType.getAttributeValue("css"), "python");
        assertEquals(pythonType.getAttributeValue("syntax"), "python");
        assertEquals(pythonType.getAttributeValue("extensions"), "py");
    }
}
