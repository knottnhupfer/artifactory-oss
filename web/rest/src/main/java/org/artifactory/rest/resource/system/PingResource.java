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

import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.security.access.AccessService;
import org.artifactory.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Resource to get information about Artifactory current state.
 * The method will:
 * <ul><li>Check that DB behaves correctly (do an actual DB retrieve)</li>
 * <li>Check that the logger behaves correctly (read and/or write to log)</li></ul>
 * Upon results return:
 * <ul><li>Code 200 with text exactly equal to "OK" if all is good</li>
 * <li>Code 500 with failure description in the text if something wrong</li></ul>
 *
 * @author Fred Simon
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Path(SystemRestConstants.PATH_ROOT + "/" + SystemRestConstants.PATH_PING)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class PingResource {

    private static final Logger log = LoggerFactory.getLogger(PingResource.class);

    @Autowired
    private AddonsManager addonsManager;

    /**
     * @return The artifactory state "OK" or "Failure"
     */
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public Response pingArtifactory() {
        try {
            log.debug("Received ping call");
            if ((addonsManager.getArtifactoryRunningMode().isHa() && addonsManager.lockdown())) {
                log.error("Ping failed since the server state is blocked");
                return Response.status(HttpStatus.SC_SERVICE_UNAVAILABLE).entity("Server state is offline").build();
            }
            if (ContextHelper.get().isOffline()) {
                log.error("Ping failed since the server state is offline");
                return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Server state is offline").build();
            }
            ArtifactoryHome artifactoryHome = ArtifactoryHome.get();
            if (!artifactoryHome.getDataDir().canWrite() || !artifactoryHome.getLogDir().canWrite()) {
                log.error("Ping failed due to file system access to data or log dir failed");
                return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("File system access failed").build();
            }
            ContextHelper.get().beanForType(StorageService.class).ping();
            log.debug("Performing ping request to Access");
            ContextHelper.get().beanForType(AccessService.class).ping();
        } catch (Exception e) {
            log.error("Error during ping test", e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        log.debug("Ping successful");
        return Response.ok().entity("OK").build();
    }

}
