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

package org.artifactory.rest.resource.distribution;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.bintray.distribution.Distribution;
import org.artifactory.api.bintray.distribution.Distributor;
import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.model.distribution.DistributionResponseBuilder;
import org.artifactory.rest.common.util.BintrayRestHelper;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;

/**
 * This endpoint provides all distribution actions separated by package type being pushed.
 * Each endpoint relies on the specific package metadata and runs only the relevant rules in the repo.
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path("distribute")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class DistributionResource {
    private static final Logger log = LoggerFactory.getLogger(DistributionResource.class);

    @Autowired
    private Distributor distributor;

    /**
     * Distributes a set of paths to Bintray using the params specified in {@param distribution} and the dist repo's
     * rule set. if {@param gpgPassphrase} was passed it will be used as an override and the each created version will
     * be signed with it.
     *
     * @return Status of the operation
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response distribute(@QueryParam("gpgPassphrase") @Nullable String gpgPassphrase, Distribution distribution)
            throws IOException {
        DistributionReporter status = new DistributionReporter(!distribution.isDryRun());
        validateParams(distribution, status);
        String opPerformedOn = "the requested artifacts";
        String response;
        if (status.isError()) {
            response = DistributionResponseBuilder.writeResponseBody(status, opPerformedOn, distribution.isAsync(),
                    distribution.isDryRun());
        } else {
            if (StringUtils.isNotEmpty(gpgPassphrase)) {
                distribution.setGpgPassphrase(gpgPassphrase);
            }
            status = distributor.distribute(distribution);
            response = DistributionResponseBuilder.writeResponseBody(status, opPerformedOn, distribution.isAsync(),
                    distribution.isDryRun());
        }
        return Response.status(DistributionResponseBuilder.getResponseCode(status)).entity(response)
                .type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    private void validateParams(Distribution distribution, DistributionReporter status) {
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        if (!BintrayRestHelper.isPushToBintrayAllowed(statusHolder, distribution.getTargetRepo())) {
            status.error(distribution.getTargetRepo(), "You do not have distribute and deploy permissions.", SC_FORBIDDEN, log);
        } else if (CollectionUtils.notNullOrEmpty(distribution.getSourceRepos())) {
            status.error("Source repositories filtration is only available for build distribution.", SC_BAD_REQUEST, log);
        }
    }
}
