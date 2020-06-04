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

import org.artifactory.addon.AddonsManager;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.exception.NotFoundException;
import org.artifactory.rest.http.request.ArtifactoryRestRequest;
import org.artifactory.rest.http.response.IResponse;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.artifactory.rest.common.util.RestUtils.RepoType.VIRTUAL;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetReplicationService extends BaseReplicationService {

    @Override
    public void execute(ArtifactoryRestRequest artifactoryRequest, IResponse artifactoryResponse) {
        String repoKey = artifactoryRequest.getPathParamByKey("repoKey");
        // verify pro license
        verifyArtifactoryPro();
        // verify key
        verifyRepositoryExists(repoKey);
        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
        RestUtils.RepoType repoType = RestUtils.repoType(repoKey);
        switch ((repoType != null) ? repoType : VIRTUAL) {
            case LOCAL:
                // fetch local replication
                fetchLocalReplication(artifactoryResponse, repoKey, descriptor);
                break;
            case REMOTE:
                // fetch remote replication
                fetchRemoteReplication(artifactoryResponse, repoKey, descriptor);
                break;
            default:
                throw new BadRequestException("Invalid repository. Replication is available for Local or Remote repositories only.");
        }
    }

    /**
     * fetch remote replication from config descriptor
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param repoKey             - repository key
     * @param descriptor          - config descriptor
     */
    private void fetchRemoteReplication(IResponse artifactoryResponse, String repoKey,
            CentralConfigDescriptor descriptor) {
        RemoteReplicationDescriptor remoteReplication = descriptor.getRemoteReplication(repoKey);
        if (remoteReplication == null) {
            throw new NotFoundException("Could not find replication");
        }
        artifactoryResponse.setIModel(remoteReplication);
    }

    /**
     * fetch remote replication from config descriptor
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param repoKey             - repository key
     * @param descriptor          - config descriptor
     */
    private void fetchLocalReplication(IResponse artifactoryResponse, String repoKey,
            CentralConfigDescriptor descriptor) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(
                AddonsManager.class);
        if (!addonsManager.isHaLicensed()) {
            // get single replication for pro license
            LocalReplicationDescriptor singleReplication = getSingleReplicationForProLicense(
                    repoKey, descriptor);
            artifactoryResponse.setIModel(singleReplication);
        } else {
            // get multi replication for enterprise License
            List<LocalReplicationDescriptor> multiReplication = getMultiReplicationForEnterpriseLicense(
                    repoKey, descriptor);
            artifactoryResponse.setIModelList(multiReplication);
        }
    }

    /**
     * get Single replication by Repo for Pro license
     *
     * @param repoKey    - repo key
     * @param descriptor - config descriptor
     */
    private LocalReplicationDescriptor getSingleReplicationForProLicense(String repoKey,
            CentralConfigDescriptor descriptor) {
        Map<String, LocalReplicationDescriptor> replicationMap = descriptor.getSingleReplicationPerRepoMap();
        LocalReplicationDescriptor localReplication = replicationMap.get(repoKey);
        if (localReplication == null) {
            throw new NotFoundException("Could not find replication");
        }
        return localReplication;
    }

    /**
     * get Multi replication by Repo for enterprise license
     *
     * @param repoKey    - repo key
     * @param descriptor - config descriptor
     */
    private List<LocalReplicationDescriptor> getMultiReplicationForEnterpriseLicense(String repoKey,
            CentralConfigDescriptor descriptor) {
        List<LocalReplicationDescriptor> localReplications = descriptor.getMultiLocalReplications(repoKey);
        if (localReplications == null || localReplications.isEmpty()) {
            throw new NotFoundException("Could not find replication");
        }
        return localReplications;
    }
}
