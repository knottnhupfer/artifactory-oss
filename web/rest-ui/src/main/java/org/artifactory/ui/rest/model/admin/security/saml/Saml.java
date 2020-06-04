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

package org.artifactory.ui.rest.model.admin.security.saml;

import org.artifactory.descriptor.security.sso.SamlSettings;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Chen Keinan
 */
public class Saml extends SamlSettings implements RestModel {

    Saml() {
    }

    public Saml(SamlSettings samlSettings) {
        if (samlSettings != null) {
            super.setEnableIntegration(samlSettings.isEnableIntegration());
            super.setCertificate(samlSettings.getCertificate());
            super.setLoginUrl(samlSettings.getLoginUrl());
            super.setLogoutUrl(samlSettings.getLogoutUrl());
            super.setNoAutoUserCreation(samlSettings.getNoAutoUserCreation());
            super.setServiceProviderName(samlSettings.getServiceProviderName());
            super.setAllowUserToAccessProfile(samlSettings.isAllowUserToAccessProfile());
            super.setAutoRedirect(samlSettings.isAutoRedirect());
            super.setSyncGroups(samlSettings.isSyncGroups());
            super.setGroupAttribute(samlSettings.getGroupAttribute());
            super.setEmailAttribute(samlSettings.getEmailAttribute());
            super.setUseEncryptedAssertion(samlSettings.isUseEncryptedAssertion());
        }
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
