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

package org.artifactory.ui.rest.service.admin.configuration.registerpro;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.AddRemoveLicenseVerificationResult;
import org.artifactory.addon.license.ArtifactoryBaseLicenseDetails;
import org.artifactory.addon.license.LicenseOperationStatus;
import org.artifactory.addon.license.VerificationResult;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.util.AolUtils;
import org.artifactory.ui.rest.model.admin.configuration.registerpro.ProLicense;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Chen Keinan
 */
@Component
public class UpdateLicenseKeyService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(UpdateLicenseKeyService.class);

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("UpdateLicenseKey");
        AddonsManager addonsManager = getAddonsManager();
        boolean hasLicenseAlready = addonsManager.isLicenseInstalled();
        // read permission checks
        if (hasLicenseAlready && !authorizationService.isAdmin()) {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            log.error("Forbidden UI REST call from user: '{}'", authorizationService.currentUsername());
            return;
        }
        try {
            ProLicense proLicense = (ProLicense) request.getImodel();
            // try to install license
            LicenseOperationStatus status = addonsManager.addAndActivateLicense(proLicense.getKey(), true, false);
            // update response with license validation result
            updateResponseWithLicenseInstallResult(response, status, addonsManager, hasLicenseAlready);
        } catch (Exception e) {
            response.error("The license key is not valid");
            log.error(e.toString());
        }
    }

    /**
     * update response with license install result
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param status  - The license add and activate status
     */
    private void updateResponseWithLicenseInstallResult(RestResponse artifactoryResponse,
            LicenseOperationStatus status, AddonsManager addonsManager, boolean hasLicenseAlready) {

        // If an exception caught, return error
        if (status.hasException()) {
            artifactoryResponse.error("Unable to install license. " + status.getException().getCause());
            return;
        }

        // If an error occurred while adding the license, return error
        Map<String, AddRemoveLicenseVerificationResult> addResult = status
                .getAddRemoveLicenseVerificationResult();
        if (addResult.size() > 0) {
            Map.Entry<String, AddRemoveLicenseVerificationResult> addStatus
                    = addResult.entrySet().iterator().next();
            if (!addStatus.getValue().isValid()) {
                artifactoryResponse.error(addStatus.getValue().showMassage());
                return;
            }
        }

        // If an error occurred while activating the license, return error
        Map<String, VerificationResult> activationResults = status.getLicenseActivationResult();
        if (activationResults != null && activationResults.size() > 0) {
            Map.Entry<String, VerificationResult> result = activationResults.entrySet().iterator().next();
            String installResult = result.getValue().showMassage();
            if (result.getValue().isValid()) {
                updateFeedbackMessage(artifactoryResponse, addonsManager, hasLicenseAlready);
            } else {
                artifactoryResponse.error(installResult);
            }
        }
    }

    /**
     * update feedback message for new license or update license
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param addonsManager       - add on manager
     * @param hasLicenseAlready   - if true license installed already
     */
    private void updateFeedbackMessage(RestResponse artifactoryResponse, AddonsManager addonsManager,
            boolean hasLicenseAlready) {
        String licenseType = addonsManager.getProductName();
        if (addonsManager.isLicenseInstalled()) {
            ArtifactoryBaseLicenseDetails licenseDetails = addonsManager.getProAndAolLicenseDetails();
            licenseType = licenseDetails.getType();
        }
        if (hasLicenseAlready) {
            artifactoryResponse.info("Successfully updated " + licenseType + " license");
        } else {
            artifactoryResponse.info("Successfully created " + licenseType + " license");
        }
    }

    /**
     * get addon manager from application context
     *
     * @return addon manager
     */
    private AddonsManager getAddonsManager() {
        ArtifactoryContext artifactoryContext = ContextHelper.get();
        return artifactoryContext.beanForType(AddonsManager.class);
    }

}
