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

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.artifactory.util.RepoLayoutUtils.CONAN_DEFAULT_NAME;

/**
 * Changing the Conan RepoLayout to be the new one for v2
 *
 * @author Yuval Reches
 */
public class ConanV2DefaultLayoutConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(ConanV2DefaultLayoutConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting Conan default repository layout v2 conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element repoLayouts = rootElement.getChild("repoLayouts", namespace);
        if (repoLayouts != null) {
            Element conanRepoLayout = repoLayouts.getChildren().stream()
                    .filter(element -> CONAN_DEFAULT_NAME.equals(element.getChild("name", namespace).getText()))
                    .findFirst().orElse(null);
            if (conanRepoLayout == null) {
                log.warn("Couldn't find Conan default RepoLayout, conversion aborts");
                return;
            }
            convertConanLayout(namespace, conanRepoLayout);
        }
        log.info("Conan default repository layout v2 conversion finished successfully");
    }

    private void convertConanLayout(Namespace namespace, Element repoLayout) {
        // Mandatory fields
        Element patternElement = repoLayout.getChild("artifactPathPattern", namespace);
        patternElement.setText("[org]/[module]/[baseRev]/[channel<[^/]+>]/[folderItegRev]/(package/[package_id<[^/]+>]/[fileItegRev]/)?[remainder<(?:.+)>]");

        // Fields that may or may not be there
        Element folderIntegrationRevisionElement = repoLayout.getChild("folderIntegrationRevisionRegExp", namespace);
        String folderIntegrationRevision = "[^/]+";
        if (folderIntegrationRevisionElement == null) {
            folderIntegrationRevisionElement = new Element("folderIntegrationRevisionRegExp", namespace);
            folderIntegrationRevisionElement.setText(folderIntegrationRevision);
            repoLayout.addContent(4, folderIntegrationRevisionElement);
        } else {
            folderIntegrationRevisionElement.setText(folderIntegrationRevision);
        }

        Element fileIntegrationRevisionElement = repoLayout.getChild("fileIntegrationRevisionRegExp", namespace);
        String fileIntegrationRevision = "[^/]+";
        if (fileIntegrationRevisionElement == null) {
            fileIntegrationRevisionElement = new Element("fileIntegrationRevisionRegExp", namespace);
            fileIntegrationRevisionElement.setText(fileIntegrationRevision);
            repoLayout.addContent(5, fileIntegrationRevisionElement);
        } else {
            fileIntegrationRevisionElement.setText(fileIntegrationRevision);
        }
    }

}




