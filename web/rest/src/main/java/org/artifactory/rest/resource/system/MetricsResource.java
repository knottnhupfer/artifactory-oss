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
package org.artifactory.rest.resource.system;

import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.storage.db.util.JdbcHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Resource that provides and control various metrics.
 *
 * @author Yossi Shaul
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Path(SystemRestConstants.PATH_METRICS)
@RolesAllowed(AuthorizationService.ROLE_ADMIN)
public class MetricsResource {

    @Autowired
    private JdbcHelper jdbcHelper;

    /**
     * @return Summary report of the sql metrics.
     */
    @GET
    @Path("sql")
    @Produces({MediaType.TEXT_PLAIN})
    public Response getSqlMetrics() {
        String report = jdbcHelper.getTracer().report();
        return Response.ok().entity(report).build();
    }

    /**
     * Enables sql tracing.
     */
    @PUT
    @Path("sql/enable")
    @Produces({MediaType.TEXT_PLAIN})
    public Response enableSqlTracing() {
        jdbcHelper.getTracer().enableTracing();
        return Response.noContent().build();
    }

    /**
     * Disables sql tracing.
     */
    @PUT
    @Path("sql/disable")
    @Produces({MediaType.TEXT_PLAIN})
    public Response disableSqlTracing() {
        jdbcHelper.getTracer().disableTracing();
        return Response.noContent().build();
    }

    /**
     * Resets the sql tracing.
     */
    @PUT
    @Path("sql/reset")
    @Produces({MediaType.TEXT_PLAIN})
    public Response resetSqlTracing() {
        jdbcHelper.getTracer().resetTracing();
        return Response.noContent().build();
    }

}