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
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RemoveKeyStoreService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String keyName = request.getQueryParamByKey("name");
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        // check if key store exist , if so the it return key store pairs
        getKeyStoreKeyPair(addonsManager, response, keyName);
    }

    /**
     * check if key store exist , if so the it return key store pairs
     *
     * @param addonsManager
     */
    private void getKeyStoreKeyPair(AddonsManager addonsManager, RestResponse response, String keyPairName) {
        ArtifactWebstartAddon artifactWebstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
        boolean isKeyPairRemoveSucceeded = artifactWebstartAddon.removeKeyPair(keyPairName);
        if (isKeyPairRemoveSucceeded) {
            response.info("Successfully removed keypair '" + keyPairName + "'");
        } else {
            response.error("Failed to remove keypair '" + keyPairName + "'");
        }
    }
}
