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

package org.artifactory.ui.rest.resource.builds;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.builds.BuildLicenseModel;
import org.artifactory.ui.rest.model.builds.BuildNumbersSearchModel;
import org.artifactory.ui.rest.model.builds.DeleteBuildsModel;
import org.artifactory.ui.rest.service.builds.BuildsServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author Chen Keinan
 */
@Path("builds")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BuildsResource extends BaseResource {

    @Autowired
    private BuildsServiceFactory buildsFactory;

    @Autowired
    @Qualifier("streamingRestResponse")
    public void setArtifactoryResponse(RestResponse artifactoryResponse) {
        this.artifactoryResponse = artifactoryResponse;
    }

    // Main Builds page

    /**
     * Gets the list of builds for the main Builds page.
     * Filtering is done according to user's permissions
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllBuilds() {
        return runService(buildsFactory.getAllBuilds());
    }

    /**
     * Gets all (unique) build names, used by the build permissions modal.
     */
    @GET
    @Path("names")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllBuildNames() {
        return runService(buildsFactory.getAllBuildNames());
    }

    /**
     * Deletes all builds under one build name according to user's permissions
     */
    @POST
    @Path("deleteAllBuilds")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllBuild(DeleteBuildsModel deleteBuildsModel) {
        return runService(buildsFactory.deleteAllBuilds(), deleteBuildsModel);
    }

    /**
     * Deletes specific build (build ID under build name) according to user's permissions
     */
    @POST
    @Path("buildsDelete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBuild(DeleteBuildsModel deleteBuildsModel) {
        return runService(buildsFactory.deleteBuild(), deleteBuildsModel);
    }

    // Specific build page

    /**
     * Gets the list of build numbers (IDs) in a specific build page
     */
    @GET
    @Path("history{name:(/[^/]+?)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildHistory () {
        return runService(buildsFactory.getBuildHistory());
    }

    /**
     * Gets all the previous builds of a build in a specific build page (used for the compare drop-down)
     */
    @GET
    @Path("prevBuild/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPrevBuild() {
        return runService(buildsFactory.getPrevBuildList());
    }

    /**
     * Specific build page - top bar data
     * Also returns isBuildFullView field that determines whether the tabs will be displayed or not
     * Also returns canDelete field that determines whether the user will be displayed the option to delete build
     */
    @GET
    @Path("buildInfo/{name}/{number}{date:(/[^/]+?)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildGeneralInfo() {
        return runService(buildsFactory.getBuildGeneralInfo());
    }

    // Published Modules page

    @GET
    @Path("publishedModules/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublishedModules() {
        return runService(buildsFactory.getPublishedModules());
    }

    /**
     * Published Modules page
     * specific artifact sub-page
     */
    @GET
    @Path("modulesArtifact/{name}/{number}/{date}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getModulesArtifact() {
        return runService(buildsFactory.getModuleArtifacts());
    }

    /**
     * Published Modules page
     * specific artifact sub-page
     */
    @GET
    @Path("modulesDependency/{name}/{number}/{date}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getModulesDependency() {
        return runService(buildsFactory.getModuleDependency());
    }

    /**
     * Published Modules page
     * specific artifact sub-page for diff
     */
    @GET
    @Path("artifactDiff/{name}/{number}/{date}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response artifactDiff() {
        return runService(buildsFactory.diffBuildModuleArtifact());
    }

    /**
     * Published Modules page
     * specific artifact sub-page for diff
     */
    @GET
    @Path("dependencyDiff/{name}/{number}/{date}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response dependencyDiff() {
        return runService(buildsFactory.diffBuildModuleDependency());
    }

    // Environment page

    /**
     * Environment page
     * Environment Variables section
     */
    @GET
    @Path("buildProps/env/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEnvBuildProps() {
        return runService(buildsFactory.getEnvBuildProps());
    }

    /**
     * Environment page
     * System Variables section
     */
    @GET
    @Path("buildProps/system/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSystemBuildProps() {
        return runService(buildsFactory.getSystemBuildProps());
    }

    // Issues page

    @GET
    @Path("buildIssues/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildIssues() {
        return runService(buildsFactory.getBuildIssues());
    }

    // Licenses page

    @GET
    @Path("buildLicenses/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildLicense() {
        return runService(buildsFactory.buildLicenses());
    }

    @POST
    @Path("exportLicenses")
    @Consumes("application/x-www-form-urlencoded")
    public Response exportLicense(@FormParam("data") String data) throws IOException {
        BuildLicenseModel buildLicenseModel = JsonUtil.mapDataToModel(data, BuildLicenseModel.class);
        return runService(buildsFactory.exportLicenseToCsv(), buildLicenseModel);
    }

    @PUT
    @Path("overrideLicenses/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response overrideLicense(BuildLicenseModel buildLicenseModel) {
        return runService(buildsFactory.overrideSelectedLicenses(), buildLicenseModel);
    }

    @GET
    @Path("changeLicenses/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChangeLicenseValues() {
        return runService(buildsFactory.changeBuildLicense());
    }

    // Diff page

    @GET
    @Path("buildDiff/{name}/{number}/{date}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buildDiff() {
        return runService(buildsFactory.buildDiff());
    }

    // Release History page

    @GET
    @Path("releaseHistory/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildReleaseHistory() {
        return runService(buildsFactory.buildReleaseHistory());
    }

    // Build JSON page

    @GET
    @Path("buildJson/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildJson() {
        return runService(buildsFactory.getBuildJson());
    }

    // Effective permission page

    @GET
    @Path("buildEffectivePermission/{name}/{number}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildEffectivePermission() {
        return runService(buildsFactory.getBuildEffectivePermission());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("buildEffectivePermissionByEntity/users/{username}")
    public Response getEffectivePermissionsByUser() {
        return runService(buildsFactory.getBuildEffectivePermissionByEntity());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("buildEffectivePermissionByEntity/groups/{groupname}")
    public Response getEffectivePermissionsByGroup() {
        return runService(buildsFactory.getBuildEffectivePermissionByEntity());
    }

    /**
     * Gets the one and only BuildInfo repository key
     */
    @GET
    @Path("repository")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildInfoRepoKey() {
        return runService(buildsFactory.getBuildInfoRepoKey());
    }

    // Unified UI search

    /**
     * Gets the list of builds that their name contains the search term.
     * Filtered according to user's permissions.
     * Limited number of results by min(limit_parameter, search default).
     * Sorted by sort_parameter or date by default, direction is direction_parameter or desc by default.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    public Response searchBuildsByName() {
        return runService(buildsFactory.searchBuildsByName());
    }

    /**
     * Gets the list of build numbers that their name exactly matches the search term.
     * Filtered according to user's permissions.
     * Limited number of results by min(limit_parameter, search default).
     * Sorted by sort_parameter or date by default, direction is direction_parameter or desc by default.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search/{buildName}")
    public Response searchBuildVersions() {
        return runService(buildsFactory.searchBuildVersions());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("search/{buildName}")
    public Response searchBuildVersionsByVersionsList(BuildNumbersSearchModel buildNumbersSearchModel) {
        return runService(buildsFactory.searchBuildVersionByVersionsList(), buildNumbersSearchModel);
    }
}
