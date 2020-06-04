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

package org.artifactory.ui.rest.resource.admin.security.sshserver;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.model.admin.security.sshserver.SshServer;
import org.artifactory.ui.rest.model.utils.FileUpload;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Noam Y. Tenne
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("sshserver")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SshServerResource extends BaseResource {
    @Autowired
    private SecurityServiceFactory securityFactory;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSshServer(SshServer sshServer)
            throws Exception {
        return runService(securityFactory.updateSshServer(), sshServer);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSshServer()
            throws Exception {
        return runService(securityFactory.getSshServer());
    }

    @POST
    @Path("install")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadSshKey(FormDataMultiPart formParams) throws Exception {
        FileUpload fileUpload = new FileUpload(formParams);
        return runService(securityFactory.uploadSshServerKey(), fileUpload);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeSigningKey()
            throws Exception {
        return runService(securityFactory.removeSshServerKeyService());
    }
}
