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

package org.artifactory.version.converter.v215;

import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dudim
 * RTFACT-16408 - Convert conan default distribution rule to the right coordinates
 */

public class ConanDefaultDistributionRuleConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(ConanDefaultDistributionRuleConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting conan default distribution rule converter");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        List<Element> distributionRepositories = getDistributionRepositories(rootElement, namespace);
        for (Element distributionRepo : distributionRepositories) {
            if (distributionRepo.getContent() != null) {
                Element rules = distributionRepo.getChild("rules", namespace);
                if (rules != null) {
                    convertConanTypeRules(namespace, distributionRepo, rules);
                }
            }
        }
        log.info("Conan convert default distribution rule finished successfully");
    }

    private void convertConanTypeRules(Namespace namespace, Element distributionRepo, Element rules) {
        for (Element rule : rules.getChildren()) {
            if (RepoType.Conan.getType().equals(rule.getChild("type", namespace).getText())) {
                Element distributionCoordinates = rule.getChild("distributionCoordinates", namespace);
                if (isMalformedCoordinates(distributionCoordinates, namespace)) {
                    log.debug("Found malformed coordinates at: '{}'",
                            distributionRepo.getChildText("key", namespace));
                    updateDistributionCoordinates(distributionCoordinates, namespace);
                    log.debug("Finished updating coordinates for repo: '{}'",
                            distributionRepo.getChildText("key", namespace));
                }
            }
        }
    }

    private void updateDistributionCoordinates(Element distributionCoordinates, Namespace namespace) {
        distributionCoordinates.getChild("pkg", namespace).setText("${module}:${org}");
        distributionCoordinates.getChild("version", namespace).setText("${baseRev}:${channel}");
    }

    private boolean isMalformedCoordinates(Element distributionCoordinates, Namespace namespace) {
        return "${packageName}".equals(distributionCoordinates.getChildText("pkg", namespace)) &&
                "${packageVersion}".equals(distributionCoordinates.getChildText("version", namespace)) &&
                "${artifactPath}".equals(distributionCoordinates.getChildText("path", namespace));
    }

    private List<Element> getDistributionRepositories(Element rootElement, Namespace namespace) {
        Element distributionRepositoriesRootElement = rootElement.getChild("distributionRepositories", namespace);
        if (distributionRepositoriesRootElement != null && distributionRepositoriesRootElement.getContent() != null) {
            return distributionRepositoriesRootElement.getChildren();
        }
        return new ArrayList<>();
    }

}