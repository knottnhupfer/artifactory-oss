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

import lombok.NoArgsConstructor;
import org.artifactory.api.repo.storage.RepoStorageSummaryInfo;
import org.artifactory.api.storage.BinariesInfo;
import org.jfrog.common.ClockUtils;

import java.io.Serializable;
import java.util.Set;

/**
 * Holds summary about the storage used by the various repositories.
 *
 * @author Yossi Shaul
 */
@NoArgsConstructor
public class StorageSummaryInfo implements Serializable {

    private Set<RepoStorageSummaryInfo> repoStorageSummaries;
    private BinariesInfo binariesInfo;
    private long totalSize;
    private long totalFolders;
    private long totalFiles;
    private long lastUpdated;

    public StorageSummaryInfo(Set<RepoStorageSummaryInfo> repoStorageSummaries, BinariesInfo binariesInfo) {
        this.repoStorageSummaries = repoStorageSummaries;
        this.binariesInfo = binariesInfo;
        for (RepoStorageSummaryInfo s : repoStorageSummaries) {
            totalFolders += s.getFoldersCount();
            totalFiles += s.getFilesCount();
            totalSize += s.getUsedSpace();
        }
        this.lastUpdated = ClockUtils.epochMillis();
    }

    public Set<RepoStorageSummaryInfo> getRepoStorageSummaries() {
        return repoStorageSummaries;
    }

    /**
     * @return Total size, in bytes, of all the stored files in Artifactory. This is the non-checksum optimized storage
     * but can still be smaller than the binary store until storage GC is executed.
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * @return Number of folders stored in Artifactory (excluding repository root folders)
     */
    public long getTotalFolders() {
        return totalFolders;
    }

    /**
     * @return Number of files stored in Artifactory
     */
    public long getTotalFiles() {
        return totalFiles;
    }

    /**
     * @return Number of files and folder stored in Artifactory
     */
    public long getTotalItems() {
        return totalFolders + totalFiles;
    }

    /**
     * @return Returns the information about the binaries stored in Artifactory
     */
    public BinariesInfo getBinariesInfo() {
        return binariesInfo;
    }

    public double getOptimization() {
        double ratio = binariesInfo.getBinariesSize() / new Double(totalSize);
        return ratio;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }
}
