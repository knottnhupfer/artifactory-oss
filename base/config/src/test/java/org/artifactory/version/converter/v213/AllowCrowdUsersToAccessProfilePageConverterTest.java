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

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Uriah Levy
 * Validates the allowUserToAccessProfile field is added to the crowdSettings
 */
public class AllowCrowdUsersToAccessProfilePageConverterTest extends XmlConverterTest {

    private static final String CONFIG_XML_WITHOUT_PROFILE =
            "/config/test/config.2.1.1.w_crowd_wo_allowUsersToAccessProfile.xml";

    private final AllowCrowdUsersToAccessProfilePageConverter converter = new AllowCrowdUsersToAccessProfilePageConverter();

    @Test
    public void convertWithoutPreviousData() throws Exception {
        Document document = convertXml(CONFIG_XML_WITHOUT_PROFILE, converter);
        validateXml(document);
    }

    private void validateXml(Document document) {
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();
        Element security = root.getChild("security", ns);
        Element crowdSettings = security.getChild("crowdSettings", ns);
        Element allowUsersToAccessProfile = crowdSettings.getChild("allowUserToAccessProfile", ns);
        Assert.assertNotNull(allowUsersToAccessProfile);
        Assert.assertTrue(allowUsersToAccessProfile.getText().equals("false"));
    }

}