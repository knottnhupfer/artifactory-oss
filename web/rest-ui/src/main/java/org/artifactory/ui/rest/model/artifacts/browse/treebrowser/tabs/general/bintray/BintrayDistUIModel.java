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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.bintray;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.bintray.distribution.model.BintrayDistInfoModel;
import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Shay Yaakov
 */
public class BintrayDistUIModel extends BaseModel {

    private Boolean show;
    private String packageType;
    private String visibility;
    private String licenses;
    private String vcsUrl;
    private String errorMessage;

    public BintrayDistUIModel() {
    }

    public BintrayDistUIModel(BintrayDistInfoModel model) {
        this.packageType = model.packageType;
        this.visibility = model.visibility;
        this.licenses = model.licenses;
        this.vcsUrl = model.vcsUrl;
        this.errorMessage = model.errorMessage;
        this.show = StringUtils.isNotBlank(model.errorMessage);
    }

    public BintrayDistUIModel(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Boolean getShow() {
        return show;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getLicenses() {
        return licenses;
    }

    public void setLicenses(String licenses) {
        this.licenses = licenses;
    }

    public String getVcsUrl() {
        return vcsUrl;
    }

    public void setVcsUrl(String vcsUrl) {
        this.vcsUrl = vcsUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
