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

import org.artifactory.api.rest.replication.ReplicationEnableDisableRequest;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.replication.ReplicationBaseDescriptor;
import org.artifactory.rest.http.request.ArtifactoryRestRequest;
import org.artifactory.rest.http.response.IResponse;
import org.jfrog.common.PathMatcher;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EnableDisableReplicationsService extends BaseReplicationService {

    @Override
    public void execute(ArtifactoryRestRequest artifactoryRequest, IResponse artifactoryResponse) {
        ReplicationEnableDisableRequest request = (ReplicationEnableDisableRequest) artifactoryRequest.getImodel();
        // validate pro license
        verifyArtifactoryPro();

        MutableCentralConfigDescriptor descriptor = centralConfigService.getMutableDescriptor();
        handleLocalReplications(request, descriptor);
        handleRemoteReplications(request, descriptor);
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
        artifactoryResponse.setResponseCode(HttpServletResponse.SC_CREATED);
    }

    private void handleLocalReplications(ReplicationEnableDisableRequest request, CentralConfigDescriptor descriptor) {
        descriptor.getLocalReplications().stream()
                .forEach(replication -> enableOrDisable(replication, replication.getUrl(), request));
    }

    private void handleRemoteReplications(ReplicationEnableDisableRequest request, CentralConfigDescriptor descriptor) {
        descriptor.getRemoteReplications().stream()
                .forEach(replication -> enableOrDisable(replication, getRemoteUrl(descriptor, replication.getRepoKey()), request));
    }

    private String getRemoteUrl(CentralConfigDescriptor descriptor, String repoKey) {
        return descriptor.getRemoteRepositoriesMap().get(repoKey).getUrl();
    }

    private void enableOrDisable(ReplicationBaseDescriptor replication, String url, ReplicationEnableDisableRequest request) {
        if (PathMatcher.matches(url, request.getInclude(), request.getExclude(), false)) {
            replication.setEnabled(request.isEnable());
        }
    }
}
