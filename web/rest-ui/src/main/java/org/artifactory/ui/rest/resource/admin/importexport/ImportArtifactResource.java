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

package org.artifactory.ui.rest.resource.admin.importexport;


import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.model.admin.importexport.ImportExportSettings;
import org.artifactory.ui.rest.model.utils.FileUpload;
import org.artifactory.ui.rest.service.admin.importexport.ImportExportServiceFactory;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author chen keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("artifactimport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ImportArtifactResource extends BaseResource {

    @Autowired
    protected ImportExportServiceFactory importExportFactory;

    @POST
    @Path("repository")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importRepository(ImportExportSettings importExportSettings) {
        return runService(importExportFactory.importRepositoryService(), importExportSettings);
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadExtractedZip(FormDataMultiPart formParams) {
        FileUpload fileUpload = new FileUpload(formParams);
        return runService(importExportFactory.uploadExtractedZip(), fileUpload);
    }

    @POST
    @Path("system")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importSystem(ImportExportSettings systemImport) {
        return runService(importExportFactory.importSystem(), systemImport);
    }

    @POST
    @Path("systemUpload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response systemExtractedZip(FormDataMultiPart formParams) {
        FileUpload fileUpload = new FileUpload(formParams);
        return runService(importExportFactory.uploadSystemExtractedZip(), fileUpload);
    }

}
