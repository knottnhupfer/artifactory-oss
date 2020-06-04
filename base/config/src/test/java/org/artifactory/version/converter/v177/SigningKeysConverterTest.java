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

package org.artifactory.version.converter.v177;

import org.artifactory.convert.XmlConverterTest;
import org.artifactory.version.converter.v178.SigningKeysConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * @author Uriah Levy
 */
public class SigningKeysConverterTest extends XmlConverterTest {
    private String CONFIG_XML = "/config/test/config.1.7.7_signing_settings.xml";

    @Test
    public void convert() throws Exception {
        Document document = convertXml(CONFIG_XML, new SigningKeysConverter());
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();
        Element security = root.getChild("security", ns);
        validateConfig(security.getChild("signingKeysSettings", ns), ns);
    }

    private void validateConfig(Element signingKeysSettings, Namespace namespace) {
        // ensure debianSettings is re-named to signingKeysSettings
        assertTrue(signingKeysSettings.getName().equals("signingKeysSettings"));
    }

}
