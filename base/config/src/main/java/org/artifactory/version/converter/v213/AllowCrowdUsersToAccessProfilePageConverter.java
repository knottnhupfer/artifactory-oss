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

package org.artifactory.version.converter.v213;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Uriah Levy
 * Adds allowUserToAccessProfile boolean to existing crowdSettings
 */
public class AllowCrowdUsersToAccessProfilePageConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(AllowCrowdUsersToAccessProfilePageConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting conversion of allowUserToAccessProfile in Crowd settings");
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();
        Element security = root.getChild("security", ns);
        if (security == null) {
            log.debug("No security settings, skipping AllowCrowdUsersToAccessProfilePageConverter");
            return;
        }

        Element crowdSettings = security.getChild("crowdSettings", ns);
        if (crowdSettings == null) {
            log.debug("No crowd settings, skipping AllowCrowdUsersToAccessProfilePageConverter");
            return;
        }

        Element allowUserToAccessProfile = crowdSettings.getChild("allowUserToAccessProfile", ns);
        Element noAutoUserCreation = crowdSettings.getChild("noAutoUserCreation", ns);
        if (allowUserToAccessProfile == null) {
            log.info("No allowUserToAccessProfile setting found present in Crowd settings, setting as false for default");
            allowUserToAccessProfile = new Element("allowUserToAccessProfile", ns);
            allowUserToAccessProfile.setText("false");
            int addAtIndex = crowdSettings.indexOf(noAutoUserCreation) + 1;
            crowdSettings.addContent(addAtIndex, allowUserToAccessProfile);
        }
        log.info("Finish config conversion of allowUserToAccessProfile in Crowd settings");
    }
}
