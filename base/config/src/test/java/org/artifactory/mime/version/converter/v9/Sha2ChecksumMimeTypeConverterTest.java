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

package org.artifactory.mime.version.converter.v9;

import org.artifactory.mime.version.converter.MimeTypeConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Test for {@link Sha2ChecksumMimeTypeConverter}.
 *
 * @author Dan Feldman
 */
public class Sha2ChecksumMimeTypeConverterTest extends MimeTypeConverterTest {

    @Test
    public void testConvert() throws Exception {
        Document document = convertXml("/org/artifactory/mime/version/mimetypes-v8.xml", new Sha2ChecksumMimeTypeConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        List mimetypes = rootElement.getChildren("mimetype", namespace);

        Element pythonType = getType(mimetypes, namespace, "application/x-checksum");
        assertNotNull(pythonType);
        assertEquals(pythonType.getAttributeValue("extensions"), "sha1, sha256, md5");
        assertTrue(Boolean.valueOf(pythonType.getAttributeValue("viewable")));
        assertEquals(pythonType.getAttributeValue("syntax"), "plain");
    }
}
