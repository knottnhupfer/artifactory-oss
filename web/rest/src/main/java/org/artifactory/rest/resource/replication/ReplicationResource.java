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

package org.artifactory.rest.resource.replication;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.distribution.DistributionAddon;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.rest.constant.ReplicationRestConstants;
import org.artifactory.api.rest.distribution.bundle.models.FileSpec;
import org.artifactory.api.rest.replication.ReplicationRequest;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.ReplicationBaseDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.util.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * REST resource for invoking local and remote replication procedures.
 *
 * @author Noam Y. Tenne
 */
@Component
@Path(ReplicationRestConstants.ROOT)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class ReplicationResource {
    private static final Logger log = LoggerFactory.getLogger(ReplicationResource.class);
    @Context
    HttpServletRequest httpRequest;

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private CentralConfigService centralConfigService;


    /**
     * Returns the latest replication status of the given path if annotated
     *
     * @param path Path to check for annotations
     * @return Response
     */
    @GET
    @Produces({ReplicationRestConstants.MT_REPLICATION_STATUS, MediaType.APPLICATION_JSON})
    @Path("{path: .+}")
    public Response getReplicationStatus(@PathParam("path") String path) {
        RepoPath repoPath = InternalRepoPathFactory.create(path);
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        return addonsManager.addonByType(RestAddon.class).getReplicationStatus(repoPath);
    }

    /**
     * Executes replication operations
     *
     * @param path               Replication root
     * @param replicationRequest Replication settings
     * @return Response
     *
     * @deprecated since 4.7.5, replaces by {@link #replicateAll}
     */
    @Deprecated
    @POST
    @Consumes({ReplicationRestConstants.MT_REPLICATION_REQUEST, MediaType.APPLICATION_JSON})
    @Path("{path: .+}")
    public Response replicate(
            @PathParam("path") String path,
            ReplicationRequest replicationRequest) throws IOException {
        RepoPath repoPath = InternalRepoPathFactory.create(path);
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        return addonsManager.addonByType(RestAddon.class).replicate(repoPath, replicationRequest);
    }

    /**
     * Executes replication with default values and no JSON body
     * (using for pull, false properties and deletes and uses the remote repository URL and credentials)
     *
     * @param path Replication root
     * @deprecated since 4.7.5, replaces by {@link ReplicationResource#replicateAll
     */
    @Deprecated
    @POST
    @Path("{path: .+}")
    public Response replicateNoJson(@PathParam("path") String path) throws IOException {
        return replicate(path, new ReplicationRequest());
    }

    /**
     * Schedule immediate replication for local/multipush/remote repository.
     *
     * @param path                Path for replication
     * @param replicationRequests List of replicationRequests objects
     * @return Response that is translated to a JSON content
     *
     * @throws IOException
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("execute/{path: .+}")
    public Response replicateAll(@PathParam("path") String path, List<ReplicationRequest> replicationRequests, @QueryParam("strategy") String strategy)
            throws IOException {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        return addonsManager.addonByType(RestAddon.class).replicate(RepoPathFactory.create(path), replicationRequests, strategy);
    }


    /**
     * Schedule immediate replication for local/multipush/remote repository.
     * Since no payload supplied by the user, Artifactory will schedule the replication for existing replication
     * configurations
     *
     * @param path Path for replication
     * @return JSON response
     *
     */
    @POST
    @Path("execute/{path: .+}")
    public Response replicateAllNoJson(@PathParam("path") String path, @QueryParam("strategy") String strategy) throws IOException {
        if (!StringUtils.isBlank(strategy)) {
            List<LocalReplicationDescriptor> replicationDescriptors = centralConfigService.getDescriptor().getLocalReplications()
                    .stream().filter(desc -> desc.getRepoPath().getRepoKey().equals(path)).filter(ReplicationBaseDescriptor::isEnabled).collect(Collectors.toList());

            if (replicationDescriptors.isEmpty()) {
                log.info("Request replication/execute/{} ignored. The repository was not found.", path);
                return Response.status(NOT_FOUND).build();
            }

            ReplicationAddon replicationAddon = addonsManager.addonByType(ReplicationAddon.class);
            replicationDescriptors.forEach(replicatonDescriptor -> replicationAddon.setPushStrategy(replicatonDescriptor, strategy));
        }

        return replicateAll(path, Lists.newArrayList(), strategy);
    }

    @POST
    @Path("replicate/file/{tx_path: .+}")
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public Response replicateArtifact(FileSpec fileSpec,
            @HeaderParam(ReplicationRestConstants.DELEGATE_TOKEN_HEADER) String delegateToken,
            @HeaderParam("Authorization") String auth,
            @PathParam("tx_path") String transactionPath) {
        validateReplicateArtifactParameters(fileSpec, delegateToken, transactionPath);
        fileSpec.setInternalTmpPath(transactionPath);
        DistributionAddon distributionAddon = addonsManager.addonByType(DistributionAddon.class);
        BasicStatusHolder statusHolder = distributionAddon.distributeArtifact(fileSpec, delegateToken, auth);
        if (statusHolder.hasErrors()) {
            return createErrorResponse(statusHolder);
        }
        return Response.ok(ResponseUtils.getResponse(statusHolder)).build();
    }

    @POST
    @Path("replicate/file/streaming/{tx_path: .+}")
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response replicateArtifactStreaming(FileSpec fileSpec,
            @HeaderParam(ReplicationRestConstants.FILE_TXID_HEADER) String fileTransactionId,
            @HeaderParam(ReplicationRestConstants.DELEGATE_TOKEN_HEADER) String delegateToken,
            @HeaderParam("Authorization") String auth,
            @PathParam("tx_path") String transactionPath) {
        log.debug("Replicate streaming request for file: {}", fileSpec);
        validateReplicateArtifactParameters(fileSpec, delegateToken, transactionPath);
        fileSpec.setInternalTmpPath(transactionPath);
        DistributionAddon distributionAddon = addonsManager.addonByType(DistributionAddon.class);
        try {
            String checksum = distributionAddon.validateFileAndGetChecksum(fileSpec);
            return Response.ok()
                    .entity((StreamingOutput) outputStream -> distributionAddon.distributeArtifactStreaming(fileTransactionId, fileSpec, delegateToken, auth, checksum, outputStream))
                    .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
                    .type(MediaType.APPLICATION_OCTET_STREAM)
                    .build();
        } catch (StatusHasErrorException e) {
            return createErrorResponse(e.getStatus());
        }
    }

    private void validateReplicateArtifactParameters(FileSpec fileSpec, String delegateToken, String transactionPath) {
        if (StringUtils.isBlank(delegateToken)) {
            throw new BadRequestException("Missing X-Auth-Token-Delegate header");
        }
        if (StringUtils.isBlank(transactionPath)) {
            throw new BadRequestException("Missing transaction id");
        }
        if (StringUtils.isBlank(fileSpec.getTargetPath())) {
            throw new BadRequestException("No target repo path specified to use for distributing the artifact " +
                    fileSpec.getSourcePath());
        }
    }

    private Response createErrorResponse(BasicStatusHolder statusHolder) {
        int code = statusHolder.getMostImportantErrorStatusCode().getStatusCode();
        return Response.status(code).entity(ResponseUtils.getErrorResponse(statusHolder)).build();
    }
}
