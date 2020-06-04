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

package org.artifactory.api.build;

import org.apache.commons.lang.StringUtils;

/**
 * @author Chen Keinan
 */
public class GeneralBuild {

    private long buildId;
    private String buildName;
    private String buildNumber;
    private long buildDate;
    private String ciUrl;
    private long created;
    private String createdBy;
    private long modified;
    private String modifiedBy;
    private String numOfModules;
    private String numOfArtifacts;
    private String numOfDependencies;
    private String status;
    private Long promotionCreated;


    public GeneralBuild(long buildId, String buildName, String buildNumber, long buildDate, String ciUrl, long created,
                        String createdBy, long modified, String modifiedBy) {
        if (buildId <= 0L) {
            throw new IllegalArgumentException("Build id cannot be zero or negative!");
        }
        if (StringUtils.isBlank(buildName) || StringUtils.isBlank(buildNumber) || buildDate <= 0L) {
            throw new IllegalArgumentException("Build name, number and date cannot be empty or null!");
        }
        if (created <= 0L) {
            throw new IllegalArgumentException("Created date cannot be zero or negative!");
        }
        this.buildId = buildId;
        this.buildName = buildName;
        this.buildNumber = buildNumber;
        this.buildDate = buildDate;
        this.ciUrl = ciUrl;
        this.created = created;
        this.createdBy = createdBy;
        this.modified = modified;
        this.modifiedBy = modifiedBy;
    }

    public GeneralBuild(String buildName, String buildNumber, long buildDate, String ciUrl, String status){
        this.buildName = buildName;
        this.buildNumber = buildNumber;
        this.buildDate = buildDate;
        this.ciUrl = ciUrl;
        this.status = status;
    }


    public long getBuildId() {
        return buildId;
    }

    public void setBuildId(long buildId) {
        this.buildId = buildId;
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public long getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(long buildDate) {
        this.buildDate = buildDate;
    }

    public String getCiUrl() {
        return ciUrl;
    }

    public void setCiUrl(String ciUrl) {
        this.ciUrl = ciUrl;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getNumOfModules() {
        return numOfModules;
    }

    public void setNumOfModules(String numOfModules) {
        this.numOfModules = numOfModules;
    }

    public String getNumOfArtifacts() {
        return numOfArtifacts;
    }

    public void setNumOfArtifacts(String numOfArtifacts) {
        this.numOfArtifacts = numOfArtifacts;
    }

    public String getNumOfDependencies() {
        return numOfDependencies;
    }

    public void setNumOfDependencies(String numOfDependencies) {
        this.numOfDependencies = numOfDependencies;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getPromotionCreated() {
        return promotionCreated;
    }

    public void setPromotionCreated(Long promotionCreated) {
        this.promotionCreated = promotionCreated;
    }
}
