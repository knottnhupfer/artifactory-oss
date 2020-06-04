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

package org.artifactory.version.converter.v220;

import org.artifactory.version.converter.v160.AddonsLayoutConverterTestBase;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertNull;

/**
 * @author Lior Gur
 */
@Test
public class DownloadRedirectConverterTest extends AddonsLayoutConverterTestBase {

    // Convert the previous descriptor and ensure that all addon layouts (including composer) are now exists
    public void convert() throws Exception {
        Document document = convertXml("/config/test/config.2.2.0.download_redirect.xml",
                new DownloadRedirectConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element localRepositoriesElement = rootElement.getChild("localRepositories", namespace);
        Element remoteRepositoriesElement = rootElement.getChild("remoteRepositories", namespace);

        assertDownloadRedirect(localRepositoriesElement, namespace);
        assertDownloadRedirect(remoteRepositoriesElement, namespace);
        assertDownloadRedirectFileMinimumSize(rootElement, namespace);
    }

    private void assertDownloadRedirect(Element localRepositoriesElement, Namespace namespace) {
        List<Element> localRepositoriesElementChildren = localRepositoriesElement.getChildren();

        for (Element element : localRepositoriesElementChildren) {
            assertNull(element.getChild("downloadRedirect", namespace));

        }
    }

    private void assertDownloadRedirectFileMinimumSize(Element rootElement, Namespace namespace) {
        assertNull(rootElement.getChild("downloadRedirectConfig", namespace));
    }
}
