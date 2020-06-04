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

package org.artifactory.version.converter.v212;

import org.apache.commons.lang.StringUtils;
import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Test the addition of 'pyPIRegistryUrl' under each of the remote repos PyPI config section.
 * The url of the repo becomes the registryUrl as well by default.
 * In case there was no pypi section we add it
 *
 * @author Yuval Reches
 */
public class PyPIRegistryUrlConverterTest extends XmlConverterTest {

    private final PyPIRegistryUrlConverter converter = new PyPIRegistryUrlConverter();

    @Test
    public void testConvert() throws Exception {
        String CONFIG_XML = "/config/test/config.2.1.1.pypi.xml";
        Document document = convertXml(CONFIG_XML, converter);
        validateXml(document);
    }

    private void validateXml(Document document) {
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();
        Element remoteRepositories = root.getChild("remoteRepositories", ns);
        assertFalse(remoteRepositories.getChildren().isEmpty());

        // Validate pypi remote repos
        remoteRepositories.getChildren()
                .stream()
                .filter(elem -> StringUtils.equals(elem.getChildText("type", ns), "pypi"))
                .peek(elem -> assertNotNull(elem.getChild("pypi", ns)))
                .forEach(elem -> {
                    String url = elem.getChildText("url", ns);
                    Element pyPIRegistryUrl = elem.getChild("pypi", ns).getChild("pyPIRegistryUrl", ns);
                    assertNotNull(pyPIRegistryUrl);
                    String pyPIRegistryUrlText = pyPIRegistryUrl.getText();
                    assertNotNull(pyPIRegistryUrlText);
                    assertEquals(url, pyPIRegistryUrlText);
                });

        // Validate other remote repos
        remoteRepositories.getChildren()
                .stream()
                .filter(elem -> !StringUtils.equals(elem.getChildText("type", ns), "pypi"))
                .forEach(elem -> assertNull(elem.getChild("pypi", ns)));

        // Validate no local repos were harmed during the process
        Element localRepositories = root.getChild("localRepositories", ns);
        assertFalse(localRepositories.getChildren().isEmpty());
        localRepositories.getChildren()
                .forEach(elem -> assertNull(elem.getChild("pypi", ns)));
    }

}