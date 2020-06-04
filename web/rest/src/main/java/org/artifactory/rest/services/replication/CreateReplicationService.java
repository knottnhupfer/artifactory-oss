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

import javax.servlet.http.HttpServletResponse;

import static org.artifactory.rest.common.util.RestUtils.RepoType.VIRTUAL;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreateReplicationService extends BaseReplicationService {

    @Override
    public void execute(ArtifactoryRestRequest artifactoryRequest, IResponse artifactoryResponse) {
        String repoKey = artifactoryRequest.getPathParamByKey("repoKey");
        ReplicationConfigRequest replicationRequest = (ReplicationConfigRequest) artifactoryRequest.getImodel();
        verifyArtifactoryPro();
        verifyRepositoryExists(repoKey);
        validateIsCheckBinaryExistenceInFilestoreAllowed(replicationRequest);
        MutableCentralConfigDescriptor descriptor = centralConfigService.getMutableDescriptor();
        RestUtils.RepoType repoType = RestUtils.repoType(repoKey);
        switch ((repoType != null) ? repoType : VIRTUAL) {
            case LOCAL:
                // add or replace local replication
                addOrReplaceLocalReplication(repoKey, replicationRequest, descriptor);
                break;
            case REMOTE:
                // add or replace remote replication
                addOrReplaceRemoteReplication(repoKey, replicationRequest, descriptor);
                break;
            default:
                throw new BadRequestException("Invalid repository");
        }
        // update response code
        artifactoryResponse.setResponseCode(HttpServletResponse.SC_CREATED);
    }

    /**
     * add or replace remote replication
     *
     * @param repoKey            - repository key
     * @param replicationRequest - replication request model
     * @param descriptor         - config descriptor
     */
    private void addOrReplaceRemoteReplication(String repoKey, ReplicationConfigRequest replicationRequest,
            MutableCentralConfigDescriptor descriptor) {
        addonsManager.addonByType(EdgeSmartRepoAddon.class).validateReplication();
        RemoteReplicationDescriptor remoteReplication = new RemoteReplicationDescriptor();
        remoteReplication.setRepoKey(repoKey);
        ReplicationConfigRequestHelper.fillBaseReplicationDescriptor(replicationRequest, remoteReplication);
        ReplicationConfigRequestHelper.fillRemoteReplicationDescriptor(replicationRequest, remoteReplication);
        ReplicationConfigRequestHelper.verifyBaseReplicationRequest(remoteReplication);
        addOrReplace(remoteReplication, descriptor.getRemoteReplications());
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }

    /**
     * add or replace local replication
     *
     * @param repoKey            - repository key
     * @param replicationRequest - replication request model
     * @param descriptor         - config descriptor
     */
    private void addOrReplaceLocalReplication(String repoKey, ReplicationConfigRequest replicationRequest,
            MutableCentralConfigDescriptor descriptor) {
        LocalReplicationDescriptor localReplication = new LocalReplicationDescriptor();
        localReplication.setRepoKey(repoKey);
        ReplicationConfigRequestHelper.fillLocalReplicationDescriptor(replicationRequest, localReplication);
        ReplicationConfigRequestHelper.verifyLocalReplicationRequest(localReplication);
        addOrReplace(localReplication, descriptor.getLocalReplications());
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }
}
