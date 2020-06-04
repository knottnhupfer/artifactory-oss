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
import org.artifactory.version.converter.v221.PyPIRegistrySuffixConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import static org.artifactory.version.converter.v221.PyPIRegistrySuffixConverter.DEFAULT_PYPI_SUFFIX;
import static org.testng.Assert.*;

/**
 * Test the addition of 'repositorySuffix' under each of the remote repos PyPI config section.
 *
 * @author Nadav Yogev
 */
public class PyPIRegistrySuffixConverterTest extends XmlConverterTest {

    private final PyPIRegistrySuffixConverter converter = new PyPIRegistrySuffixConverter();

    @Test
    public void testConvert() throws Exception {
        String CONFIG_XML = "/config/test/config.2.2.1.pypi.xml";
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
                    Element repositorySuffix = elem.getChild("pypi", ns).getChild("repositorySuffix", ns);
                    assertNotNull(repositorySuffix);
                    assertEquals(repositorySuffix.getText(), DEFAULT_PYPI_SUFFIX);
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