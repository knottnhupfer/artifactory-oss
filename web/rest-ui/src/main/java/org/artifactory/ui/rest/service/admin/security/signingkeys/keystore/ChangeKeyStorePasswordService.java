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

package org.artifactory.ui.rest.service.admin.security.signingkeys.keystore;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.signingkey.KeyStore;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ChangeKeyStorePasswordService implements RestService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        KeyStore keyStore = (KeyStore) request.getImodel();
        //update key store password
        updateKeyStorePassword(response, keyStore);
    }

    /**
     * update keyStore password
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param keyStore            - key store model
     */
    private void updateKeyStorePassword(RestResponse artifactoryResponse, KeyStore keyStore) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        ArtifactWebstartAddon artifactWebstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
        String password = keyStore.getPassword();
        try {
            artifactWebstartAddon.setKeyStorePassword(password);
            artifactoryResponse.info("Successfully updated Key Store password");
        } catch (Exception e) {
            artifactoryResponse.error(e.getMessage());
        }
    }
}
