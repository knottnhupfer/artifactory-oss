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

package org.artifactory.ui.rest.model.admin.configuration.licenses;

import org.artifactory.api.license.ArtifactLicenseModel;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Chen Kainan
 */
public class License extends ArtifactLicenseModel implements RestModel {

    private String status;
    License() {
    }

    public License(LicenseInfo licenseInfo) {
        if (licenseInfo != null) {
            super.setApproved(licenseInfo.isApproved());
            super.setComments(licenseInfo.getComments());
            super.setLongName(licenseInfo.getLongName());
            super.setName(licenseInfo.getName());
            super.setRegexp(licenseInfo.getRegexp());
            super.setUrl(licenseInfo.getUrl());
        }
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
