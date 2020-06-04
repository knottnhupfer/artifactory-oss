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

package org.artifactory.rest.resource.traffic;

import org.artifactory.api.rest.constant.HaRestConstants;
import org.artifactory.api.rest.constant.TrafficRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.exception.RestException;
import org.artifactory.traffic.TrafficService;
import org.artifactory.traffic.TransferUsage;
import org.jfrog.common.StringList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * @author Noam Tenne
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(TrafficRestConstants.PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, HaRestConstants.ROLE_HA})
public class TrafficResource {

    @Context
    private HttpServletResponse httpResponse;

    @Autowired
    private TrafficService trafficService;

    @GET
    @Path(TrafficRestConstants.PATH_FILTER_NODE)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, HaRestConstants.ROLE_HA})
    public TransferUsage getTransferUsageCurrentNode(
            @QueryParam(TrafficRestConstants.PARAM_START_DATE) long startLong,
            @QueryParam(TrafficRestConstants.PARAM_END_DATE) long endLong,
            @QueryParam(TrafficRestConstants.PARAM_FILTER) StringList ipsToFilter) throws IOException {
        return trafficService.getTrafficUsageWithFilterCurrentNode(startLong, endLong, ipsToFilter);
    }

    @GET
    @Path(TrafficRestConstants.PATH_FILTER)
    @Produces(MediaType.APPLICATION_JSON)
    public TransferUsage getTransferUsage(
            @QueryParam(TrafficRestConstants.PARAM_START_DATE) long startLong,
            @QueryParam(TrafficRestConstants.PARAM_END_DATE) long endLong,
            @QueryParam(TrafficRestConstants.PARAM_FILTER) StringList ipsToFilter,
            @QueryParam(TrafficRestConstants.FILTER_XRAY_USAGE) boolean filterXrayUsage) throws IOException {
        TransferUsage transferUsage = trafficService.getTrafficUsageWithFilter(startLong, endLong, ipsToFilter);
        if (transferUsage == null) {
            throw new RestException("HA member is null or inactive and therefore propagate traffic collector is not allowed");
        }
        return transferUsage;
    }
}