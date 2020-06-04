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
import org.artifactory.rest.http.request.ArtifactoryRestRequest;
import org.artifactory.rest.http.response.IResponse;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yoaz Menda
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAllReplicationsService extends BaseReplicationService {

    @Override
    public void execute(ArtifactoryRestRequest artifactoryRequest, IResponse artifactoryResponse) {
        verifyArtifactoryPro();
        CentralConfigDescriptor configDescriptor = centralConfigService.getDescriptor();
        List<ReplicationConfigurationDto> replications = new ArrayList<>();
        configDescriptor.getLocalReplications().forEach(localReplicationDescriptor -> replications
                .add(new ReplicationConfigurationDto(localReplicationDescriptor)));
        configDescriptor.getRemoteReplications().forEach(remoteReplicationDescriptor -> replications
                .add(new ReplicationConfigurationDto(remoteReplicationDescriptor)));
        artifactoryResponse.setIModel(replications);
    }
}
