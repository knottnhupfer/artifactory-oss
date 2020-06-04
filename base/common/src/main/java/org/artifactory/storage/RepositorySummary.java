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

package org.artifactory.storage;

import org.artifactory.api.repo.storage.RepoStorageSummaryInfo;
import org.artifactory.util.NumberFormatter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.jfrog.storage.common.StorageUnit;

/**
 * @author Chen Keinan
 */
public class RepositorySummary {

    private String repoKey;
    private RepoStorageSummaryInfo.RepositoryType repoType;
    private long foldersCount;
    private long filesCount;
    private String usedSpace;
    private long itemsCount;
    @JsonIgnore
    private Double percentage;
    @JsonProperty("percentage")
    private String displayPercentage;
    private String packageType;

    public RepositorySummary() {
    }

    public RepositorySummary(RepoStorageSummaryInfo repoStorageSummaryInfo, long totalSize) {
        this.setRepoKey(repoStorageSummaryInfo.getRepoKey());
        this.setUsedSpace(StorageUnit.toReadableString(repoStorageSummaryInfo.getUsedSpace()));
        this.setPercentage(Double.longBitsToDouble(repoStorageSummaryInfo.getUsedSpace()) / Double.longBitsToDouble(totalSize));
        this.setDisplayPercentage(NumberFormatter.formatPercentage(getPercentage()));
        this.setFilesCount(repoStorageSummaryInfo.getFilesCount());
        this.setFoldersCount(repoStorageSummaryInfo.getFoldersCount());
        this.setItemsCount(repoStorageSummaryInfo.getItemsCount());
        this.packageType = repoStorageSummaryInfo.getType();
        this.repoType = repoStorageSummaryInfo.getRepoType();
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public RepoStorageSummaryInfo.RepositoryType getRepoType() {
        return repoType;
    }

    public void setRepoType(RepoStorageSummaryInfo.RepositoryType repoType) {
        this.repoType = repoType;
    }

    public String getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(String usedSpace) {
        this.usedSpace = usedSpace;
    }

    public long getFoldersCount() {
        return foldersCount;
    }

    public void setFoldersCount(long foldersCount) {
        this.foldersCount = foldersCount;
    }

    public long getFilesCount() {
        return filesCount;
    }

    public void setFilesCount(long filesCount) {
        this.filesCount = filesCount;
    }

    public long getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(long itemsCount) {
        this.itemsCount = itemsCount;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getDisplayPercentage() {
        return displayPercentage;
    }

    public void setDisplayPercentage(String displayPercentage) {
        this.displayPercentage = displayPercentage;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }
}
