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

package org.artifactory.rest.services.replication;

import org.artifactory.addon.smartrepo.EdgeSmartRepoAddon;
import org.artifactory.api.rest.replication.ReplicationConfigRequest;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.http.request.ArtifactoryRestRequest;
import org.artifactory.rest.http.response.IResponse;
import org.artifactory.rest.resource.replication.ReplicationConfigRequestHelper;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.artifactory.rest.common.util.RestUtils.RepoType.VIRTUAL;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateReplicationService extends BaseReplicationService {

    @Override
    public void execute(ArtifactoryRestRequest artifactoryRequest, IResponse artifactoryResponse) {
        ReplicationConfigRequest replicationRequest = (ReplicationConfigRequest) artifactoryRequest.getImodel();
        String repoKey = artifactoryRequest.getPathParamByKey("repoKey");
        verifyArtifactoryPro();
        verifyRepositoryExists(repoKey);
        validateIsCheckBinaryExistenceInFilestoreAllowed(replicationRequest);
        MutableCentralConfigDescriptor descriptor = centralConfigService.getMutableDescriptor();
        RestUtils.RepoType repoType = RestUtils.repoType(repoKey);
        switch ((repoType != null) ? repoType : VIRTUAL) {
            case LOCAL:
                // update Local replications
                updateLocalReplication(replicationRequest, repoKey, descriptor);
                break;
            case REMOTE:
                // update remote replications
                updateRemoteReplications(replicationRequest, repoKey, descriptor);
                break;
            default:
                throw new BadRequestException("Invalid repository");
        }
    }

    /**
     * update remote replications
     *
     * @param replicationRequest - replication  request model
     * @param repoKey            - rep key
     * @param descriptor         - config descriptor
     */
    private void updateRemoteReplications(ReplicationConfigRequest replicationRequest, String repoKey,
            MutableCentralConfigDescriptor descriptor) {
        RemoteReplicationDescriptor remoteReplication = descriptor.getRemoteReplication(repoKey);
        if (remoteReplication == null) {
            throw new BadRequestException("Could not find existing replication for update");
        }
        addonsManager.addonByType(EdgeSmartRepoAddon.class).validateReplication();
        ReplicationConfigRequestHelper.fillBaseReplicationDescriptor(replicationRequest, remoteReplication);
        ReplicationConfigRequestHelper.verifyBaseReplicationRequest(remoteReplication);
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }

    /**
     * update local replications
     *
     * @param replicationRequest - replications request model
     * @param repoKey            - repository key
     * @param descriptor         - config descriptor
     */
    private void updateLocalReplication(ReplicationConfigRequest replicationRequest, String repoKey,
            MutableCentralConfigDescriptor descriptor) {
        LocalReplicationDescriptor localReplication = descriptor.getLocalReplication(repoKey);
        if (localReplication == null) {
            throw new BadRequestException("Could not find existing replication for update");
        }
        ReplicationConfigRequestHelper.fillLocalReplicationDescriptor(replicationRequest, localReplication);
        ReplicationConfigRequestHelper.verifyLocalReplicationRequest(localReplication);
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }
}
