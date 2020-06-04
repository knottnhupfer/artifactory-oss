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

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Moves the 'enable xray integration' boolean into a a separate xml element that holds the xray config for each repo.
 *
 * @author Dan Feldman
 */
public class XrayRepoConfigConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(XrayRepoConfigConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting Xray repo config conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        convertRepos(rootElement.getChild("localRepositories", namespace), namespace);
        convertRepos(rootElement.getChild("remoteRepositories", namespace), namespace);

        removeOldXrayTags(rootElement, "distributionRepositories", namespace);
        removeOldXrayTags(rootElement, "virtualRepositories", namespace);
        log.info("Finished Xray repo config conversion");
    }

    private void convertRepos(Element repos, Namespace namespace) {
        if (repos != null && repos.getChildren() != null && !repos.getChildren().isEmpty()) {
            repos.getChildren().forEach(repo -> insertNewXrayConfig(repo, namespace));
        }
    }

    //For distribution and virtual - remove xray tags, they make no sense there
    private void removeOldXrayTags(Element rootElement, String repoType, Namespace namespace) {
        Element repos = rootElement.getChild(repoType, namespace);
        if (repos != null && repos.getChildren() != null && !repos.getChildren().isEmpty()) {
            repos.getChildren().forEach(repo -> removeOldXrayTag(repo, namespace));
        }
    }

    private void insertNewXrayConfig(Element repoElement, Namespace namespace) {
        addConfig(repoElement, namespace, removeOldXrayTag(repoElement, namespace));
    }

    //Also returns the value of 'xrayIndex' if it exists for this repo
    private boolean removeOldXrayTag(Element repoElement, Namespace namespace) {
        Element xrayIndex = repoElement.getChild("xrayIndex", namespace);
        if (xrayIndex != null) {
            String repoKey = repoElement.getChild("key", namespace).getText();
            log.debug("Removing the '{}' tag from '{}'", "xrayIndex", repoKey);
            repoElement.removeChild("xrayIndex", namespace);
            return Boolean.parseBoolean(xrayIndex.getValue());
        }
        return false;
    }

    private void addConfig(Element repoElement, Namespace namespace, boolean xrayEnabled) {
        //If xray was already enabled don't set block unscanned to true, it will break behavior
        Element xray = new Element("xray", namespace);
        Namespace xrayNamespace = xray.getNamespace();
        xray.addContent(new Text("\n                "));

        Element index = new Element("enabled", xrayNamespace).setText(String.valueOf(xrayEnabled));
        xray.addContent(index);
        xray.addContent(new Text("\n                "));

        Element unscanned = new Element("blockUnscannedArtifacts", xrayNamespace).setText(String.valueOf(false));
        xray.addContent(unscanned);
        xray.addContent(new Text("\n            "));

        int lastLocation = findLocationToInsert(repoElement);
        if (lastLocation < 0) {
            log.error("Failed to find a correct location to insert Xray config for repo {}, with enabled = {}",
                    repoElement.getChild("key", namespace).getText(), xrayEnabled);
        } else {
            repoElement.addContent(lastLocation + 1, new Text("\n            "));
            repoElement.addContent(lastLocation + 2, xray);
            repoElement.addContent(lastLocation + 3, new Text("\n        "));
        }
    }

    //Has to be inserted as the last element in the RealRepo schema
    private int findLocationToInsert(Element repo) {
        return findElementLastLocation(repo,
                "key",
                "type",
                "description",
                "notes",
                "includesPattern",
                "excludesPattern",
                "repoLayoutRef",
                "dockerApiVersion",
                "forceNugetAuthentication",
                "blackedOut",
                "handleReleases",
                "handleSnapshots",
                "maxUniqueSnapshots",
                "maxUniqueTags",
                "suppressPomConsistencyChecks",
                "propertySets",
                "archiveBrowsingEnabled");
    }

    private int findElementLastLocation(Element parent, String... elements) {
        int lastIndex = -1;
        for (String element : elements) {
            Element child = parent.getChild(element, parent.getNamespace());
            if (child != null) {
                int childIndex = parent.indexOf(child);
                lastIndex = childIndex > lastIndex ? childIndex : lastIndex;
            }
        }
        return lastIndex;
    }
}
