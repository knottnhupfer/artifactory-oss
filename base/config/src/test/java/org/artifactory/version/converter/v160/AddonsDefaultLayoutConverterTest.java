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

package org.artifactory.version.converter.v160;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Shay Yaakov
 */
@Test
public class AddonsDefaultLayoutConverterTest extends AddonsLayoutConverterTestBase {

    public void convert() throws Exception {
        Document document = convertXml("/config/test/config-1.5.13-no_addons_layouts.xml", new AddonsDefaultLayoutConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element repoLayoutsElement = rootElement.getChild("repoLayouts", namespace);
        assertNotNull(repoLayoutsElement, "Converted configuration should contain default repo layouts.");
        checkForDefaultLayouts(repoLayoutsElement, namespace);
    }

    public void convertExisting() throws Exception {
        Document document = convertXml("/config/test/config-1.5.13-existing_addon_layout.xml", new AddonsDefaultLayoutConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element repoLayoutsElement = rootElement.getChild("repoLayouts", namespace);
        assertNotNull(repoLayoutsElement, "Converted configuration should contain default repo layouts.");
        checkForDefaultLayouts(repoLayoutsElement, namespace);

        checkLayout(repoLayoutsElement.getChildren(), namespace, "art-nuget-default",
                "[orgPath]/[module]/[module].[baseRev](-[fileItegRev]).nupkg",
                "false",
                null,
                ".*",
                ".*");
    }

    public void convertExistingRandom() throws Exception {
        Document document = convertXml("/config/test/config-1.5.13-random_addon_layout.xml", new AddonsDefaultLayoutConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element repoLayoutsElement = rootElement.getChild("repoLayouts", namespace);
        assertNotNull(repoLayoutsElement, "Converted configuration should contain default repo layouts.");
        checkForDefaultLayouts(repoLayoutsElement, namespace);

        long nugetLayoutsCount = repoLayoutsElement.getChildren()
                .stream()
                .filter(element -> element.getChild("name", namespace).getText().contains("nuget"))
                .count();
        assertEquals(nugetLayoutsCount, 3);
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
        checkForDefaultSimpleLayout(repoLayoutElements, namespace);
    }
}