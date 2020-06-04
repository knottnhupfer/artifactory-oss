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

package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.p2.P2Repo;
import org.artifactory.addon.smartrepo.EdgeSmartRepoAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.ResearchService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.ui.rest.model.admin.configuration.repository.distribution.DistributionRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.releasebundles.ReleaseBundlesRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.P2TypeSpecificConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualRepositoryConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.replication.ReplicationConfigService;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.ReplicationConfigDescriptorBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigDescriptorBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.ReplicationConfigValidator;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.RepoConfigValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Delegate helper for creating and persisting repo config
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateRepoConfigHelper {
    private static final Logger log = LoggerFactory.getLogger(UpdateRepoConfigHelper.class);

    private static final String MSG_UPDATING_DESCRIPTOR = "Updating descriptor for {}.";

    @Autowired
    private RepositoryService repoService;

    @Autowired
    private RepoConfigDescriptorBuilder builder;

    @Autowired
    private RepoConfigValidator validator;

    @Autowired
    private ReplicationConfigDescriptorBuilder replicationBuilder;

    @Autowired
    private ReplicationConfigService replicationConfigService;

    @Autowired
    private ReplicationConfigValidator replicationValidator;

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private CreateRepoConfigHelper creator;

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private ResearchService researchService;

    public void handleLocal(LocalRepositoryConfigModel model) throws RepoConfigException {
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        log.debug("Model resolved to local repo descriptor, updating.");
        String repoKey = model.getGeneral().getRepoKey();
        LocalRepoDescriptor repoDescriptor = model.toDescriptor(validator, builder, configDescriptor);
        ReverseProxyDescriptor reverseProxyDescriptor = model.getReverseProxyDescriptor(repoDescriptor, builder);
        Set<LocalReplicationDescriptor> replications = model.getReplicationDescriptors(replicationValidator, replicationBuilder);
        log.debug(MSG_UPDATING_DESCRIPTOR, repoKey);
        configDescriptor.updateReverseProxy(reverseProxyDescriptor);
        configDescriptor.getLocalRepositoriesMap().put(repoDescriptor.getKey(), repoDescriptor);
        replicationConfigService.updateLocalReplications(replications, repoKey, configDescriptor);
        configService.saveEditedDescriptorAndReload(configDescriptor);
    }

    public void handleRemote(RemoteRepositoryConfigModel model) throws IOException, RepoConfigException {
        log.debug("Model resolved to remote repo descriptor, updating.");
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        String repoKey = model.getGeneral().getRepoKey();
        HttpRepoDescriptor repoDescriptor = model.toDescriptor(validator, builder, configDescriptor);
        validateSmartRemoteRepoOnEdge(repoDescriptor);
        ReverseProxyDescriptor reverseProxyDescriptor = model.getReverseProxyDescriptor(repoDescriptor, builder);
        configDescriptor.updateReverseProxy(reverseProxyDescriptor);
        RemoteReplicationDescriptor replication = model.getReplicationDescriptor(replicationValidator, replicationBuilder);
        log.debug(MSG_UPDATING_DESCRIPTOR, repoDescriptor.getKey());
        if (replication != null) {
            replicationConfigService.updateRemoteReplication(replication, repoDescriptor, configDescriptor);
        }
        configDescriptor.getRemoteRepositoriesMap().put(repoKey, repoDescriptor);
        configService.saveEditedDescriptorAndReload(configDescriptor);
    }

    private void validateSmartRemoteRepoOnEdge(HttpRepoDescriptor repoDescriptor) throws RepoConfigException {
        if (addonsManager.addonByType(EdgeSmartRepoAddon.class).shouldBlockNonSmartRepo(repoDescriptor)) {
            throw new RepoConfigException(EdgeSmartRepoAddon.ERR_MSG, HttpStatus.SC_BAD_REQUEST);
        }
    }

    public void handleVirtual(VirtualRepositoryConfigModel model) throws RepoConfigException {
        log.debug("Model resolved to virtual repo descriptor, updating.");
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        String repoKey = model.getGeneral().getRepoKey();
        VirtualRepoDescriptor repoDescriptor = model.toDescriptor(validator, builder, configDescriptor);
        ReverseProxyDescriptor reverseProxyDescriptor = model.getReverseProxyDescriptor(repoDescriptor, builder);
        configDescriptor.updateReverseProxy(reverseProxyDescriptor);
        if (repoDescriptor.getType().equals(RepoType.P2)) {
            updateP2Config(configDescriptor, repoDescriptor,
                    ((P2TypeSpecificConfigModel) model.getTypeSpecific()).getP2Repos());
        }
        log.debug(MSG_UPDATING_DESCRIPTOR, repoDescriptor.getKey());
        configDescriptor.getVirtualRepositoriesMap().put(repoKey, repoDescriptor);
        configService.saveEditedDescriptorAndReload(configDescriptor);
    }

    public void handleDistribution(DistributionRepositoryConfigModel model) throws RepoConfigException {
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        log.debug("Model resolved to distribution repo descriptor, updating.");
        String repoKey = model.getGeneral().getRepoKey();
        DistributionRepoDescriptor repoDescriptor = model.toDescriptor(validator, builder, configDescriptor);
        ReverseProxyDescriptor reverseProxyDescriptor = model.getReverseProxyDescriptor(repoDescriptor, builder);
        log.debug(MSG_UPDATING_DESCRIPTOR, repoKey);
        configDescriptor.updateReverseProxy(reverseProxyDescriptor);
        configDescriptor.getDistributionRepositoriesMap().put(repoDescriptor.getKey(), repoDescriptor);
        configService.saveEditedDescriptorAndReload(configDescriptor);
    }

    public void handleReleaseBundles(ReleaseBundlesRepositoryConfigModel model) throws RepoConfigException {
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        log.debug("Model resolved to release bundle repo descriptor, updating.");
        String repoKey = model.getGeneral().getRepoKey();
        ReleaseBundlesRepoDescriptor repoDescriptor = model.toDescriptor(validator, builder, configDescriptor);
        ReverseProxyDescriptor reverseProxyDescriptor = model.getReverseProxyDescriptor(repoDescriptor, builder);

        log.debug(MSG_UPDATING_DESCRIPTOR, repoKey);
        configDescriptor.updateReverseProxy(reverseProxyDescriptor);
        configDescriptor.getReleaseBundlesRepositoriesMap().put(repoDescriptor.getKey(), repoDescriptor);

        configService.saveEditedDescriptorAndReload(configDescriptor);
    }

    private void updateP2Config(MutableCentralConfigDescriptor configDescriptor, VirtualRepoDescriptor repoDescriptor,
            List<P2Repo> requestedRepos) {
        log.debug("Updating P2 config for repo {}", repoDescriptor.getKey());
        //Preserve all already-aggregated virtual repos this repo might have for backwards compatibility although
        // we don't allow adding new ones anymore, and append to the ones returned by the P2 config logic
        List<RepoDescriptor> reposToAdd = repoService.virtualRepoDescriptorByKey(repoDescriptor.getKey())
                .getRepositories().stream()
                .filter(aggregatedRepo -> !aggregatedRepo.isReal()).collect(Collectors.toList());
        reposToAdd.addAll(creator.validateAndCreateP2ConfigRepos(configDescriptor, repoDescriptor, requestedRepos));
        repoDescriptor.setRepositories(reposToAdd);
    }
}
