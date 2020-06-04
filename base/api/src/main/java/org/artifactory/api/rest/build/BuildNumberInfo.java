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
import lombok.Data;
import org.artifactory.api.build.model.BuildGeneralInfo;

/**
 * @author Lior Gur
 */
@Builder
@AllArgsConstructor
@Data
public class BuildNumberInfo {

    private String buildName;
    private String buildNumber;
    private String ciUrl;
    private String buildStat;
    private String buildTime;
    private long time;
    private boolean canDelete;

    public BuildNumberInfo() {
    }

    public BuildNumberInfo(BuildGeneralInfo generalInfo) {
        this.buildName = generalInfo.getBuildName();
        this.buildNumber = generalInfo.getBuildNumber();
        this.ciUrl = generalInfo.getCiUrl();
        this.buildStat = generalInfo.getBuildStat();
        this.canDelete = generalInfo.getCanDelete();

    }

}
