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

package org.artifactory.version.converter.v177;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author gidis
 */
public class LdapPoisoningProtectionConverterTest extends XmlConverterTest {

    @Test
    public void convertConfigWithLdap() throws Exception {
        String CONFIG_WITH_LDAP = "/config/test/config.1.4.3_without_multildap.xml";
        Document document = convertXml(CONFIG_WITH_LDAP, new LdapPoisoningProtectionConverter());
        doTest(document);
    }

    @Test
    public void convertConfigWithoutLdap() throws Exception {
        String CONFIG_WITHOUT_LDAP = "/config/test/config.1.7.5_docker_force_auth.xml";
        Document document = convertXml(CONFIG_WITHOUT_LDAP, new LdapPoisoningProtectionConverter());
        doTest(document);
    }

    private void doTest(Document document) {
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();
        Element security = root.getChild("security", ns);
        Element ldapSettings = security.getChild("ldapSettings", ns);
        List ldapSettingList = ldapSettings.getChildren("ldapSetting", ns);
        for (Object ldapSettingObject : ldapSettingList) {
            Element ldapSetting = (Element) ldapSettingObject;
            Element ldapPoisoningProtection = ldapSetting.getChild("ldapPoisoningProtection", ns);
            Assert.assertTrue("true".equals(ldapPoisoningProtection.getText()));
        }
    }
}
