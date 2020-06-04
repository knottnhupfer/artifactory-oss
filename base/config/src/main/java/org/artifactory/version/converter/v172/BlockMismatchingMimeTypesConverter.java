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

package org.artifactory.version.converter.v172;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds the 'Block Mismatching Mime Types' flag (on) as default for all remote repositories
 *
 * @author Dan Feldman
 */
public class BlockMismatchingMimeTypesConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(BlockMismatchingMimeTypesConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting conversion: add 'Block mismatching mime type' flag, on by default, to all remote repos ");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element remoteRepos = rootElement.getChild("remoteRepositories", namespace);
        if (remoteRepos != null && !remoteRepos.getChildren().isEmpty()) {
            remoteRepos.getChildren().forEach(this::addDefaultBlockMimeTypes);
        }
        log.info("Finished mismatching mime types conversion.");
    }

    private void addDefaultBlockMimeTypes(Element repo) {
        Element blockMime = repo.getChild("blockMismatchingMimeTypes", repo.getNamespace());
        if (blockMime == null) {
            blockMime = new Element("blockMismatchingMimeTypes", repo.getNamespace());
            int lastLocation = findLocationToInsert(repo);
            repo.addContent(lastLocation + 1, new Text("\n            "));
            repo.addContent(lastLocation + 2, blockMime);
            blockMime.setText("true");
        }
    }

    private int findLocationToInsert(Element repo) {
        return findLastLocation(repo, "contentSynchronisation",
                "vcs",
                "p2OriginalUrl",
                "cocoaPods",
                "bower",
                "pypi",
                "nuget",
                "p2Support",
                "rejectInvalidJars",
                "remoteRepoLayoutRef",
                "listRemoteFolderItems",
                "synchronizeProperties",
                "shareConfiguration",
                "unusedArtifactsCleanupPeriodHours",
                "remoteRepoChecksumPolicyType",
                "missedRetrievalCachePeriodSecs",
                "assumedOfflinePeriodSecs",
                "retrievalCachePeriodSecs",
                "fetchSourcesEagerly",
                "fetchJarsEagerly",
                "storeArtifactsLocally",
                "hardFail",
                "offline",
                "url");
    }
}
