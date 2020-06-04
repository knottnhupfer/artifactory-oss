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

package org.artifactory.ui.rest.service.admin.security.general;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.general.SecurityConfig;
import org.artifactory.ui.rest.model.admin.security.login.UserLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetSecurityConfigService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private SecurityService securityService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        UserLogin userLogin = (UserLogin) request.getImodel();

        CentralConfigDescriptor centralConfig = centralConfigService.getMutableDescriptor();
        SecurityDescriptor securityDescriptor = centralConfig.getSecurity();
        SecurityConfig securityConfig = new SecurityConfig(securityDescriptor.isAnonAccessEnabled(),
                securityDescriptor.isBuildGlobalBasicReadAllowed(),
                securityDescriptor.isBuildGlobalBasicReadForAnonymous(),
                securityDescriptor.isHideUnauthorizedResources(),
                securityDescriptor.getPasswordSettings(),
                securityDescriptor.getUserLockPolicy());

        // set number of days left till password expires, userLogin != null only when UI logs a user in
        if (userLogin != null) {
            Integer userPasswordDaysLeft = securityService.getUserPasswordDaysLeft(userLogin.getUser());
            if (userPasswordDaysLeft != null) {
                securityConfig.getPasswordSettings().getExpirationPolicy().setCurrentPasswordValidFor(userPasswordDaysLeft);
            }
        }

        response.iModel(securityConfig);
    }
}
