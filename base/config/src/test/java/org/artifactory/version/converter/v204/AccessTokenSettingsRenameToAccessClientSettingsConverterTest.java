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

package org.artifactory.version.converter.v204;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Yinon Avraham.
 */
@Test
public class AccessTokenSettingsRenameToAccessClientSettingsConverterTest extends XmlConverterTest {

    private static final String CONFIG_XML_WITH_ACCESS_TOKEN_SETTINGS = "/config/test/config.2.0.3.with_accessTokenSettings.xml";
    private static final String CONFIG_XML_WITHOUT_ACCESS_TOKEN_SETTINGS = "/config/test/config.2.0.3.without_accessTokenSettings.xml";

    private final AccessTokenSettingsRenameToAccessClientSettingsConverter converter = new AccessTokenSettingsRenameToAccessClientSettingsConverter();

    public void convertWithPreviousData() throws Exception {
        Document document = convertXml(CONFIG_XML_WITH_ACCESS_TOKEN_SETTINGS, converter);
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();
        Element security = root.getChild("security", ns);
        Element accessTokenSettings = security.getChild("accessTokenSettings", ns);
        assertNull(accessTokenSettings);
        Element accessClientSettings = security.getChild("accessClientSettings", ns);
        assertNotNull(accessClientSettings);
        assertEquals(accessClientSettings.getChild("userTokenMaxExpiresInMinutes", ns).getValue(), "60");
        assertEquals(accessClientSettings.getChildren().size(), 1);
    }

    public void convertWithoutPreviousData() throws Exception {
        Document document = convertXml(CONFIG_XML_WITHOUT_ACCESS_TOKEN_SETTINGS, converter);
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();
        Element security = root.getChild("security", ns);
        Element accessTokenSettings = security.getChild("accessTokenSettings", ns);
        assertNull(accessTokenSettings);
        Element accessClientSettings = security.getChild("accessClientSettings", ns);
        assertNull(accessClientSettings);
    }

}
