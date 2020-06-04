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

package org.artifactory.ui.rest.resource.artifacts.setmeup;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.setmeup.GradleSettingModel;
import org.artifactory.ui.rest.model.setmeup.IvySettingModel;
import org.artifactory.ui.rest.model.setmeup.MavenSettingModel;
import org.artifactory.ui.rest.service.general.GeneralServiceFactory;
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
@Path("setMeUp")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SetMeUpResource extends BaseResource {

    @Autowired
    @Qualifier("streamingRestResponse")
    public void setArtifactoryResponse(RestResponse artifactoryResponse) {
        this.artifactoryResponse = artifactoryResponse;
    }

    @Autowired
    GeneralServiceFactory generalFactory;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRepoKeyTypeForSetMeUp()
            throws Exception {
        return runService(generalFactory.getSetMeUp());
    }

    @GET
    @Path("mavenSettings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMavenSettings()
            throws Exception {
        return runService(generalFactory.mavenSettingGenerator());
    }

    @GET
    @Path("gradleSettings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGradleSettings()
            throws Exception {
        return runService(generalFactory.gradleSettingGenerator());
    }

    @GET
    @Path("ivySettings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIvySettings()
            throws Exception {
        return runService(generalFactory.ivySettingGenerator());
    }

    @GET
    @Path("reverseProxyData")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReverseProxyData()
            throws Exception {
        return runService(generalFactory.getReverseProxySetMeUpData());
    }

    @POST
    @Path("mavenSnippet")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateMavenSnippet(MavenSettingModel mavenSettingModel)
    throws Exception {
        return runService(generalFactory.getMavenSettingSnippet(),mavenSettingModel);
    }

    @POST
    @Path("gradleSnippet")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateGradleSnippet(GradleSettingModel gradleSettingModel)
            throws Exception {
        return runService(generalFactory.getGradleSettingSnippet(),gradleSettingModel);
    }

    @POST
    @Path("ivySnippet")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateIvySnippet(IvySettingModel ivySettingModel)
            throws Exception {
        return runService(generalFactory.GetIvySettingSnippet(), ivySettingModel);
    }

    @GET
    @Path("downloadBuildGradle")
    @Produces(MediaType.TEXT_PLAIN)
    public Response downloadGradleProperties(@QueryParam("data") String data)
            throws Exception {
        GradleSettingModel gradleSettingModel = (GradleSettingModel) JsonUtil.mapDataToModel(data,
                GradleSettingModel.class);
        return runService(generalFactory.getGradleSettingSnippet(), gradleSettingModel);
    }

    @GET
    @Path("downloadBuildMaven")
    @Produces(MediaType.APPLICATION_XML)
    public Response downloadMavenSnippet(@QueryParam("data") String data)
            throws Exception {
        MavenSettingModel mavenSettingModel = (MavenSettingModel) JsonUtil.mapDataToModel(data,
                MavenSettingModel.class);
        return runService(generalFactory.getMavenSettingSnippet(), mavenSettingModel);
    }

    @GET
    @Path("downloadBuildIvy")
    @Produces(MediaType.APPLICATION_XML)
    public Response generateIvySnippet(@QueryParam("data") String data)
            throws Exception {
        IvySettingModel ivySettingModel = (IvySettingModel) JsonUtil.mapDataToModel(data,
                IvySettingModel.class);
        return runService(generalFactory.GetIvySettingSnippet(), ivySettingModel);
    }

    @GET
    @Path("mavenDistributionManagement")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mavenDistributionManagement()
            throws Exception {
        return runService(generalFactory.getMavenDistributionMgnt());
    }
}
