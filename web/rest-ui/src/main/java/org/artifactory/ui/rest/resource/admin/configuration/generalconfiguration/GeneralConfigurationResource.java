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

package org.artifactory.ui.rest.resource.admin.configuration.generalconfiguration;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.admin.configuration.generalconfig.ArtifactoryConfig;
import org.artifactory.ui.rest.model.admin.configuration.generalconfig.GeneralConfig;
import org.artifactory.ui.rest.model.admin.configuration.generalconfig.PlatformConfig;
import org.artifactory.ui.rest.model.utils.FileUpload;
import org.artifactory.ui.rest.service.admin.configuration.ConfigServiceFactory;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Path("generalConfig")
@RolesAllowed(AuthorizationService.ROLE_ADMIN)
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GeneralConfigurationResource extends BaseResource {

    @Autowired
    protected ConfigServiceFactory configServiceFactory;

    @Autowired
    @Qualifier("streamingRestResponse")
    public void setArtifactoryResponse(RestResponse artifactoryResponse) {
        this.artifactoryResponse = artifactoryResponse;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGeneralConfig() {
        return runService(configServiceFactory.getGeneralConfig());
    }

    @Path("platform")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlatformConfig() {
        return runService(configServiceFactory.getPlatformConfig());
    }

    @Path("artifactory")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArtifactoryConfig() {
        return runService(configServiceFactory.getArtifactoryConfig());
    }

    @GET
    @Path("data")
    @RolesAllowed({AuthorizationService.ROLE_ADMIN,AuthorizationService.ROLE_USER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGeneralConfigData() {
        return runService(configServiceFactory.getGeneralConfigData());
    }

    @POST
    @Path("logo")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadLogo(FormDataMultiPart formDataMultiPart) {
        FileUpload fileUpload = new FileUpload(formDataMultiPart);
        return runService(configServiceFactory.uploadLogo(), fileUpload);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveGeneralConfig(GeneralConfig generalConfig) {
        return runService(configServiceFactory.updateGeneralConfig(), generalConfig);
    }

    @Path("platform")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response savePlatformConfig(PlatformConfig platformConfig) {
        return runService(configServiceFactory.updatePlatformConfig(), platformConfig);
    }

    @Path("artifactory")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveArtifactoryConfig(ArtifactoryConfig artifactoryConfig) {
        return runService(configServiceFactory.updateArtifactoryConfig(), artifactoryConfig);
    }



    @GET
    @Produces("image/*")
    @Path("logo")
    @RolesAllowed({AuthorizationService.ROLE_ADMIN,AuthorizationService.ROLE_USER})
    public Response getUploadLogo() {
        return runService(configServiceFactory.getUploadLogo());
    }

    @DELETE
    @Path("logo")
    public Response deleteUploadLogo() {
        return runService(configServiceFactory.deleteUploadedLogo());
    }
}
