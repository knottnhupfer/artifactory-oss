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

package org.artifactory.ui.rest.service.admin.security.auth.forgotpassword;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.sso.saml.SamlSsoAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.mail.MailServerDescriptor;
import org.artifactory.descriptor.security.sso.SamlSettings;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
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
public class LoginRelatedDataService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();

        UserLogin userLogin = request.getImodel() != null ? (UserLogin) request.getImodel() : new UserLogin();
        // update saml provider link
        updateSamlLinkIfEnable(request, descriptor, userLogin);
        // update display forgot password flag
        updateDisplayForgotFlag(descriptor, userLogin);
        updateCanRememberMeFlag(userLogin);
        response.iModel(userLogin);
    }

    /**
     * update display forgot password flag
     *
     * @param descriptor - config descriptor
     * @param userLogin  - user details
     */
    private void updateDisplayForgotFlag(CentralConfigDescriptor descriptor, UserLogin userLogin) {
        MailServerDescriptor mailServer = descriptor.getMailServer();
        boolean isMailServerEnable = (mailServer != null && mailServer.isEnabled()) ? true : false;
        if (isMailServerEnable) {
            userLogin.setForgotPassword(true);
        } else {
            userLogin.setForgotPassword(false);
        }
    }

    private void updateCanRememberMeFlag(UserLogin userLogin) {
        userLogin.setCanRememberMe(!ConstantValues.securityDisableRememberMe.getBoolean());
    }

    /**
     * @param request    - encapsulate data related to request
     * @param descriptor - config descriptor
     * @param userLogin  - user login details
     */
    private void updateSamlLinkIfEnable(ArtifactoryRestRequest request, CentralConfigDescriptor descriptor,
            UserLogin userLogin) {
        SamlSettings samlSettings = descriptor.getSecurity().getSamlSettings();
        if (samlSettings != null && samlSettings.isEnableIntegration()) {
            AddonsManager addons = ContextHelper.get().beanForType(AddonsManager.class);
            SamlSsoAddon samlSsoAddon = addons.addonByType(SamlSsoAddon.class);
            String samlLoginIdentityProviderUrl = samlSsoAddon.getSamlLoginIdentityProviderUrl(
                    request.getServletRequest(), userLogin.getRedirectTo());
            // add sso link if available
            userLogin.setSsoProviderLink(samlLoginIdentityProviderUrl);
        }
    }

}
