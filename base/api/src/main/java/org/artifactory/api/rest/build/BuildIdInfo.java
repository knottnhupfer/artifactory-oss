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

package org.artifactory.api.rest.build;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.artifactory.api.build.model.BuildGeneralInfo;

/**
 * @author Lior Gur
 */
@Builder
@AllArgsConstructor
public class BuildIdInfo {

    private String buildName;
    private String lastBuildNumber;
    private String lastBuildTime;
    private long time;
    private boolean canDelete;

    public BuildIdInfo() {
    }

    public BuildIdInfo(BuildGeneralInfo generalInfo) {
        this.buildName = generalInfo.getBuildName();
        this.lastBuildNumber = generalInfo.getBuildNumber();
        this.lastBuildTime = generalInfo.getLastBuildTime();
        this.canDelete = generalInfo.getCanDelete();
        this.time = generalInfo.getTime();
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public String getLastBuildNumber() {
        return lastBuildNumber;
    }

    public void setLastBuildNumber(String lastBuildNumber) {
        this.lastBuildNumber = lastBuildNumber;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public String getLastBuildTime() {
        return lastBuildTime;
    }

    public void setLastBuildTime(String lastBuildTime) {
        this.lastBuildTime = lastBuildTime;
    }

    public long getTime() { return time; }

    public void setTime(long time) { this.time = time;}
}
