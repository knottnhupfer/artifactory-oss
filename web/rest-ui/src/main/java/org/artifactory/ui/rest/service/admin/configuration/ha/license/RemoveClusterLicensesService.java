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

package org.artifactory.ui.rest.service.admin.configuration.ha.license;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.AddRemoveLicenseVerificationResult;
import org.artifactory.addon.license.LicenseOperationStatus;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.model.artifactorylicense.RemoveClusterLicenseModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.util.AolUtils;
import org.artifactory.rest.exception.ForbiddenWebAppException;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.artifactory.addon.ArtifactoryRunningMode.HA;
import static org.artifactory.rest.common.model.artifactorylicense.RemoveClusterLicenseModel.RemoveLicenseModel;

/**
 * UI service that handles license removal
 *
 * @author Shay Bagants
 */
@Component
public class RemoveClusterLicensesService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(RemoveClusterLicensesService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private AuthorizationService authService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        // Block using on AOL and instances that are not HA configured
        AolUtils.assertNotAol("RemoveLicenseKeys");
        assertHaConfigured();
        if (!authService.isAdmin()) {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Get the request data
        RemoveClusterLicenseModel model = (RemoveClusterLicenseModel) request.getImodel();
        if (model != null && CollectionUtils.notNullOrEmpty(model.getLicenses())) {
            log.debug("Checking if licenses to delete were provided by the request");
            Set<String> licensesToRemove = model.getLicenses().stream()
                    .map(RemoveLicenseModel::getLicenseHash)
                    .collect(Collectors.toSet());

            if (CollectionUtils.notNullOrEmpty(licensesToRemove)) {
                LicenseOperationStatus status = new LicenseOperationStatus();
                addonsManager.removeLicenses(licensesToRemove, status);
                updateFeedbackMessage(response, status, licensesToRemove.size());
            } else {
                response.error("Invalid input");
            }
        }
    }

    private void updateFeedbackMessage(RestResponse response, LicenseOperationStatus status,
            int numOfLicensesFromUser) {
        String message;
        if (status.hasException()) {
            message = "Unable to remove Artifactory licenses. See artifactory.log file for further details";
            log.error("Unable to remove Artifactory licenses", status.getException().getCause());
            response.error(message);
            return;
        }

        Map<String, AddRemoveLicenseVerificationResult> results = status.getAddRemoveLicenseVerificationResult();
        List<Map.Entry<String, AddRemoveLicenseVerificationResult>> inUseLicenses = results.entrySet().stream()
                .filter(entry -> entry.getValue().equals(AddRemoveLicenseVerificationResult.licenseInUse))
                .collect(Collectors.toList());
        if (CollectionUtils.notNullOrEmpty(inUseLicenses)) {
            if (numOfLicensesFromUser == inUseLicenses.size()) {
                message = "Unable to remove Artifactory licenses. See artifactory.log file for further details";
                inUseLicenses
                        .forEach(entry -> log.warn("Unable to remove Artifactory license:\n'{}'. {} ", entry.getKey(),
                                entry.getValue().showMassage()));
                response.error(message).responseCode(400);
                return;
            }

            if (numOfLicensesFromUser > inUseLicenses.size()) {
                message = "Succeeded with errors: " + inUseLicenses.size() + " out of " + numOfLicensesFromUser +
                        " licenses were not removed. See artifactory.log file for further details";
                inUseLicenses
                        .forEach(entry -> log.warn("Unable to remove Artifactory license:\n'{}'. {} ", entry.getKey(),
                                entry.getValue().showMassage()));
                response.info(message);
            }
        }
    }

    /**
     * Ensure that HA configured instances (has ha node props) are not allowed to use this service
     */
    private void assertHaConfigured() {
        if (!addonsManager.getArtifactoryRunningMode().equals(HA)) {
            throw new ForbiddenWebAppException("In order to use this function, it is required to configure your Artifactory" +
                    "instance as HA.");
        }
    }
}
