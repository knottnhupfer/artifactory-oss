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

package org.artifactory.ui.rest.service.admin.security.user;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.UserLockPolicy;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Michael Pasternak
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateUserLockPolicyService<T extends UserLockPolicy> implements RestService<T> {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        UserLockPolicy userLockPolicy = request.getImodel();

        if (userLockPolicy.getLoginAttempts() > 100 || userLockPolicy.getLoginAttempts()< 1) {
            response.responseCode(400);
            response.error("LoginAttempts must be between 1 - 100");
            return;
        }

        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        UserLockPolicy userLockPolicyConfig = mutableDescriptor.getSecurity().getUserLockPolicy();

        if (userLockPolicyConfig == null) {
            mutableDescriptor.getSecurity().setUserLockPolicy(userLockPolicy);
        } else {
            userLockPolicyConfig.setEnabled(userLockPolicy.isEnabled());
            userLockPolicyConfig.setLoginAttempts(userLockPolicy.getLoginAttempts());
        }
        centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);

        response.info("UserLockPolicy was successfully updated");
    }
}
