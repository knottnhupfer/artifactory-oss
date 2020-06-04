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

package org.artifactory.ui.rest.service.builds.buildsinfo.tabs;

import org.artifactory.api.build.BuildService;
import org.artifactory.build.BuildRun;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.ui.utils.DateUtils;
import org.jfrog.build.api.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author Chen Keinan
 */
public abstract class AbstractBuildService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(AbstractBuildService.class);

    private static final String PARAM_DATE = "date";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_NUMBER = "number";

    @Autowired
    protected BuildService buildService;

    protected Build getBuild(ArtifactoryRestRequest request, RestResponse response) {
        String datePathParam = request.getPathParamByKey(PARAM_DATE);
        String buildStarted;
        String name = request.getPathParamByKey(PARAM_NAME);
        String buildNumber = request.getPathParamByKey(PARAM_NUMBER);
        buildStarted = parseBuildDate(datePathParam);
        // get license-repo map
        return getBuild(name, buildNumber, buildStarted, response);
    }

    /**
     * get build info
     *
     * @param buildName    - build name
     * @param buildNumber  - build number
     * @param buildStarted - build date
     * @param response     - encapsulate data related to request
     */
    protected Build getBuild(String buildName, String buildNumber, String buildStarted, RestResponse response) {
        boolean buildStartedSupplied = isNotBlank(buildStarted);
        try {
            Build build = getBuild(buildName, buildNumber, buildStarted, buildStartedSupplied);
            if (build == null) {
                StringBuilder builder = new StringBuilder().append("Could not find build '").append(buildName).
                        append("' #").append(buildNumber);
                if (buildStartedSupplied) {
                    builder.append(" that started at ").append(buildStarted);
                }
                throwNotFoundError(response, builder.toString());
            }
            return build;
        } catch (RepositoryRuntimeException e) {
            String errorMessage = "Error locating latest build for '" + buildName + "' #" + buildNumber +
                    ": " + e.getMessage();
            throwInternalError(errorMessage, response);
        }
        //Should not happen
        return null;
    }

    private Build getBuild(String buildName, String buildNumber, String buildStarted, boolean buildStartedSupplied) {
        if (buildStartedSupplied) {
            BuildRun buildRun = buildService.getBuildRun(buildName, buildNumber, buildStarted);
            if (buildRun != null) {
                return buildService.getBuild(buildRun);
            }
        } else {
            // Take the latest build of the specified number
            return buildService.getLatestBuildByNameAndNumber(buildName, buildNumber);
        }
        return null;
    }

    private String parseBuildDate(String datePathParam) {
        String buildStarted = "";
        try {
            buildStarted = isNotBlank(datePathParam) ? DateUtils.formatBuildDate(Long.parseLong(datePathParam)) : "";
        } catch (Exception pe) {
            //Parse failure will just get the latest build
            log.error("Cannot parse build date from query param '{}={}': {}", PARAM_DATE, datePathParam, pe.getMessage());
            log.debug("", pe);
        }
        return buildStarted;
    }

    /**
     * Throws a 404 AbortWithHttpErrorCodeException with the given message
     *
     * @param errorMessage Message to display in the error
     */
    private void throwNotFoundError(RestResponse response, String errorMessage) {
        log.error(errorMessage);
        response.error(errorMessage);
    }

    /**
     * return not found error
     */
    private void throwInternalError(String errorMessage, RestResponse response) {
        response.error(errorMessage);
        response.responseCode(HttpServletResponse.SC_NOT_FOUND);
    }
}
