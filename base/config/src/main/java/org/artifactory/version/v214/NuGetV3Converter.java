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

package org.artifactory.version.v214;

import org.apache.commons.lang.StringUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add 'v3FeedUrl' under each remote NuGet config section.
 *
 * @author Maxim Yurkovsky
 */
public class NuGetV3Converter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(NuGetV3Converter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting NuGet 'v3FeedUrl' conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element remoteRepos = rootElement.getChild("remoteRepositories", namespace);
        if (remoteRepos == null || remoteRepos.getChildren() == null || remoteRepos.getChildren().isEmpty()) {
            log.info("Finished NuGet 'v3FeedUrl' conversion (no changed made)");
            return;
        }
        remoteRepos.getChildren().stream()
                .filter(repoElement -> StringUtils.equals(repoElement.getChildText("type", namespace), "nuget"))
                        .forEach(nugetRepo -> addV3FeedUrlElement(namespace, nugetRepo));
        log.info("Finished NuGet 'v3FeedUrl' conversion");
    }

    private void addV3FeedUrlElement(Namespace namespace, Element nugetRepo) {
        String repoKey = nugetRepo.getChildText("key", namespace);
        Element nugetRepoConfig = nugetRepo.getChild("nuget", namespace);
        if(nugetRepoConfig == null) {
            log.info("Couldn't find <nuget> section. Skipping repo {}", repoKey);
            return;
        }
        int index = findLastLocation(nugetRepoConfig, "downloadContextPath");
        nugetRepoConfig.addContent(index+1, new Text("    "));
        Content v3FeedUrl = buildV3FeedUrl(namespace, nugetRepo.getChildText("url", namespace));
        nugetRepoConfig.addContent(index+2, v3FeedUrl);
        nugetRepoConfig.addContent(index+3, new Text("\n            "));
    }

    private Content buildV3FeedUrl(Namespace namespace, String url) {
        String v3FeedUrl = "";
        if (url.equals("https://www.nuget.org/")) {
            v3FeedUrl = "https://api.nuget.org/v3/index.json";
        }
        Element v3FeedUrlElement = new Element("v3FeedUrl", namespace);
        v3FeedUrlElement.addContent(v3FeedUrl);
        return v3FeedUrlElement;
    }
}
