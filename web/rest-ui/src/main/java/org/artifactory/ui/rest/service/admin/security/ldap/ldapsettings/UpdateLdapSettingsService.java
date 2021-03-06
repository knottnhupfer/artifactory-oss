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

package org.artifactory.ui.rest.service.admin.security.ldap.ldapsettings;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.ldap.LdapSettingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateLdapSettingsService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String ldapKey = request.getPathParamByKey("id");
        LdapSettingModel ldapSetting = (LdapSettingModel) request.getImodel();
        // update Ldap Setting
        updateLdapSetting(centralConfigService.getMutableDescriptor(), ldapSetting, ldapKey, response);
    }

    /**
     * update ldap settings
     *
     * @param configDescriptor - config descriptor
     * @param ldapSetting      - ldap setting config
     */
    public void updateLdapSetting(MutableCentralConfigDescriptor configDescriptor, LdapSetting ldapSetting,
            String ldapKey, RestResponse response) {
        SecurityDescriptor securityDescriptor = configDescriptor.getSecurity();
        LdapSetting setting = securityDescriptor.getLdapSettings(ldapKey);
        if (setting != null) {
            List<LdapSetting> ldapSettings = securityDescriptor.getLdapSettings();
            int indexOfLdapSetting = ldapSettings.indexOf(ldapSetting);
            if (indexOfLdapSetting != -1) {
                ldapSettings.set(indexOfLdapSetting, ldapSetting);
            }
            centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
            response.info("Successfully updated LDAP settings '" + ldapSetting.getKey() + "'");
        }
    }
}
