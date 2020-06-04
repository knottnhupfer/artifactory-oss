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

package org.artifactory.rest.resource.trash;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.constant.TrashcanRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.StatusHolder;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.rest.common.model.trash.RestoreArtifact;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.services.RepoServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(TrashcanRestConstants.ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
public class  TrashcanResource extends BaseResource {

    @Autowired
    RepoServiceFactory repoServiceFactory;

    @Autowired
    RepositoryService repositoryService;

    @POST
    @Path("empty")
    public Response empty() {
        return runService(repoServiceFactory.emptyTrash());
    }

    @POST
    @Path("restore/{path: .+}")
    public Response restoreFromTrash(@PathParam("path") String path, @QueryParam("to") String target,
                                     @QueryParam("transaction-size") Integer transactionSize) {
        RepoPath targetRepoPath = StringUtils.isNotBlank(target) ? RepoPathFactory.create(target) : RepoPathFactory.create(path);
        RepoPath sourcePath = InternalRepoPathFactory.create(TrashService.TRASH_KEY, path);

        RestoreArtifact restoreArtifact = new RestoreArtifact();
        restoreArtifact.setRepoKey(sourcePath.getRepoKey());
        restoreArtifact.setPath(sourcePath.getPath());
        restoreArtifact.setTargetRepoKey(targetRepoPath.getRepoKey());
        restoreArtifact.setTargetPath(targetRepoPath.getPath());
        restoreArtifact.setTransactionSize(transactionSize);

        return runService(repoServiceFactory.restoreArtifact(), restoreArtifact);
    }

    @DELETE
    @Path("clean/{path: .+}")
    public Response cleanTrashItem(@PathParam("path") String path) {
        RepoPath repoPath = InternalRepoPathFactory.create(TrashService.TRASH_KEY, path);
        StatusHolder statusHolder = repositoryService.undeployMultiTransaction(repoPath);
        if (statusHolder.isError()) {
            throw new BadRequestException(statusHolder.getLastError().getMessage());
        } else {
            return Response.noContent().build();
        }
    }
}
