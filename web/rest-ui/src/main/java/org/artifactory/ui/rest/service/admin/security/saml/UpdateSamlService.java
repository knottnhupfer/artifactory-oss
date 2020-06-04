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

package org.artifactory.ui.rest.service.admin.security.saml;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.sso.saml.SamlSsoAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.sso.SamlSettings;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateSamlService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(UpdateSamlService.class);

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        //update saml setting
        updateSamlSetting(request, response);
    }

    /**
     * update saml setting
     *
     * @param artifactoryRequest - encapsulate data related to request
     */
    private void updateSamlSetting(ArtifactoryRestRequest artifactoryRequest, RestResponse response) {
        try {
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            SamlSsoAddon samlSsoAddon = addonsManager.addonByType(SamlSsoAddon.class);
            SamlSettings samlSettings = (SamlSettings) artifactoryRequest.getImodel();
            if (samlSettings.isEnableIntegration()) {
                samlSsoAddon.createCertificate(samlSettings.getCertificate());
                if (samlSettings.isUseEncryptedAssertion()) {
                    samlSsoAddon.createStoreAndGetKeyPair(false);
                }
            }
            samlSettings.setCertificate(samlSettings.getCertificate());
            updateSaml(samlSettings);
            response.info("Successfully updated SAML SSO settings");
        } catch (Exception e) {
            String message = "Error occurred while updating SAML settings";
            log.error(message + ". {}", e.getMessage());
            response.error(message + ", please review the log");
        }
    }

    private void updateSaml(SamlSettings samlSettings) {
        MutableCentralConfigDescriptor centralConfig = centralConfigService.getMutableDescriptor();
        SecurityDescriptor securityDescriptor = centralConfig.getSecurity();
        securityDescriptor.setSamlSettings(samlSettings);
        centralConfigService.saveEditedDescriptorAndReload(centralConfig);
    }
}
