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

package org.artifactory.ui.rest.model.admin.configuration.registerpro;

import org.artifactory.addon.license.ArtifactoryBaseLicenseDetails;
import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class ProLicense extends BaseModel {

    public ProLicense() {
    }

    public ProLicense(ArtifactoryBaseLicenseDetails licenseDetails, String key) {
        if (licenseDetails != null ) {
            this.setLicenseTo(licenseDetails.getLicensedTo());
            this.setValidThough(licenseDetails.getValidThrough());
            this.setLicenseType(licenseDetails.getType());
            this.setKey(key);
        }
    }

    private String key;
    private String licenseTo;
    private String validThough;
    private String licenseType;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLicenseTo() {
        return licenseTo;
    }

    public void setLicenseTo(String licenseTo) {
        this.licenseTo = licenseTo;
    }

    public String getValidThough() {
        return validThough;
    }

    public void setValidThough(String validThough) {
        this.validThough = validThough;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }
}
