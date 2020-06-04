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

package org.artifactory.ui.rest.resource.artifacts.packages;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.NativeSearchServiceFactory;
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
 * @author ortalh
 */
@Path("v1/native/packages")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PackageNativeResource extends BaseResource {

    @Autowired
    private NativeSearchServiceFactory nativeSearchServiceFactory;

    @POST
    @Path("{type}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response nativePackageSearch(@PathParam("type") String type,
            @QueryParam("order") String order,
            @QueryParam("sort_by") String sortBy,
            List<AqlUISearchModel> searchModels) {
        if (RepoType.Docker.getType().equals(type)) {
            return runService(nativeSearchServiceFactory.packageNativeDockerSearchService(), searchModels);
        } else {
            return runService(nativeSearchServiceFactory.packageNativeSearchService(), searchModels);
        }
    }

    @POST
    @Path("{type}/extra_info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response packageNativeExtraInfo(List<AqlUISearchModel> search) {
        return runService(nativeSearchServiceFactory.packageNativeExtraInfoService(), search);
    }

    @POST
    @Path("{type}/summary")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response packageNativeSummary(List<AqlUISearchModel> searchModels) {
        return runService(nativeSearchServiceFactory.packageNativeSummaryService(), searchModels);
    }

    @POST
    @Path("{type}/summary/extra_info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response packageNativeSummaryExtraInfo(@QueryParam("path") String path, List<AqlUISearchModel> searchModels) {
        return runService(nativeSearchServiceFactory.packageNativeSummaryExtraInfoService(), searchModels);
    }

    @GET
    @Path("show_extra_info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDbType() {
        return runService(nativeSearchServiceFactory.getDbTypeService());
    }

    @POST
    @Path("{type}/count_packages")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response packageNativeCountPackages(List<AqlUISearchModel> search) {
        return runService(nativeSearchServiceFactory.countNativePackagesService(), search);
    }

    /////// Docker Only ///////
    @GET
    @Path("docker/total_downloads")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response packageNativeDockerTotalDownload(@QueryParam("packageName") String packageName) {
        return runService(nativeSearchServiceFactory.nativeDockerTotalDownloadSearchService());

    }

    @GET
    @Path("docker/extra_info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response packageNativeDockerExtraInfo() {
        return runService(nativeSearchServiceFactory.packageNativeDockerExtraInfoService());
    }
}