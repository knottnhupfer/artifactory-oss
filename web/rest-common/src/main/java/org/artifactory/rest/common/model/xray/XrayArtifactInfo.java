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

package org.artifactory.rest.common.model.xray;

/**
 * @author Yuval Reches
 */
public class XrayArtifactInfo {

    private String xrayIndexStatus;
    private String xrayIndexStatusLastUpdatedTimestamp;
    private Boolean xrayBlocked;
    private String xrayBlockReason;
    private Boolean allowBlockedArtifacts;
    private String detailsUrl;

    public XrayArtifactInfo(){}

    public String getXrayIndexStatus() {
        return xrayIndexStatus;
    }

    public void setXrayIndexStatus(String xrayIndexStatus) {
        this.xrayIndexStatus = xrayIndexStatus;
    }

    public String getXrayIndexStatusLastUpdatedTimestamp() {
        return xrayIndexStatusLastUpdatedTimestamp;
    }

    public void setXrayIndexStatusLastUpdatedTimestamp(String xrayIndexStatusLastUpdatedTimestamp) {
        this.xrayIndexStatusLastUpdatedTimestamp = xrayIndexStatusLastUpdatedTimestamp;
    }

    public Boolean getXrayBlocked() {
        return xrayBlocked;
    }

    public void setXrayBlocked(Boolean xrayBlocked) {
        this.xrayBlocked = xrayBlocked;
    }

    public String getXrayBlockReason() {
        return xrayBlockReason;
    }

    public void setXrayBlockReason(String xrayBlockReason) {
        this.xrayBlockReason = xrayBlockReason;
    }

    public Boolean getAllowBlockedArtifacts() {
        return allowBlockedArtifacts;
    }

    public void setAllowBlockedArtifacts(Boolean allowBlockedArtifacts) {
        this.allowBlockedArtifacts = allowBlockedArtifacts;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }

    public void setDetailsUrl(String detailsUrl) {
        this.detailsUrl = detailsUrl;
    }
}
