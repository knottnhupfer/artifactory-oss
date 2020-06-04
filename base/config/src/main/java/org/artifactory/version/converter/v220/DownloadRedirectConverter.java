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

package org.artifactory.version.converter.v220;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lior Gur
 */
public class DownloadRedirectConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(DownloadRedirectConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting Download redirect conversion");
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();
        Element localRepositories = root.getChild("localRepositories", ns);
        Element remoteRepositories = root.getChild("remoteRepositories", ns);

        deleteDownloadRedirectFromRepository(ns, localRepositories);
        deleteDownloadRedirectFromRepository(ns, remoteRepositories);

        if(root.removeChild("downloadRedirectConfig", ns)) {
            log.debug("downloadRedirectConfig has been removed");
        }

        log.info("Finished Download redirect conversion");
    }

    private void deleteDownloadRedirectFromRepository(Namespace ns, Element repositories) {
        if (repositories != null) {
            for (Element repositorie : repositories.getChildren()) {
                if (repositorie != null){
                    if(repositorie.removeChild("downloadRedirect", ns)){
                        log.debug("{} : downloadRedirect has been removed" , repositorie.getChild("key", ns).getValue());

                    }
                }

            }
        }
    }
}
