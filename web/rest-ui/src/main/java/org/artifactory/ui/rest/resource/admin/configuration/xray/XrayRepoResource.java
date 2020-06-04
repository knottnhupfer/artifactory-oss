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

package org.artifactory.ui.rest.resource.admin.configuration.xray;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.model.xray.*;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.configuration.ConfigServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Path("xrayRepo")
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class XrayRepoResource extends BaseResource {

    @Autowired
    protected ConfigServiceFactory configServiceFactory;

    @GET
    @Path("getIndex")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getXrayIndexedRepo() throws Exception {
        return runService(configServiceFactory.getXrayIndexedRepo());
    }

    @POST
    @Path("setXrayEnabled")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setXrayEnabled(XrayEnabledModel xrayEnabled) throws Exception {
        return runService(configServiceFactory.setXrayEnabled(), xrayEnabled);
    }

    @GET
    @Path("getIntegrationConfig")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getXrayIntegrationConfig() throws Exception {
        return runService(configServiceFactory.GetXrayIntegrationConfig());
    }

    @POST
    @Path("setAllowWhenUnavailable")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setAllowDownloadWhenXrayUnavailable(XrayAllowWhenUnavailableModel xrayAllowWhenUnavailableModel)
            throws Exception {
        return runService(configServiceFactory.setAllowDownloadWhenXrayUnavailable(), xrayAllowWhenUnavailableModel);
    }

    @POST
    @Path("setAllowBlockedArtifactsDownload")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setAllowBlockedArtifactsDownload(XrayAllowBlockedDownloadModel xrayAllowBlockedDownloadModel)
            throws Exception {
        return runService(configServiceFactory.setAllowBlockedArtifactsDownload(), xrayAllowBlockedDownloadModel);
    }

    @POST
    @Path("setBlockUnscannedArtifactsDownloadTimeout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setBlockUnscannedArtifactsDownloadTimeout(@QueryParam("seconds") Integer seconds) {
        return runService(configServiceFactory.setBlockUnscannedArtifactsDownloadTimeout());
    }

    @GET
    @Path("getNoneIndex")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNoneXrayIndexedRepo() throws Exception {
        return runService(configServiceFactory.getNoneXrayIndexedRepo());
    }

    @POST
    @Path("addIndex")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addXrayIndexedRepo(List<XrayRepoModel> repos) throws Exception {
        return runService(configServiceFactory.addXrayIndexedRepo(), repos);
    }

    @POST
    @Path("removeIndex")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeXrayIndexedRepo(List<XrayRepoModel> repos) throws Exception {
        return runService(configServiceFactory.removeXrayIndexedRepo(), repos);
    }

    @PUT
    @Path("indexRepos")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response selectXrayIndexedRepos(List<XrayRepoModel> repos) {
        return runService(configServiceFactory.updateXrayIndexRepos(), repos);
    }

    @POST
    @Path("updateProxy")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProxy(String proxy) {
        return runService(configServiceFactory.updateXrayProxyService(), proxy);
    }

    @POST
    @Path("setBypassDefaultProxy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setBypassDefaultProxy(XrayBypassSystemDefaultProxyModel bypassDefaultProxyModel) {
        return runService(configServiceFactory.xrayBypassDefaultProxyService(), bypassDefaultProxyModel);
    }
}
