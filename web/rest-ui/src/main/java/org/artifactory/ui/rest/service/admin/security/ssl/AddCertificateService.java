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

package org.artifactory.ui.rest.service.admin.security.ssl;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.exception.BadRequestException;
import org.artifactory.ui.rest.model.admin.security.ssl.CertificateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Shay Bagants
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AddCertificateService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(AddCertificateService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        CertificateModel certificate = (CertificateModel) request.getImodel();
        assertValidRequest(certificate);
        ArtifactWebstartAddon webstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
        try {
            webstartAddon.addPemCertificateToKeystore(certificate.getCertificatePEM(), certificate.getCertificateName());
        } catch (Exception e) {
            log.debug("Could not add certificate.", e);
            response.error("Certificate could not be added. " + e.getMessage());
        }
    }

    private void assertValidRequest(CertificateModel certificate) {
        if (certificate == null) {
            throw new BadRequestException("Certificate is missing");
        }
        if (StringUtils.isBlank(certificate.getCertificateName())) {
            throw new BadRequestException("Certificate name (alias) is missing");
        }
        if (StringUtils.isBlank(certificate.getCertificatePEM())) {
            throw new BadRequestException("Pem file is missing");
        }
    }
}
