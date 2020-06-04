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

package org.artifactory.ui.rest.resource.home;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.service.home.HomePageServiceFactory;
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
 * @author Chen keinan
 */
@Path("home/widget")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HomeResource extends BaseResource {

    @Autowired
    private HomePageServiceFactory homePageFactory;

    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGeneralHomeInfo() {
        return runService(homePageFactory.getGeneralInfoWidget());
    }

    @GET
    @Path("artifactCount")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArtifactsCountWidget() {
        return runService(homePageFactory.artifactCountWidget());
    }

    @GET
    @Path("addon")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAddonsInfo() {
        return runService(homePageFactory.getAddonsWidget());
    }

    @GET
    @Path("latestBuilds")
    @Produces(MediaType.APPLICATION_JSON)
    public Response latestBuildsWidget() {
        return runService(homePageFactory.getLatestBuildsWidget());
    }

    @GET
    @Path("mostDownloaded")
    @Produces(MediaType.APPLICATION_JSON)
    public Response mostDownloadedWidget() {
        return runService(homePageFactory.getMostDownloadedWidget());
    }
}
