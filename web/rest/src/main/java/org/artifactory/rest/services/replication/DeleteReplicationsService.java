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

import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.http.request.ArtifactoryRestRequest;
import org.artifactory.rest.http.response.IResponse;
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
public class DeleteReplicationsService extends BaseReplicationService {
    @Override
    public void execute(ArtifactoryRestRequest artifactoryRequest, IResponse artifactoryResponse) {
        String repoKey = artifactoryRequest.getPathParamByKey("repoKey");
        String url = artifactoryRequest.getQueryParamByKey("url");
        verifyArtifactoryPro();
        verifyRepositoryExists(repoKey);
        MutableCentralConfigDescriptor descriptor = centralConfigService.getMutableDescriptor();
        RestUtils.RepoType repoType = RestUtils.repoType(repoKey);
        switch ((repoType != null) ? repoType : VIRTUAL) {
            case LOCAL:
                // delete local replications
                deleteLocalReplications(repoKey, url, descriptor);
                break;
            case REMOTE:
                // delete remote replications
                deleteRemoteReplication(repoKey, descriptor);
                break;
            default:
                throw new BadRequestException("Invalid repository");
        }
    }

    /**
     * delete local replications
     *
     * @param repoKey    - repo key
     * @param url        - url
     * @param descriptor - config descriptor
     */
    private void deleteLocalReplications(String repoKey, String url, MutableCentralConfigDescriptor descriptor) {
        if (url.length() == 0) {
            deleteAllReplication(repoKey, descriptor);
        } else {
            deleteSpecificReplication(repoKey, url, descriptor);
        }
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }

    /**
     * iterate repo replication and delete replication with same kkey and url
     *
     * @param repoKey    - repo key
     * @param url        - url
     * @param descriptor - config descriptor
     */
    private void deleteSpecificReplication(String repoKey, String url, MutableCentralConfigDescriptor descriptor) {
        List<LocalReplicationDescriptor> localReplications = descriptor.getLocalReplications();
        if (localReplications == null || localReplications.isEmpty()) {
            throw new BadRequestException("Could not find existing replication for delete");
        }
        int replicationIndex = 0;
        for (LocalReplicationDescriptor localReplicationDescriptor : localReplications) {
            if (localReplicationDescriptor.getRepoKey().equals(repoKey) && localReplicationDescriptor.getUrl().equals(
                    url)) {
                break;
            }
            replicationIndex = replicationIndex + 1;
        }
        if (localReplications.size() == replicationIndex) {
            throw new BadRequestException("Invalid replication url");
        } else {
            localReplications.remove(replicationIndex);
            descriptor.setLocalReplications(localReplications);
        }
    }

    /**
     * replication and delete all replication for repo key
     *
     * @param repoKey    - repo key
     * @param descriptor - config descriptor
     */
    private void deleteAllReplication(String repoKey, CentralConfigDescriptor descriptor) {
        List<LocalReplicationDescriptor> localReplications = descriptor.getMultiLocalReplications(repoKey);
        if (localReplications == null || localReplications.isEmpty()) {
            throw new BadRequestException("Could not find existing replication for update");
        }
        descriptor.getLocalReplications().removeAll(localReplications);
    }

    /**
     * delete remote replications
     *
     * @param repoKey    - repo key
     * @param descriptor - config descriptor
     */
    private void deleteRemoteReplication(String repoKey, MutableCentralConfigDescriptor descriptor) {
        RemoteReplicationDescriptor remoteReplication = descriptor.getRemoteReplication(repoKey);
        if (remoteReplication == null) {
            throw new BadRequestException("Could not find existing replication for update");
        }
        descriptor.getRemoteReplications().remove(remoteReplication);
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }
}
