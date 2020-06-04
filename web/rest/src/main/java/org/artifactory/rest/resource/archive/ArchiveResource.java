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

package org.artifactory.rest.resource.archive;


import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.rest.RestAddon;
import org.jfrog.common.archive.ArchiveType;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.download.FolderDownloadResult;
import org.artifactory.api.rest.build.artifacts.BuildArtifactsRequest;
import org.artifactory.api.rest.constant.ArchiveRestConstants;
import org.artifactory.api.rest.constant.BuildRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.download.FolderDownloadException;
import org.artifactory.mime.MimeType;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.exception.UnauthorizedException;
import org.artifactory.rest.resource.ci.BuildResource;
import org.glassfish.jersey.server.CloseableService;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Resource class which handles archive operations
 *
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(ArchiveRestConstants.PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_USER, AuthorizationService.ROLE_ADMIN})
public class ArchiveResource {
    private static final Logger log = LoggerFactory.getLogger(ArchiveResource.class);

    @Context
    private CloseableService closeableService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private CentralConfigService centralConfig;

    @Autowired
    private AddonsManager addonsManager;

    @POST
    @Path(ArchiveRestConstants.PATH_BUILD_ARTIFACTS)
    @Consumes({BuildRestConstants.MT_BUILD_ARTIFACTS_REQUEST, MediaType.APPLICATION_JSON})
    public Response getBuildArtifactsArchive(BuildArtifactsRequest buildArtifactsRequest) throws IOException {
        if (isBlank(buildArtifactsRequest.getBuildName())) {
            throw new BadRequestException("Cannot search without build name.");
        }
        boolean buildNumberIsBlank = isBlank(buildArtifactsRequest.getBuildNumber());
        boolean buildStatusIsBlank = isBlank(buildArtifactsRequest.getBuildStatus());
        if (buildNumberIsBlank && buildStatusIsBlank) {
            throw new BadRequestException("Cannot search without build number or build status.");
        }
        if (!buildNumberIsBlank && !buildStatusIsBlank) {
            throw new BadRequestException("Cannot search with both build number and build status parameters, " +
                    "please omit build number if your are looking for latest build by status " +
                    "or omit build status to search for specific build version.");
        }

        if (buildArtifactsRequest.getArchiveType() == null) {
            throw new BadRequestException("Archive type cannot be empty, please provide a type of zip/tar/tar.gz/tgz.");
        }

        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        try {
            final File buildArtifactsArchive = restAddon.getBuildArtifactsArchive(buildArtifactsRequest);
            if (buildArtifactsArchive == null) {
                throw new NotFoundException(
                        String.format("Could not find any build artifacts for build '%s' number '%s'.",
                                buildArtifactsRequest.getBuildName(),
                                buildArtifactsRequest.getBuildNumber()));
            }

            markForDeletionAtResponseEnd(buildArtifactsArchive);

            MimeType mimeType = NamingUtils.getMimeType(buildArtifactsArchive.getName());
            return Response.ok().entity(buildArtifactsArchive).type(mimeType.getType()).build();
        } catch (IOException e) {
            log.error("Failed to create builds artifacts archive: " + e.getMessage(), e);
            throw new NotFoundException("Failed to create builds artifacts archive");
        }
    }

    @GET
    @Path("download/{repoKey}/{path: .+}")
    public Response downloadFolder(@PathParam("repoKey") String repoKey, @PathParam("path") String path,
            @QueryParam("archiveType") String archiveTypeString,
            @QueryParam("includeChecksumFiles") Boolean includeChecksumFiles) throws Exception {
        return downloadFolderOrRepo(repoKey, path, archiveTypeString, includeChecksumFiles);
    }

    @GET
    @Path("download/{repoKey}")
    public Response downloadFolderOrRepo(@PathParam("repoKey") String repoKey,
            @QueryParam("archiveType") String archiveTypeString,
            @QueryParam("includeChecksumFiles") Boolean includeChecksumFiles) throws Exception {
        return downloadFolderOrRepo(repoKey, "", archiveTypeString, includeChecksumFiles);
    }

    private Response downloadFolderOrRepo(String repoKey, String path, String archiveTypeString, Boolean includeChecksumFiles) throws Exception {
        if (authorizationService.isAnonymous() && !centralConfig.getDescriptor().getFolderDownloadConfig().isEnabledForAnonymous()) {
            throw new UnauthorizedException("You must be logged in to download a folder or repository.");
        }
        RepoPath pathToDownload = RepoPathFactory.create(repoKey, path);
        ArchiveType archiveType;
        try {
            archiveType = ArchiveType.fromValue(archiveTypeString);
        } catch (IllegalArgumentException iae) {
            log.debug("", iae);
            throw new BadRequestException(iae.getMessage());
        }
        boolean includeChecksums = includeChecksumFiles == null ? false : includeChecksumFiles;
        Response.ResponseBuilder response;
        try {
            FolderDownloadResult result = addonsManager.addonByType(RestAddon.class)
                    .downloadFolderOrRepo(pathToDownload, archiveType, includeChecksums);
            response = Response.status(HttpStatus.SC_OK).type(MediaType.WILDCARD).entity((StreamingOutput) result::accept);
        } catch (FolderDownloadException fde) {
            response = Response.status(fde.getCode()).entity(fde.getMessage());
            log.error("Error while executing folder download on path: {} --> {}", pathToDownload, fde.getMessage());
            log.debug("Caught exception in folder download execution on path " + pathToDownload, fde);
        } catch (Exception e) {
            response = Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e.getMessage());
            log.error("Error while executing folder download on path: {} --> {}", pathToDownload, e.getMessage());
            log.debug("Caught exception in folder download execution on path " + pathToDownload, e);
        }
        return response.build();
    }

    private void markForDeletionAtResponseEnd(final File buildArtifactsArchive) {
        // delete the file after jersey streamed it back to the client
        closeableService.add(() -> FileUtils.deleteQuietly(buildArtifactsArchive));
    }
}
