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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.licenses;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.rest.common.model.BaseModel;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @author Dan Feldman
 */
public class GeneralTabLicenseModel extends BaseModel {

    private String name;
    private String url;
    private Boolean approved = null;

    @JsonIgnore
    public static final GeneralTabLicenseModel NOT_FOUND = createNotFound();

    public GeneralTabLicenseModel() {

    }

    public GeneralTabLicenseModel(String name) {
        this.name = name;
        this.url = null;
    }

    /**
     * Constructor for licenseInfo
     */
    public GeneralTabLicenseModel(LicenseInfo licenseInfo) {
        if (licenseInfo.getName().equals(LicenseInfo.UNKNOWN)) {
            this.name = LicenseInfo.UNKNOWN + "(" + licenseInfo.getLongName() + ")";
            this.approved = false;
        } else {
            this.name = licenseInfo.getName();
            this.approved = licenseInfo.isApproved();
        }
        this.url = StringUtils.isNotBlank(licenseInfo.getUrl()) ? licenseInfo.getUrl() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    @JsonIgnore
    private static GeneralTabLicenseModel createNotFound() {
        GeneralTabLicenseModel notFound = new GeneralTabLicenseModel();
        notFound.url = "";
        notFound.approved = false;
        notFound.name = LicenseInfo.NOT_FOUND;
        return notFound;
    }

    @JsonIgnore
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeneralTabLicenseModel)) return false;

        GeneralTabLicenseModel that = (GeneralTabLicenseModel) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getUrl() != null ? !getUrl().equals(that.getUrl()) : that.getUrl() != null) return false;
        return !(getApproved() != null ? !getApproved().equals(that.getApproved()) : that.getApproved() != null);

    }

    @JsonIgnore
    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getUrl() != null ? getUrl().hashCode() : 0);
        result = 31 * result + (getApproved() != null ? getApproved().hashCode() : 0);
        return result;
    }
}
