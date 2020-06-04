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
import org.artifactory.ui.rest.model.admin.security.ssl.CertificateDeleteModel;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Shay Bagants
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RemoveCertificateService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetAllCertificateDataService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        CertificateDeleteModel certsToDeleteModel = (CertificateDeleteModel) request.getImodel();
        if (certsToDeleteModel == null || CollectionUtils.isNullOrEmpty(certsToDeleteModel.getCertificates())) {
            response.error("Invalid request. You must provide at lease one certificate to remove.").responseCode(400);
            return;
        }
        List<String> internalAliases = certsToDeleteModel.getCertificates().stream()
                .filter(StringUtils::isNotBlank)
                .map(alias -> alias = ArtifactWebstartAddon.SSL_CERT_ALIAS_PREFIX + alias)
                .collect(Collectors.toList());
        if (CollectionUtils.isNullOrEmpty(internalAliases)) {
            response.error("Invalid request. You must provide at lease one valid certificate to remove.")
                    .responseCode(400);
            return;
        }
        removeCertificates(response, internalAliases);
    }

    private void removeCertificates(RestResponse response, List<String> internalAliases) {
        ArtifactWebstartAddon webstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
        int deletedCertificates = 0;
        log.debug("Attempting to delete certificate name");
        for (String alias : internalAliases) {
            try {
                boolean deleteResult = webstartAddon.removeKeyPair(alias);
                if (deleteResult) {
                    deletedCertificates++;
                    log.debug("{}, has been successfully removed", alias);
                }
            } catch (Exception e) {
                log.error("Failed to delete certificate. {}", e.getMessage());
                log.debug("Failed to delete certificate. ", e);
            }
        }
        updateResponse(response, internalAliases, deletedCertificates);
    }

    private void updateResponse(RestResponse response, List<String> internalAliases, int deletedCertificates) {
        if (deletedCertificates == 0) {
            response.responseCode(400).error("Failed to delete certificate/s. See log for further details");
            return;
        }
        if (deletedCertificates == internalAliases.size()) {
            // Check if removing single certificates or bulk and send the right notification
            if (deletedCertificates == 1) {
                String item = internalAliases.get(0);
                response.info("Successfully deleted '" +
                    StringUtils.substringAfter(item, ArtifactWebstartAddon.SSL_CERT_ALIAS_PREFIX) +
                        "' certificate");
            } else {
                response.info("Successfully deleted " + deletedCertificates + " certificate");
            }
            return;
        }
        if (deletedCertificates != internalAliases.size()) {
            response.info("Succeeded with errors. " + deletedCertificates + " of " + internalAliases.size() +
                    " certificate were removed. See logs for further details");
        }
    }
}
