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
import org.artifactory.addon.license.ArtifactoryBaseLicenseDetails;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.util.AolUtils;
import org.artifactory.ui.rest.model.admin.configuration.registerpro.ProLicense;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
public class GetLicenseKeyService implements RestService {
    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("GetLicenseKey");
        AddonsManager addonsManager = getAddonsManager();
        ArtifactoryBaseLicenseDetails licenseDetails = null;
        if (addonsManager.isLicenseInstalled()) {
            licenseDetails = addonsManager.getProAndAolLicenseDetails();
        }
        // update response with license details
        updateResponseWithLicenseDetails(response, addonsManager, licenseDetails);
    }

    /**
     * update response with license details
     *
     * @param artifactoryResponse - encapsulate all data require for response
     * @param addonsManager       - add on manager
     * @param licenseDetails      - license details array
     */
    private void updateResponseWithLicenseDetails(RestResponse artifactoryResponse, AddonsManager addonsManager,
            ArtifactoryBaseLicenseDetails licenseDetails) {
        ProLicense proLicense = new ProLicense(licenseDetails, addonsManager.getLicenseKey());
        artifactoryResponse.iModel(proLicense);
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
