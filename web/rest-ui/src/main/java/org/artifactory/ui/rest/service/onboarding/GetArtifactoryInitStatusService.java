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

package org.artifactory.ui.rest.service.onboarding;

import org.artifactory.addon.AddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.AuthenticationHelper;
import org.artifactory.security.UserInfo;
import org.artifactory.security.access.AccessService;
import org.artifactory.ui.rest.model.onboarding.ArtifactoryInitStatusModel;
import org.jfrog.access.rest.user.LoginRequest;
import org.jfrog.security.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.artifactory.api.security.SecurityService.DEFAULT_ADMIN_PASSWORD;
import static org.artifactory.api.security.SecurityService.DEFAULT_ADMIN_USER;
import static org.artifactory.security.FirstStartupHelper.onlyDefaultReposExist;

/**
 * @author nadavy
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetArtifactoryInitStatusService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetArtifactoryInitStatusService.class);

    private SecurityService securityService;
    private AddonsManager addonsManager;
    private CentralConfigService configService;
    private UserGroupService userGroupService;
    private AccessService accessService;

    @Autowired
    public GetArtifactoryInitStatusService(SecurityService securityService, AddonsManager addonsManager,
            CentralConfigService configService, UserGroupService userGroupService, AccessService accessService) {
        this.securityService = securityService;
        this.addonsManager = addonsManager;
        this.configService = configService;
        this.userGroupService = userGroupService;
        this.accessService = accessService;
    }

    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        boolean hasOnlyDefaultRepos = onlyDefaultReposExist(configService.getDescriptor());
        boolean licenseInstalled = addonsManager.isLicenseInstalled();
        boolean hasDefaultPassword = false;
        String username = userGroupService.currentUser().getUsername();
        try {
            accessService.getAccessClient().auth().authenticate(
                    new LoginRequest().username(username)
                            .password(DEFAULT_ADMIN_PASSWORD));
            hasDefaultPassword = true;
        } catch (Exception e) {
            log.debug("Unable to authenticate user: '{}'", username, e);
        }
        boolean hasProxies = !configService.getDescriptor().getProxies().isEmpty();
        boolean skipWizard = ConstantValues.skipOnboardingWizard.getBoolean();
        response.iModel(new ArtifactoryInitStatusModel(hasOnlyDefaultRepos, licenseInstalled, hasPriorLogin(),
                hasDefaultPassword, hasProxies, skipWizard));
    }

    private boolean hasPriorLogin() {
        UserInfo userInfo = userGroupService.currentUser();
        if (userInfo != null && userInfo.getUsername().equals(DEFAULT_ADMIN_USER)) {
            return userInfo.getLastLoginTimeMillis() != 0;
        } else {
            return Optional.ofNullable(securityService.getUserLastLoginInfo(DEFAULT_ADMIN_USER))
                    .map(Pair::getSecond)
                    .map(r -> r > 0)
                    .orElse(false);
        }
    }
}
