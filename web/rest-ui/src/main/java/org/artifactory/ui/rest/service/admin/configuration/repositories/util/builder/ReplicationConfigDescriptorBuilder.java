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

package org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder;

import com.google.common.collect.Sets;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.remote.RemoteReplicationConfigModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Utility class for converting model to descriptor
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReplicationConfigDescriptorBuilder {

    @Autowired
    private CentralConfigService centralConfig;

    public Set<LocalReplicationDescriptor> buildLocalReplications(List<LocalReplicationConfigModel> models, String repoKey) {
        Set<LocalReplicationDescriptor> descriptors = Sets.newHashSet();
        for (LocalReplicationConfigModel model : models) {
            LocalReplicationDescriptor descriptor = buildLocalReplication(model, repoKey);
            descriptors.add(descriptor);
        }
        return descriptors;
    }

    public LocalReplicationDescriptor buildLocalReplication(LocalReplicationConfigModel model, String repoKey) {
        LocalReplicationDescriptor descriptor = new LocalReplicationDescriptor();
        descriptor.setEnableEventReplication(model.isEnableEventReplication());
        descriptor.setUrl(model.getUrl());
        descriptor.setUsername(model.getUsername());
        descriptor.setPassword(CryptoHelper.encryptIfNeeded(ArtifactoryHome.get(), model.getPassword()));
        descriptor.setProxy(centralConfig.getDescriptor().getProxy(model.getProxy()));
        descriptor.setSocketTimeoutMillis(model.getSocketTimeout());
        descriptor.setCronExp(model.getCronExp());
        descriptor.setEnabled(model.isEnabled());
        descriptor.setRepoKey(repoKey);
        descriptor.setSyncDeletes(model.isSyncDeletes());
        descriptor.setSyncProperties(model.isSyncProperties());
        descriptor.setSyncStatistics(model.isSyncStatistics());
        descriptor.setPathPrefix(model.getPathPrefix());
        return descriptor;
    }

    public RemoteReplicationDescriptor buildRemoteReplication(RemoteReplicationConfigModel model, String repoKey) {
        RemoteReplicationDescriptor descriptor = new RemoteReplicationDescriptor();
        descriptor.setEnabled(model.isEnabled());
        descriptor.setCronExp(model.getCronExp());
        descriptor.setSyncDeletes(model.isSyncDeletes());
        descriptor.setSyncProperties(model.isSyncProperties());
        descriptor.setEnableEventReplication(model.isEnableEventReplication());
        descriptor.setRepoKey(repoKey);
        descriptor.setPathPrefix(model.getPathPrefix());
        return descriptor;
    }
}
