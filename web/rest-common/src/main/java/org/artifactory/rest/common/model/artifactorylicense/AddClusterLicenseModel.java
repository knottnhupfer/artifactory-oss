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

package org.artifactory.rest.common.model.artifactorylicense;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Model that represent one or more licenses.
 * <pre>
 * {@code
 * {
 *      "licenses" : [
 *          {
 *              "licenseKey" : "abcd"
 *          },{
 *              "licenseKey" : "efgh"
 *          }
 *      ]
 * }
 * }
 * </pre>
 *
 * @author Shay Bagants
 */
public class AddClusterLicenseModel {

    @JsonProperty("licenses")
    private List<BaseLicenseDetails> licenses = new ArrayList<>();

    public List<BaseLicenseDetails> getLicenses() {
        return licenses;
    }

    public void setLicenses(List<BaseLicenseDetails> licenses) {
        this.licenses = licenses;
    }

    public void addLicense(BaseLicenseDetails licenseDetails) {this.licenses.add(licenseDetails);}

    /**
     * Model that represent single license.
     */
    public static class AddLicenseModel {

        private String licenseKey;

        public String getLicenseKey() {
            return licenseKey;
        }

        public void setLicenseKey(String licenseKey) {
            this.licenseKey = licenseKey;
        }
    }
}
