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
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.ldap.LdapSettingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetLdapSettingsService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String ldapKey = request.getPathParamByKey("id");
        // retrieve ldap data for view or edit
        fetchLdapViewOrEditData(response, ldapKey);
    }

    private void fetchLdapViewOrEditData(RestResponse artifactoryResponse, String ldapKey) {
        if (ldapKey.length() == 0) {
            List<LdapSettingModel> ldapSettingForView = getLdapSettingForView();
            artifactoryResponse.iModelList(ldapSettingForView);
        } else {
            LdapSettingModel ldapSettingForEdit = getLdapSettingForEdit(ldapKey);
            artifactoryResponse.iModel(ldapSettingForEdit);
        }
    }

    /**
     * get Ldap setting for edit
     *
     * @param ldapKey - ldap repo key
     * @return ldap setting model
     */
    private LdapSettingModel getLdapSettingForEdit(String ldapKey) {
        LdapSetting ldapSettings = centralConfigService.getMutableDescriptor().getSecurity().getLdapSettings(
                ldapKey);
        LdapSettingModel ldapSettingModel = new LdapSettingModel(ldapSettings, false);
        return ldapSettingModel;
    }

    /**
     * return list of ldap settings for view
     */
    private List<LdapSettingModel> getLdapSettingForView() {
        List<LdapSettingModel> settingModelList = new ArrayList<>();
        List<LdapSetting> ldapSettings =
                centralConfigService.getMutableDescriptor().getSecurity().getLdapSettings();
        ldapSettings.forEach(ldapSetting -> settingModelList.add(new LdapSettingModel(ldapSetting, true)));
        return settingModelList;
    }
}
