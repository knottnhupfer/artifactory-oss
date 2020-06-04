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

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author gidis
 */
public class LdapPoisoningProtectionConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(LdapPoisoningProtectionConverter.class);

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();
        Element security = root.getChild("security", ns);
        if (security == null) {
            log.debug("no security settings");
            return;
        }

        Element ldapSettings = security.getChild("ldapSettings", ns);
        if (ldapSettings == null) {
            log.debug("no ldap settings");
            return;
        }

        List ldapSettingList = ldapSettings.getChildren("ldapSetting", ns);
        if (ldapSettingList == null) {
            log.debug("no ldap settings");
            return;
        }
        for (Object ldapSettingObject : ldapSettingList) {
            Element ldapSetting = (Element) ldapSettingObject;
            Element objectInjectionProtection = ldapSetting.getChild("ldapPoisoningProtection", ns);
            if (objectInjectionProtection == null) {
                log.debug("ldap object injection protection");
                objectInjectionProtection = new Element("ldapPoisoningProtection", ns);
                objectInjectionProtection.setText("true");
                ldapSetting.addContent(objectInjectionProtection);
            }
        }
    }
}
