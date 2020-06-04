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

import org.artifactory.api.rest.replication.MultipleReplicationConfigRequest;
import org.artifactory.api.rest.replication.ReplicationConfigRequest;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.http.request.ArtifactoryRestRequest;
import org.artifactory.rest.http.response.IResponse;
import org.artifactory.rest.resource.replication.ReplicationConfigRequestHelper;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.artifactory.rest.common.util.RestUtils.RepoType.VIRTUAL;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateMultipleReplicationsService extends BaseReplicationService {
    @Override
    public void execute(ArtifactoryRestRequest artifactoryRequest, IResponse artifactoryResponse) {
        MultipleReplicationConfigRequest replicationRequest = (MultipleReplicationConfigRequest) artifactoryRequest.getImodel();
        String repoKey = artifactoryRequest.getPathParamByKey("repoKey");
        verifyArtifactoryEnterprise();
        verifyRepositoryExists(repoKey);
        MutableCentralConfigDescriptor descriptor = centralConfigService.getMutableDescriptor();
        RestUtils.RepoType repoType = RestUtils.repoType(repoKey);
        switch ((repoType != null) ? repoType : VIRTUAL) {
            case LOCAL:
                // update Local replications
                updateMultipleLocalReplication(replicationRequest, repoKey, descriptor);
                break;
            default:
                throw new BadRequestException("Invalid repository");
        }
    }

    /**
     * update local replications
     *
     * @param replicationRequest - replications request model
     * @param repoKey            - repository key
     * @param descriptor         - config descriptor
     */
    private void updateMultipleLocalReplication(MultipleReplicationConfigRequest replicationRequest, String repoKey,
            MutableCentralConfigDescriptor descriptor) {
        String cronExp = replicationRequest.getCronExp();
        Boolean eventReplications = replicationRequest.isEnableEventReplication();
        List<ReplicationConfigRequest> replications = replicationRequest.getReplications();
        for (ReplicationConfigRequest replication : replications) {
            validateIsCheckBinaryExistenceInFilestoreAllowed(replication);
            LocalReplicationDescriptor localReplication = descriptor.getLocalReplication(repoKey, replication.getUrl());
            if (localReplication == null) {
                throw new BadRequestException("Could not find existing replication for update");
            }
            if (eventReplications != null) {
                replication.setEnableEventReplication(eventReplications);
            }
            if (cronExp != null) {
                localReplication.setCronExp(cronExp);
            }
            ReplicationConfigRequestHelper.fillLocalReplicationDescriptor(replication, localReplication);
            ReplicationConfigRequestHelper.verifyLocalReplicationRequest(localReplication);
        }
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }
}
