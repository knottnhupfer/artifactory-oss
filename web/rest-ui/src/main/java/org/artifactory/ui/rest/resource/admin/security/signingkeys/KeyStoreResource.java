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

package org.artifactory.ui.rest.resource.admin.security.signingkeys;


import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.model.admin.security.signingkey.KeyStore;
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
 * @author Chen Keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("keystore")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class KeyStoreResource extends BaseResource {

    @Autowired
    private SecurityServiceFactory securityFactory;

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadKeyStore(FormDataMultiPart formParams)
            throws Exception {
        FileUpload fileUpload = new FileUpload(formParams);
        return runService(securityFactory.addKeyStore(), fileUpload);
    }

    @PUT
    @Path("updatePass")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateKeyStorePass(KeyStore keyStore)
            throws Exception {
        return runService(securityFactory.changeKeyStorePassword(), keyStore);
    }

    @POST
    @Path("add")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addKeyStorePass(KeyStore keyStore)
            throws Exception {
        return runService(securityFactory.saveKeyStore(), keyStore);
    }

    @POST
    @Path("cancel")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cancelKeyStorePass(KeyStore keyStore)
            throws Exception {
        return runService(securityFactory.cancelKeyPair(), keyStore);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeKeyStore()
            throws Exception {
        return runService(securityFactory.removeKeyStore());
    }

    @DELETE
    @Path("password")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeKeyStorePassword() {
        return runService(securityFactory.removeKeystorePassword());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKeyStore()
            throws Exception {
        return runService(securityFactory.getKeyStore());
    }
}
