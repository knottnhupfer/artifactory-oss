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

package org.artifactory.rest.filter;


import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.rest.constant.HaRestConstants;
import org.artifactory.rest.common.exception.RestException;
import org.artifactory.webapp.servlet.HttpArtifactoryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.net.URI;

import static org.artifactory.api.rest.constant.SystemRestConstants.*;

/**
 * @author Gidi Shabat
 */
public class LicenseRestFilter implements ContainerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(LicenseRestFilter.class);

    @Context
    HttpServletResponse response;

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        ArtifactoryResponse artifactoryResponse = new HttpArtifactoryResponse(response);
        try {
            String path = containerRequest.getUriInfo().getPath();
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            if (isUnlicensedHa(addonsManager) && isAnyActiveMemberLicensed(addonsManager) &&
                    !isRequestAllowedWithoutLicenseInstalled(containerRequest)) {
                log.warn("License is not installed");
                throw new RestException(HttpStatus.SC_SERVICE_UNAVAILABLE, "License expired or not installed");
            }

            try {
                // Filter out all events in case that the trial license expired
                addonsManager.interceptRestResponse(artifactoryResponse, path);
            } catch (HttpResponseException e) {
                // throw the exception to make sure to return
                throw new RestException(e.getStatusCode(), e.getMessage());
            }

        } catch (IOException e) {
            log.error("Fail to intercept license REST validation", e);
            throw new RuntimeException("Fail to intercept license validation during rest request", e);
        }
    }

    private boolean isUnlicensedHa(AddonsManager addonsManager) {
        return !addonsManager.isLicenseInstalled() && addonsManager.getArtifactoryRunningMode().isHa();
    }

    private boolean isAnyActiveMemberLicensed(AddonsManager addonsManager) {
        return addonsManager.getClusterLicensesDetails()
                .stream()
                .filter(licenseDetail -> !"OSS".equalsIgnoreCase(licenseDetail.getType()))
                .filter(licenseDetail -> !"Community Edition for C/C++".equalsIgnoreCase(licenseDetail.getType()))
                .anyMatch(licenseDetail -> !"Not in use".equals(licenseDetail.getNodeId()));
    }

    private boolean isRequestAllowedWithoutLicenseInstalled(ContainerRequestContext request) {
        // Only propagation requests (used internally between the nodes), mission control requests or license requests are allowed when the node has no license
        if (request.getHeaderString(HaRestConstants.ARTIFACTORY_HA_SECURITY_TOKEN) != null) {
            return true;
        }

        String path = request.getUriInfo().getPath();
        if (path != null && (path.startsWith(PATH_ROOT+"/"+PATH_LICENSE) || path.startsWith(PATH_ROOT+"/"+PATH_NEW_LICENSES))) {
            return true;
        }

        URI baseUri = request.getUriInfo().getBaseUri();
        return baseUri != null && baseUri.getPath() != null && baseUri.getPath().endsWith("/mc/");
    }
}
