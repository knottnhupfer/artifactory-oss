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

package org.artifactory.version.converter.v207;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds 'enabledForAnonymous' under the Folder Download config section with default value false
 *
 * @author Rotem Kfir
 */
public class FolderDownloadForAnonymousConfigConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(FolderDownloadForAnonymousConfigConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting to add 'Enable Folder Download For Anonymous Access' config conversion");
        Element rootElement = doc.getRootElement();
        Element folderDownloadConfig = rootElement.getChild("folderDownloadConfig", rootElement.getNamespace());
        Element enabledForAnonymous = folderDownloadConfig.getChild("enabledForAnonymous", folderDownloadConfig.getNamespace());
        if (enabledForAnonymous == null) {
            log.info("No enabledForAnonymous config found - adding default one");
            enabledForAnonymous = new Element("enabledForAnonymous", folderDownloadConfig.getNamespace()).setText("false");

            int enabledLocation = findLastLocation(folderDownloadConfig, "enabled");
            folderDownloadConfig.addContent(enabledLocation + 1, new Text("\n        "));
            folderDownloadConfig.addContent(enabledLocation + 2, enabledForAnonymous);
        }
        log.info("Finished to add 'Enable Folder Download For Anonymous Access' config conversion");
    }
}
