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

package org.artifactory.ui.rest.resource.artifacts.search;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.model.admin.importexport.ImportExportSettings;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearchResult;
import org.artifactory.ui.rest.service.artifacts.search.SearchServiceFactory;
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
 * @author Chen keinan
 */
@Path("stashResults")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class StashSearchResultsResource extends BaseResource {

    @Autowired
    SearchServiceFactory searchFactory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveSearchResults(List<BaseSearchResult> baseSearchResults)
            throws Exception {
        return runService(searchFactory.saveSearchResults(), baseSearchResults);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSearchResults()
            throws Exception {
        return runService(searchFactory.getSearchResults());
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSearchResults()
            throws Exception {
        return runService(searchFactory.removeSearchResults());
    }

    @POST
    @Path("subtract")
    @Produces(MediaType.APPLICATION_JSON)
    public Response subtractSearchResults(List<BaseSearchResult> baseSearchResults)
            throws Exception {
        return runService(searchFactory.subtractSearchResults(), baseSearchResults);
    }

    @POST
    @Path("intersect")
    @Produces(MediaType.APPLICATION_JSON)
    public Response intersectSearchResults(List<BaseSearchResult> baseSearchResults)
            throws Exception {
        return runService(searchFactory.intersectSearchResults(), baseSearchResults);
    }

    @POST
    @Path("add")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSearchResults(List<BaseSearchResult> baseSearchResults)
            throws Exception {
        return runService(searchFactory.addSearchResults(), baseSearchResults);
    }

    @POST
    @Path("export")
    @Produces(MediaType.APPLICATION_JSON)
    public Response exportSearchResults(ImportExportSettings importExportSettings)
            throws Exception {
        return runService(searchFactory.exportSearchResults(), importExportSettings);
    }

    @POST
    @Path("copy")
    @Produces(MediaType.APPLICATION_JSON)
    public Response copySearchResults()
            throws Exception {
        return runService(searchFactory.copySearchResults());
    }

    @POST
    @Path("move")
    @Produces(MediaType.APPLICATION_JSON)
    public Response moveSearchResults()
            throws Exception {
        return runService(searchFactory.moveSearchResults());
    }

    @POST
    @Path("discard")
    @Produces(MediaType.APPLICATION_JSON)
    public Response discardResults()
            throws Exception {
        return runService(searchFactory.discardResults());
    }

}
