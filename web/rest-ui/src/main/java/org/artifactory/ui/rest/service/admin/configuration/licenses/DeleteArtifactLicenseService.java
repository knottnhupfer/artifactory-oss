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

package org.artifactory.ui.rest.service.admin.configuration.licenses;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.licenses.DeleteLicensesModel;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteArtifactLicenseService<T extends DeleteLicensesModel> implements RestService<T> {
    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        T model = request.getImodel();
        for (String licenseId : model.getLicenseskeys()) {
            // get license addon
            LicensesAddon licensesAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                    LicensesAddon.class);
            // delete artifact license
            LicenseInfo licenseInfo = licensesAddon.getLicenseByName(licenseId);
            licensesAddon.deleteLicenseInfo(licenseInfo);
        }
        if(model.getLicenseskeys().size()>1){
            response.info("Successfully removed "+model.getLicenseskeys().size()+" licenses");
        }else if(model.getLicenseskeys().size()==1){
            response.info("Successfully removed license '" + model.getLicenseskeys().get(0) + "'");
        }
    }
}
