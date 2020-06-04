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
 * Internal model that should be used to retrieve the license details from the AddonManager for HA.
 * This is not a REST-API model!
 *
 * @author Shay Bagants
 */
public class ArtifactoryHaLicenseDetails extends ArtifactoryBaseLicenseDetails {

    private String licenseHash;
    private Boolean expired;
    private String nodeId;
    private String nodeUrl;

    public ArtifactoryHaLicenseDetails(String type, String validThrough, String licensedTo,
            String licenseHash, Boolean expired, String nodeId, String nodeUrl) {
        super(type, validThrough, licensedTo);
        this.licenseHash = licenseHash;
        this.expired = expired;
        this.nodeId = nodeId;
        this.nodeUrl = nodeUrl;
    }

    public String getLicenseHash() {
        return licenseHash;
    }

    public void setLicenseHash(String licenseHash) {
        this.licenseHash = licenseHash;
    }

    public Boolean isExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }
}
