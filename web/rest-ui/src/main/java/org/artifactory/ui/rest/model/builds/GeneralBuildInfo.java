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

package org.artifactory.ui.rest.model.builds;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.artifactory.api.build.model.BuildGeneralInfo;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.rest.common.model.RestPaging;
import org.artifactory.rest.common.util.BintrayRestHelper;

/**
 * @author Chen Keinan
 */
@Builder
@AllArgsConstructor
public class GeneralBuildInfo extends BaseModel implements RestPaging {

    private String buildName;
    private String buildNumber;
    private String ciUrl;
    private String releaseStatus;
    private String agent;
    private String buildAgent;
    private String lastBuildTime;
    private String duration;
    private String principal;
    private String artifactoryPrincipal;
    private String url;
    private Long time;
    private String buildStat;
    private Boolean userCanDistribute;
    private Boolean isBuildFullView;
    private Boolean canManage;
    private Boolean canDelete;


    public GeneralBuildInfo() {
    }

    public GeneralBuildInfo(BuildGeneralInfo generalInfo) {
        this.buildName = generalInfo.getBuildName();
        this.lastBuildTime = generalInfo.getLastBuildTime();
        this.buildNumber = generalInfo.getBuildNumber();
        this.ciUrl = generalInfo.getCiUrl();
        this.releaseStatus = generalInfo.getReleaseStatus();
        this.agent = generalInfo.getAgent();
        this.buildAgent = generalInfo.getBuildAgent();
        this.duration = generalInfo.getDuration();
        this.principal = generalInfo.getPrincipal();
        this.artifactoryPrincipal = generalInfo.getArtifactoryPrincipal();
        this.url = generalInfo.getUrl();
        this.time = generalInfo.getTime();
        this.buildStat = generalInfo.getBuildStat();
        userCanDistribute = BintrayRestHelper.userCanDistributeBuild();
        isBuildFullView = generalInfo.getIsBuildFullView();
        canManage = generalInfo.getCanManage();
        canDelete = generalInfo.getCanDelete();
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public String getLastBuildTime() {
        return lastBuildTime;
    }

    public void setLastBuildTime(String lastBuildTime) {
        this.lastBuildTime = lastBuildTime;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getCiUrl() {
        return ciUrl;
    }

    public void setCiUrl(String ciUrl) {
        this.ciUrl = ciUrl;
    }

    public String getReleaseStatus() {
        return releaseStatus;
    }

    public void setReleaseStatus(String releaseStatus) {
        this.releaseStatus = releaseStatus;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getBuildAgent() {
        return buildAgent;
    }

    public void setBuildAgent(String buildAgent) {
        this.buildAgent = buildAgent;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getArtifactoryPrincipal() {
        return artifactoryPrincipal;
    }

    public void setArtifactoryPrincipal(String artifactoryPrincipal) {
        this.artifactoryPrincipal = artifactoryPrincipal;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBuildStat() {
        return buildStat;
    }

    public void setBuildStat(String buildStat) {
        this.buildStat = buildStat;
    }

    public Boolean getUserCanDistribute() {
        return userCanDistribute;
    }

    public Boolean getBuildFullView() {
        return isBuildFullView;
    }

    public void setBuildFullView(Boolean buildFullView) {
        isBuildFullView = buildFullView;
    }

    public Boolean getCanManage() {
        return canManage;
    }

    public void setCanManage(Boolean canManage) {
        this.canManage = canManage;
    }

    public Boolean getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(Boolean canDelete) {
        this.canDelete = canDelete;
    }
}
