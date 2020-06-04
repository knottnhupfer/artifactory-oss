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

package org.artifactory.ui.rest.service.admin.security.signingkeys;

import com.google.common.base.Joiner;
import org.artifactory.addon.common.gpg.GpgKeyStore;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.security.signingkeys.SigningKeysSettings;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.signingkey.SignKey;
import org.artifactory.util.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetSigningKeyService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    private GpgKeyStore gpgKeyStore;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        SignKey signKey = new SignKey();
        signKey.setPrivateKeyInstalled(gpgKeyStore.hasPrivateKey());
        signKey.setPassPhrase(getPassPhrase());
        boolean publicKeyInstalled = gpgKeyStore.hasPublicKey();
        if (publicKeyInstalled) {
            String link = getKeyLink(request.getServletRequest());
            signKey.setPublicKeyInstalled(true);
            signKey.setPublicKeyLink(link);
        }
        response.iModel(signKey);
    }

    private String getKeyLink(HttpServletRequest request) {
        return Joiner.on('/').join(HttpUtils.getServletContextUrl(request),
                "api", "gpg", "key/public");
    }

    public String getPassPhrase() {
        SigningKeysSettings signingKeysSettings = centralConfigService.getDescriptor().getSecurity().getSigningKeysSettings();
        return signingKeysSettings != null ? CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), signingKeysSettings.getPassphrase()) : null;
    }
}
