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

package org.artifactory.version.converter.v207;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Rotem Kfir
 */
@Test
public class FolderDownloadForAnonymousConfigConverterTest extends XmlConverterTest {

    @Test
    public void convert() throws Exception {
        Document document = convertXml("/config/test/config.2.0.7.with_adminToken.xml",
                new FolderDownloadForAnonymousConfigConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element folderDownloadConfig = rootElement.getChild("folderDownloadConfig", namespace);
        assertTrue(folderDownloadConfig != null, "Expected to find 'folderDownloadConfig' section");
        // Make sure the element was added in the right location and with the correct default value
        assertEquals(((Element) folderDownloadConfig.getContent(3)).getName(), "enabledForAnonymous");
        assertEquals(folderDownloadConfig.getChildText("enabledForAnonymous", folderDownloadConfig.getNamespace()), "false");
    }
}