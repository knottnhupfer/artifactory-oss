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

package org.artifactory.ui.rest.resource.artifacts.versions;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.browse.BrowseServiceFactory;
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
@Path("v1/native/versions")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VersionNativeResource extends BaseResource {

    @Autowired
    private BrowseServiceFactory browseServiceFactory;
    @Autowired
    private NativeSearchServiceFactory nativeSearchServiceFactory;

    @POST
    @Path("{type}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response nativeVersionSearch(@PathParam("type") String type, @QueryParam("packageName") String packageName,
            @QueryParam("order") String order,
            @QueryParam("sort_by") String sortBy,
            @QueryParam("limit") String limit,
            @QueryParam("with_xray") String withXray,
            List<AqlUISearchModel> searchModels) {
        if (RepoType.Docker.getType().equals(type)) {
            return runService(nativeSearchServiceFactory.versionNativeDockerListSearchService(), searchModels);
        } else {
            return runService(nativeSearchServiceFactory.versionNativeListSearchService(), searchModels);
        }
    }

    @POST
    @Path("{type}/summary")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response versionNativeSummary(List<AqlUISearchModel> search) {
        return runService(nativeSearchServiceFactory.versionNativeSummaryService(), search);
    }

    @POST
    @Path("{type}/summary/extra_info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response versionNativeSummaryExtraInfo(@QueryParam("path") String path, List<AqlUISearchModel> search) {
        return runService(nativeSearchServiceFactory.versionNativeSummaryExtraInfoService(), search);
    }

    @GET
    @Path("/{type}/builds")
    @Produces(MediaType.APPLICATION_JSON)
    public Response versionNativeBuilds(@QueryParam("path") String path) {
        return runService(nativeSearchServiceFactory.versionNativeGetBuildsService());
    }

    @GET
    @Path("/{type}/props")
    @Produces(MediaType.APPLICATION_JSON)
    public Response versionNativeProps(@QueryParam("path") String path) {
        return runService(nativeSearchServiceFactory.versionNativeGetPropsService());
    }

    @GET
    @Path("/{type}/dependencies")
    @Produces(MediaType.APPLICATION_JSON)
    public Response versionNativeDependencies(@PathParam("type") String type,
            @QueryParam("path") String path) {
        return runService(nativeSearchServiceFactory.versionNativeDependenciesService());
    }

    @GET
    @Path("/{type}/readme")
    @Produces(MediaType.APPLICATION_JSON)
    public Response versionNativeReadme(@PathParam("type") String type,
            @QueryParam("path") String path) {
        return runService(nativeSearchServiceFactory.versionNativeReadmeService());
    }

    @POST
    @Path("{type}/extra_info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response versionNativeExtraInfo(List<AqlUISearchModel> search) {
        return runService(nativeSearchServiceFactory.versionNativeExtraInfoService(), search);
    }


    /////// Docker Only ////////
    @POST
    @Path("docker/extra_info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response dockerNativeExtraInfo(List<AqlUISearchModel> search) {
        return runService(nativeSearchServiceFactory.packageNativeDockerExtraInfoService(), search);
    }

    @GET
    @Path("/docker/{repoKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response nativeVersionManifestSearch() {
        return runService(browseServiceFactory.dockerV2ViewNativeService());
    }

    @GET
    @Path("docker/total_downloads/{repoKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response nativeVersionDownloads(@PathParam("repoKey") String repoKey,
            @QueryParam("packageName") String packageName,
            @QueryParam("versionName") String versionName) {
        return runService(nativeSearchServiceFactory.nativeDockerTotalDownloadSearchService());
    }
}
