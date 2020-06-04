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

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Helper class for the layout converters
 *
 * @author Shay Bagants
 */
public class AddonsDefaultLayoutConverterHelper {

    /**
     * Return a layout Element from the provided layout attributes
     */
    public static Element getRepoLayoutElement(Element repoLayoutsElement, Namespace namespace, String name, String artifactPathPattern,
            String distinctiveDescriptorPathPattern, String descriptorPathPattern,
            String folderIntegrationRevisionRegExp, String fileIntegrationRevisionRegExp) {

        // Maybe the user already configured *-default, if so randomize the name
        for (Element repoLayoutElement : repoLayoutsElement.getChildren()) {
            if (name.equals(repoLayoutElement.getChild("name", namespace).getText())) {
                if (repoLayoutsElement.getChild("art-" + name, namespace) != null) {
                    name += RandomStringUtils.randomNumeric(3);
                } else {
                    name = "art-" + name;
                }
            }
        }

        Element repoLayoutElement = new Element("repoLayout", namespace);

        Element nameElement = new Element("name", namespace);
        nameElement.setText(name);
        repoLayoutElement.addContent(nameElement);

        Element artifactPathPatternElement = new Element("artifactPathPattern", namespace);
        artifactPathPatternElement.setText(artifactPathPattern);
        repoLayoutElement.addContent(artifactPathPatternElement);

        Element distinctiveDescriptorPathPatternElement = new Element("distinctiveDescriptorPathPattern", namespace);
        distinctiveDescriptorPathPatternElement.setText(distinctiveDescriptorPathPattern);
        repoLayoutElement.addContent(distinctiveDescriptorPathPatternElement);

        if (StringUtils.isNotBlank(descriptorPathPattern)) {
            Element descriptorPathPatternElement = new Element("descriptorPathPattern", namespace);
            descriptorPathPatternElement.setText(descriptorPathPattern);
            repoLayoutElement.addContent(descriptorPathPatternElement);
        }

        if (StringUtils.isNotBlank(folderIntegrationRevisionRegExp)) {
            Element folderIntegrationRevisionRegExpElement = new Element("folderIntegrationRevisionRegExp", namespace);
            folderIntegrationRevisionRegExpElement.setText(folderIntegrationRevisionRegExp);
            repoLayoutElement.addContent(folderIntegrationRevisionRegExpElement);
        }

        if (StringUtils.isNotBlank(fileIntegrationRevisionRegExp)) {
            Element fileIntegrationRevisionRegExpElement = new Element("fileIntegrationRevisionRegExp", namespace);
            fileIntegrationRevisionRegExpElement.setText(fileIntegrationRevisionRegExp);
            repoLayoutElement.addContent(fileIntegrationRevisionRegExpElement);
        }

        return repoLayoutElement;
    }

}
