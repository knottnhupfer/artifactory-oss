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

package org.artifactory.rest.common.util.build;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.build.BuildService;
import org.artifactory.build.BuildInfoUtils;
import org.artifactory.build.BuildRun;
import org.artifactory.util.DoesNotExistException;

import java.util.Date;
import java.util.Set;

/**
 * @author Dan Feldman
 */
public class BuildResourceHelper {

    private BuildResourceHelper() {
    }

    /**
     * Validates the parameters of the move\copy request and returns the basic build info object if found without permission validation
     *
     * @param buildName   Name of build to target
     * @param buildNumber Number of build to target
     * @param started     Start date of build to target (can be null)
     * @return Basic info of build to target
     */
    public static BuildRun validateParamsAndGetBuildInfo(String buildName, String buildNumber, String started, BuildService buildService) {
        if (StringUtils.isBlank(buildName)) {
            throw new IllegalArgumentException("Build name cannot be blank.");
        }
        if (StringUtils.isBlank(buildNumber)) {
            throw new IllegalArgumentException("Build number cannot be blank.");
        }
        BuildRun requestedBuild = getRequestedBuildInfo(buildName, buildNumber, started, buildService);
        if (requestedBuild == null) {
            throw new DoesNotExistException("Cannot find build by the name '" + buildName + "' and the number '" +
                    buildNumber + "' which started on " + started + ".");
        }
        return requestedBuild;
    }

    /**
     * Returns the basic info object of the build to target without permission validation
     *
     * @param buildName   Name of build to target
     * @param buildNumber Number of build to target
     * @param started     Start date of build to target (can be null)
     * @return Basic info of build to target
     */
    private static BuildRun getRequestedBuildInfo(String buildName, String buildNumber, String started, BuildService buildService) {
        Set<BuildRun> buildRuns = buildService.searchBuildsByNameAndNumberInternal(buildName, buildNumber);
        if (buildRuns.isEmpty()) {
            throw new DoesNotExistException("Cannot find builds by the name '" + buildName + "' and the number '" + buildNumber + "'.");
        }
        BuildRun requestedBuild = null;
        if (StringUtils.isBlank(started)) {
            for (BuildRun buildRun : buildRuns) {
                if ((requestedBuild == null) || requestedBuild.getStartedDate().before(buildRun.getStartedDate())) {
                    requestedBuild = buildRun;
                }
            }
        } else {
            Date requestedStartDate = new Date(BuildInfoUtils.parseBuildTime(started));
            requestedBuild = buildRuns.stream()
                    .filter(buildRun -> buildRun.getStartedDate().equals(requestedStartDate))
                    .findFirst()
                    .orElse(null);
        }
        return requestedBuild;
    }
}
