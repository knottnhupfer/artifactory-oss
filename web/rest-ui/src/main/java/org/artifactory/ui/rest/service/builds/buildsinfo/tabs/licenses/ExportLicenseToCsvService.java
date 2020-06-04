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

package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.licenses;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.license.ModuleLicenseModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.StreamRestResponse;
import org.artifactory.ui.rest.model.admin.configuration.licenses.ExportLicense;
import org.artifactory.ui.rest.model.builds.BuildLicenseModel;
import org.artifactory.ui.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExportLicenseToCsvService implements RestService {

    private BuildService buildService;
    private AddonsManager addonsManager;

    @Autowired
    public ExportLicenseToCsvService(BuildService buildService, AddonsManager addonsManager) {
        this.buildService = buildService;
        this.addonsManager = addonsManager;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String name = request.getQueryParamByKey("name");
        String buildNumber = request.getQueryParamByKey("number");
        String buildStarted = DateUtils.formatBuildDate(Long.parseLong(request.getQueryParamByKey("date")));
        buildService.assertReadPermissions(name, buildNumber, buildStarted);
        Collection<ModuleLicenseModel> models = ((BuildLicenseModel) request.getImodel()).getLicenses();
        if (models != null && !models.isEmpty()) {
            String licenseCsv = addonsManager.addonByType(LicensesAddon.class).generateLicenseCsv(models);
            ((StreamRestResponse) response).setDownloadFile("licenses.csv");
            ((StreamRestResponse) response).setDownload(true);
            ExportLicense exportLicense = new ExportLicense(licenseCsv);
            response.iModel(exportLicense);
        }
    }
}
