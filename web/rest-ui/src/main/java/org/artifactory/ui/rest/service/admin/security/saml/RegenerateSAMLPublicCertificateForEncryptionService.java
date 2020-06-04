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

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.sso.saml.SamlException;
import org.artifactory.addon.sso.saml.SamlSsoAddon;
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
 * @author Omri Ziv
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RegenerateSAMLPublicCertificateForEncryptionService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(RegenerateSAMLPublicCertificateForEncryptionService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        //update saml setting
        try {
            regenerateCertificate(request, response);
        } catch (SamlException e) {
            response.error(e.getMessage());
            response.responseCode(HttpStatus.SC_BAD_REQUEST);
        }
    }

    /**
     * get saml encrypted assertion public certificate
     *
     * @param artifactoryRequest - encapsulate data related to request
     * @param response - feedback of regenerating operation
     */
    private void regenerateCertificate(ArtifactoryRestRequest artifactoryRequest, RestResponse response)
            throws SamlException {
        SamlSsoAddon samlSsoAddon = addonsManager.addonByType(SamlSsoAddon.class);

        String certificateStr = null;
        certificateStr = samlSsoAddon.createStoreAndGetKeyPair(true);
        if(StringUtils.isNotBlank(certificateStr)) {
            response.responseCode(HttpStatus.SC_OK);
            response.info("Public saml encrypted assertion file regenerate successfully");
        } else {
            response.error("No public saml encrypted assertion file exists in Artifactory");
            response.responseCode(HttpStatus.SC_NOT_FOUND);
        }
    }

}
