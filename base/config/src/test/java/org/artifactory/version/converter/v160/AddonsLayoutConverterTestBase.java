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

import org.apache.commons.lang.StringUtils;
import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Base class for the layout converters tests
 *
 * @author Shay Bagants
 */
public class AddonsLayoutConverterTestBase extends XmlConverterTest {

    protected void checkForDefaultNuGetLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "nuget-default",
                "[orgPath]/[module]/[module].[baseRev](-[fileItegRev]).nupkg",
                "false",
                null,
                ".*",
                ".*");
    }

    protected void checkForDefaultNpmLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "npm-default",
                "[orgPath]/[module]/[module]-[baseRev](-[fileItegRev]).tgz",
                "false",
                null,
                ".*",
                ".*");
    }

    protected void checkForDefaultBowerLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "bower-default",
                "[orgPath]/[module]/[module]-[baseRev](-[fileItegRev]).[ext]",
                "false",
                null,
                ".*",
                ".*");
    }

    protected void checkForDefaultVcsLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "vcs-default",
                "[orgPath]/[module]/[refs<tags|branches>]/[baseRev]/[module]-[baseRev](-[fileItegRev])(-[classifier]).[ext]",
                "false",
                null,
                ".*",
                "[a-zA-Z0-9]{40}");
    }

    protected void checkForDefaultSbtLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "sbt-default",
                "[org]/[module]/(scala_[scalaVersion<.+>])/(sbt_[sbtVersion<.+>])/[baseRev]/[type]s/[module](-[classifier]).[ext]",
                "true",
                "[org]/[module]/(scala_[scalaVersion<.+>])/(sbt_[sbtVersion<.+>])/[baseRev]/[type]s/ivy.xml",
                "\\d{14}",
                "\\d{14}");
    }

    protected void checkForDefaultSimpleLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "simple-default",
                "[orgPath]/[module]/[module]-[baseRev](-[fileItegRev]).[ext]",
                "false",
                null,
                ".*",
                ".*");
    }

    // In version 4.6.0 there is a converter to remote the 'fileItegRev'
    protected void checkForDefaultSimpleLayoutAfterVer460(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "simple-default",
                "[orgPath]/[module]/[module]-[baseRev].[ext]",
                "false",
                null,
                ".*",
                ".*");
    }

    protected void checkForDefaultComposerLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "composer-default",
                "[orgPath]/[module]/[module]-[baseRev](-[fileItegRev]).[ext]",
                "false",
                null,
                ".*",
                ".*");
    }

    protected void checkForDefaultPuppetLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "puppet-default",
                "[orgPath]/[module]/[orgPath]-[module]-[baseRev].tar.gz",
                "false",
                null,
                ".*",
                ".*");
    }

    protected void checkForDefaultConanLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "conan-default",
                "[module]/[baseRev]@[org]/[channel<[^/]+>][remainder<(?:.*)>]",
                "false",
                null,
                ".*",
                ".*");
    }

    protected void checkForDefaultGoLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "go-default",
                "[orgPath]/[module]/@v/v[refs].zip",
                "false",
                null,
                ".*",
                ".*");
    }

    protected void checkForFixedDefaultConanLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "conan-default",
                "[org]/[module]/[baseRev]/[channel<[^/]+>][remainder<(?:.*)>].[ext]",
                "false",
                null,
                ".*",
                ".*");
    }

    protected void checkForDefaultConanV2Layout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "conan-default",
                "[org]/[module]/[baseRev]/[channel<[^/]+>]/[folderItegRev]/(package/[package_id<[^/]+>]/[fileItegRev]/)?[remainder<(?:.+)>]",
                "false",
                null,
                "[^/]+",
                "[^/]+");
    }

    protected void checkForDefaultBuildRepoLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "build-default",
                "[orgPath]/[module](-[fileItegRev]).[ext]",
                "false",
                null,
                ".*",
                ".*");
    }

    protected void checkLayout(List<Element> repoLayoutElements, Namespace namespace, String layoutName,
            String artifactPathPattern, String distinctiveDescriptorPathPattern, String descriptorPathPattern,
            String folderIntegrationRevisionRegExp, String fileIntegrationRevisionRegExp) {

        boolean foundLayout = false;
        for (Element repoLayoutElement : repoLayoutElements) {
            if (layoutName.equals(repoLayoutElement.getChild("name", namespace).getText())) {
                checkLayoutElement(repoLayoutElement, namespace, layoutName, artifactPathPattern,
                        distinctiveDescriptorPathPattern, descriptorPathPattern, folderIntegrationRevisionRegExp,
                        fileIntegrationRevisionRegExp);
                foundLayout = true;
            }
        }
        assertTrue(foundLayout, "Could not find the default layout: " + layoutName);
    }

    private void checkLayoutElement(Element repoLayoutElement, Namespace namespace, String layoutName,
            String artifactPathPattern, String distinctiveDescriptorPathPattern, String descriptorPathPattern,
            String folderIntegrationRevisionRegExp, String fileIntegrationRevisionRegExp) {

        checkLayoutField(repoLayoutElement, namespace, layoutName, "artifactPathPattern", artifactPathPattern,
                "artifact path pattern");

        checkLayoutField(repoLayoutElement, namespace, layoutName, "distinctiveDescriptorPathPattern",
                distinctiveDescriptorPathPattern, "distinctive descriptor path pattern");

        if (StringUtils.isNotBlank(descriptorPathPattern)) {
            checkLayoutField(repoLayoutElement, namespace, layoutName, "descriptorPathPattern", descriptorPathPattern,
                    "descriptor path pattern");
        } else {
            assertNull(repoLayoutElement.getChild("descriptorPathPattern"));
        }

        checkLayoutField(repoLayoutElement, namespace, layoutName, "folderIntegrationRevisionRegExp",
                folderIntegrationRevisionRegExp, "folder integration revision reg exp");

        checkLayoutField(repoLayoutElement, namespace, layoutName, "fileIntegrationRevisionRegExp",
                fileIntegrationRevisionRegExp, "file integration revision reg exp");
    }

    private void checkLayoutField(Element repoLayoutElement, Namespace namespace, String layoutName, String childName,
            String expectedChildValue, String childDisplayName) {
        Element childElement = repoLayoutElement.getChild(childName, namespace);
        assertNotNull(childElement, "Could not find " + childDisplayName + " element in default repo layout: " +
                layoutName);
        assertEquals(childElement.getText(), expectedChildValue, "Unexpected " + childDisplayName +
                " in default repo layout: " + layoutName);
    }
}
