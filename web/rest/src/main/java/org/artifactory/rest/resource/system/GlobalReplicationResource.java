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

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.security.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.artifactory.api.rest.constant.SystemRestConstants.PATH_REPLICATIONS;
import static org.artifactory.api.rest.constant.SystemRestConstants.PATH_ROOT;

/**
 * @author gidis
 */
@Path(PATH_ROOT+ "/" + PATH_REPLICATIONS)
@RolesAllowed(AuthorizationService.ROLE_ADMIN)
@Component
public class GlobalReplicationResource {

    @Autowired
    AddonsManager addonsManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGlobalReplicationConfig() {
        ReplicationAddon replicationAddon = addonsManager.addonByType(ReplicationAddon.class);
        return replicationAddon.getGlobalReplicationConfig();
    }
    /**
     * Globally disable the push replication
     */
    @POST
    @Path("block")
    public Response blockPushPull(@QueryParam("push") String push,
                                  @QueryParam("pull") String pull) {
        ReplicationAddon replicationAddon = addonsManager.addonByType(ReplicationAddon.class);
        return replicationAddon.blockPushPull(push, pull);
    }

    /**
     * Globally enable the push replication
     */
    @POST
    @Path("unblock")
    public Response unblockPushPull(@QueryParam("push") String push,
                                    @QueryParam("pull") String pull) {
        ReplicationAddon replicationAddon = addonsManager.addonByType(ReplicationAddon.class);
        return replicationAddon.unblockPushPull(push, pull);
    }
}
