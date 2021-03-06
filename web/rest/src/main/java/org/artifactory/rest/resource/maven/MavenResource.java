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

package org.artifactory.rest.resource.maven;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.maven.MavenMetadataService;
import org.artifactory.api.maven.MavenMetadataWorkItem;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.constant.MavenRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.repo.RepoPath;
import org.jfrog.common.StringList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.artifactory.rest.common.util.RestUtils.calcRepoPathFromRequestPath;

/**
 * A resource for manually running maven indexer
 *
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Path(MavenRestConstants.PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class MavenResource {

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private MavenMetadataService mavenMetadataService;

    @POST
    @Produces({MediaType.TEXT_PLAIN})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response runIndexer(@QueryParam(MavenRestConstants.PARAM_REPOS_TO_INDEX) StringList reposToIndex,
            @QueryParam(MavenRestConstants.PARAM_FORCE) int force) {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return restAddon.runMavenIndexer(reposToIndex, force);
    }

    @POST
    @Path("calculateMetadata/{path: .+}")
    public Response calculateMetadata(@PathParam("path") String path , @QueryParam(MavenRestConstants.PARAM_RECURSIVE) boolean noneRecursive) {
        RepoPath repoPath = calcRepoPathFromRequestPath(path);
        if (!repositoryService.exists(repoPath)) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find path '" + path + "'").build();
        }

        String repoKey = repoPath.getRepoKey();
        LocalRepoDescriptor localRepo = repositoryService.localRepoDescriptorByKey(repoKey);
        if (localRepo == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    "Unable to find local repository '" + repoKey + "'.")
                    .build();
        }

        if (!localRepo.getType().isMavenGroup()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    "Repository '" + repoKey + "' is not a maven repository.").build();
        }

        mavenMetadataService.calculateMavenMetadataAsync(new MavenMetadataWorkItem(repoPath, !noneRecursive));
        return Response.ok().build();
    }
}
