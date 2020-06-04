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

package org.artifactory.ui.rest.service.admin.security.httpsso;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.sso.HttpSsoSettings;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.util.AolUtils;
import org.artifactory.ui.rest.model.admin.security.httpsso.HttpSso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetHttpSsoService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        boolean isHttpSsoEnabledAOL = ConstantValues.aolSecurityHttpSsoEnabled.getBoolean();
        if (!isHttpSsoEnabledAOL){
            AolUtils.assertNotAol("GetHttpSso");
        }
        // get security descriptor
        SecurityDescriptor securityDescriptor = getSecurityDescriptor();
        // get http sso
        HttpSso httpSso = getHttpSso(securityDescriptor);
        // update response entity
        response.iModel(httpSso);
    }

    /**
     * get Http Sso instance
     *
     * @param securityDescriptor - security descriptor
     * @return security descriptor
     */
    private HttpSso getHttpSso(SecurityDescriptor securityDescriptor) {
        HttpSsoSettings httpSsoSetting = securityDescriptor.getHttpSsoSettings();
        if (httpSsoSetting == null) {
            HttpSso httpSso = new HttpSso();
            httpSso.setNoAutoUserCreation(false);
            flipCreationLogic(httpSso);
            return httpSso;
        }
        return new HttpSso(httpSsoSetting);
    }

    /**
     * get config descriptor
     *
     * @return config descriptor
     */
    private SecurityDescriptor getSecurityDescriptor() {
        MutableCentralConfigDescriptor centralConfig = centralConfigService.getMutableDescriptor();
        return centralConfig.getSecurity();
    }

    /**
     * flip logic as descriptor expect not auth user creation
     * and ui expect auto user creation
     *
     * @param httpSsoSettings
     */
    private void flipCreationLogic(HttpSsoSettings httpSsoSettings) {
        boolean creation = httpSsoSettings.isNoAutoUserCreation();
        httpSsoSettings.setNoAutoUserCreation(!creation);
    }
}
