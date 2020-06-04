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

package org.artifactory.addon.license;

/**
 * Internal model that should be used to retrieve the license details from the AddonManager for Pro and Trial.
 * This is not a REST-API model!
 *
 * @author Shay Bagants
 */
public class ArtifactoryBaseLicenseDetails {

    private String type;
    private String validThrough;
    private String licensedTo;

    public ArtifactoryBaseLicenseDetails() {
    }

    public ArtifactoryBaseLicenseDetails(String type, String validThrough, String licensedTo) {
        this.type = type;
        this.validThrough = validThrough;
        this.licensedTo = licensedTo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValidThrough() {
        return validThrough;
    }

    public void setValidThrough(String validThrough) {
        this.validThrough = validThrough;
    }

    public String getLicensedTo() {
        return licensedTo;
    }

    public void setLicensedTo(String licensedTo) {
        this.licensedTo = licensedTo;
    }
}
