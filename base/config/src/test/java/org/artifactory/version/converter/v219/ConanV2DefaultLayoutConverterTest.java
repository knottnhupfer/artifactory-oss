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

package org.artifactory.version.converter.v219;

import org.artifactory.version.converter.v160.AddonsLayoutConverterTestBase;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

/**
 * @author Yuval Reches
 */
public class ConanV2DefaultLayoutConverterTest extends AddonsLayoutConverterTestBase {

    // In this config we have all the fields of RepoLayout element
    private static final String CONFIG_WITH_ALL_FIELDS = "/config/test/config.2.1.4.without_nuget_v3_feed_url.xml";
    // In this config we don't have the non mandatory fields of RepoLayout element
    private static final String CONFIG_WITHOUT_ALL_FIELDS = "/config/test/config.2.1.4.without_conan_default_fields.xml";

    @Test
    public void testConvertFromPreviousConfigVersion() throws Exception {
        runTestForConfig(CONFIG_WITH_ALL_FIELDS);
        runTestForConfig(CONFIG_WITHOUT_ALL_FIELDS);
    }

    private void runTestForConfig(String config) throws Exception {
        Document document = convertXml(config, new ConanV2DefaultLayoutConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element repoLayoutsElement = rootElement.getChild("repoLayouts", namespace);
        checkForDefaultLayouts(repoLayoutsElement, namespace);
    }

    private void checkForDefaultLayouts(Element repoLayoutsElement, Namespace namespace) {
        List<Element> repoLayoutElements = repoLayoutsElement.getChildren();

        assertNotNull(repoLayoutElements, "Converted configuration should contain default repo layouts.");
        assertFalse(repoLayoutElements.isEmpty(),
                "Converted configuration should contain default repo layouts.");

        checkForDefaultNuGetLayout(repoLayoutElements, namespace);
        checkForDefaultNpmLayout(repoLayoutElements, namespace);
        checkForDefaultBowerLayout(repoLayoutElements, namespace);
        checkForDefaultVcsLayout(repoLayoutElements, namespace);
        checkForDefaultSbtLayout(repoLayoutElements, namespace);
        checkForDefaultSimpleLayoutAfterVer460(repoLayoutElements, namespace);
        checkForDefaultComposerLayout(repoLayoutElements, namespace);

        // The actual converter we care about
        checkForDefaultConanV2Layout(repoLayoutElements, namespace);
    }
}
