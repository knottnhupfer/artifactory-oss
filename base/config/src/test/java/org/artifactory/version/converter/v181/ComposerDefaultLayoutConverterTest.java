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

package org.artifactory.version.converter.v181;

import org.artifactory.version.converter.v160.AddonsLayoutConverterTestBase;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

/**
 * @author Shay Bagants
 */
@Test
public class ComposerDefaultLayoutConverterTest extends AddonsLayoutConverterTestBase {

    // Convert the previous descriptor and ensure that all addon layouts (including composer) are now exists
    public void convert() throws Exception {
        Document document = convertXml("/config/test/config.1.8.0_no_composer_layout.xml",
                new ComposerDefaultLayoutConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element repoLayoutsElement = rootElement.getChild("repoLayouts", namespace);
        checkForDefaultLayouts(repoLayoutsElement, namespace);
    }

    // Convert an older descriptor (1.7.5) and ensure that all addon layouts (including composer) are now exists as well
    public void convert175Descriptor() throws Exception {
        Document document = convertXml("/config/test/config.1.7.5_docker_force_auth.xml",
                new ComposerDefaultLayoutConverter());
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
    }
}
