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

/**
 * @author Chen Keinan
 */
public class BinariesSummary {

    private String binariesCount;
    private String binariesSize;
    private String artifactsSize;
    private String optimization;
    private String itemsCount;
    private String artifactsCount;
    private String totalSize;

    public String getBinariesSize() {
        return binariesSize;
    }

    public void setBinariesSize(String binariesSize) {
        this.binariesSize = binariesSize;
    }

    public String getArtifactsSize() {
        return artifactsSize;
    }

    public void setArtifactsSize(String artifactsSize) {
        this.artifactsSize = artifactsSize;
    }

    public String getOptimization() {
        return optimization;
    }

    public void setOptimization(String optimization) {
        this.optimization = optimization;
    }

    public String getBinariesCount() {
        return binariesCount;
    }

    public void setBinariesCount(String binariesCount) {
        this.binariesCount = binariesCount;
    }

    public String getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(String itemsCount) {
        this.itemsCount = itemsCount;
    }

    public String getArtifactsCount() {
        return artifactsCount;
    }

    public void setArtifactsCount(String artifactsCount) {
        this.artifactsCount = artifactsCount;
    }

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }
}
