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

package org.artifactory.version.converter.v180;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNull;

/**
 * Unit tests for {@link ExternalProvidersRemovalConverter}.
 *
 * @author Yossi Shaul
 */
@Test
public class ExternalProvidersRemovalConverterTest extends XmlConverterTest {

    public void removeExternalProviders() throws Exception {
        Document document = convertXml("/config/test/config.1.7.9-external-providers.xml",
                new ExternalProvidersRemovalConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element externalProviders = rootElement.getChild("externalProviders", namespace);
        assertNull(externalProviders, "External providers section should not be present");
    }

    public void noExternalProviderSection() throws Exception {
        Document document = convertXml("/config/install/config.1.7.9.xml", new ExternalProvidersRemovalConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element externalProviders = rootElement.getChild("externalProviders", namespace);
        assertNull(externalProviders, "External providers section should not be present");
    }
}
