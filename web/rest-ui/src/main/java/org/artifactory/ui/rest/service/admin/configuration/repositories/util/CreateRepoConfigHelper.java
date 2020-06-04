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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.p2.P2Addon;
import org.artifactory.addon.p2.P2Repo;
import org.artifactory.addon.smartrepo.EdgeSmartRepoAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.ResearchService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.repo.config.RepoConfigDefaultValues;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
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
import org.artifactory.util.InvalidNameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

/**
 * Delegate helper for creating and persisting repo config
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreateRepoConfigHelper {
    private static final Logger log = LoggerFactory.getLogger(CreateRepoConfigHelper.class);

    private static final String MSG_CREATING_DESCRIPTOR = "Creating descriptor from received model";

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private RepoConfigDescriptorBuilder repoBuilder;

    @Autowired
    private ReplicationConfigDescriptorBuilder replicationBuilder;

    @Autowired
    private ReplicationConfigService replicationConfigService;

    @Autowired
    private ReplicationConfigValidator replicationValidator;

    @Autowired
    private RepoConfigValidator repoValidator;

    @Autowired
    private ResearchService researchService;

    public MutableCentralConfigDescriptor handleLocal(LocalRepositoryConfigModel model,
            MutableCentralConfigDescriptor configDescriptor) throws RepoConfigException {
        log.debug("Model resolved to local repo descriptor, adding.");
        log.debug(MSG_CREATING_DESCRIPTOR);
        LocalRepoDescriptor repoDescriptor = model.toDescriptor(repoValidator, repoBuilder, configDescriptor);
        checkIfKeyLowerCase(repoDescriptor);
        configDescriptor.addLocalRepository(repoDescriptor);
        configDescriptor.conditionallyAddToBackups(repoDescriptor);
        ReverseProxyDescriptor reverseProxyDescriptor = getReverseProxyDescriptor(repoDescriptor, model);
        configDescriptor.updateReverseProxy(reverseProxyDescriptor);
        if (model.getReplications() != null) {
            log.debug("Creating push replication descriptor(s) from received model");
            Set<LocalReplicationDescriptor> replications =
                    model.getReplicationDescriptors(replicationValidator, replicationBuilder);
            replicationConfigService.addLocalReplications(replications, repoDescriptor, configDescriptor);
        }
        return configDescriptor;
    }

    public MutableCentralConfigDescriptor handleRemote(RemoteRepositoryConfigModel model,
            MutableCentralConfigDescriptor configDescriptor) throws RepoConfigException {
        log.debug("Model resolved to remote repo descriptor, adding.");
        log.debug(MSG_CREATING_DESCRIPTOR);
        HttpRepoDescriptor repoDescriptor = model.toDescriptor(repoValidator, repoBuilder, configDescriptor);
        validateSmartRemoteRepoOnEdge(repoDescriptor);
        ReverseProxyDescriptor reverseProxyDescriptor = getReverseProxyDescriptor(repoDescriptor, model);
        configDescriptor.updateReverseProxy(reverseProxyDescriptor);
        checkIfKeyLowerCase(repoDescriptor);
        configDescriptor.addRemoteRepository(repoDescriptor);
        if (model.getReplications() != null) {
            log.debug("Creating pull replication descriptor from received model");
            RemoteReplicationDescriptor replicationDescriptor =
                    model.getReplicationDescriptor(replicationValidator, replicationBuilder);
            replicationConfigService.addRemoteReplication(replicationDescriptor, repoDescriptor, configDescriptor);
        }
        return configDescriptor;
    }

    private void validateSmartRemoteRepoOnEdge(HttpRepoDescriptor repoDescriptor) throws RepoConfigException {
        if (addonsManager.addonByType(EdgeSmartRepoAddon.class).shouldBlockNonSmartRepo(repoDescriptor)) {
            throw new RepoConfigException(EdgeSmartRepoAddon.ERR_MSG, HttpStatus.SC_BAD_REQUEST);
        }
    }

    public MutableCentralConfigDescriptor handleVirtual(VirtualRepositoryConfigModel model,
            MutableCentralConfigDescriptor configDescriptor) throws RepoConfigException {
        log.debug("Model resolved to virtual repo descriptor, adding.");
        log.debug(MSG_CREATING_DESCRIPTOR);
        VirtualRepoDescriptor repoDescriptor = model.toDescriptor(repoValidator, repoBuilder, configDescriptor);
        ReverseProxyDescriptor reverseProxyDescriptor = getReverseProxyDescriptor(repoDescriptor, model);
        configDescriptor.updateReverseProxy(reverseProxyDescriptor);
        if (repoDescriptor.getType().equals(RepoType.P2)) {
            log.debug("Creating P2 config for repo {}", repoDescriptor.getKey());
            repoDescriptor.setRepositories(validateAndCreateP2ConfigRepos(configDescriptor, repoDescriptor,
                    ((P2TypeSpecificConfigModel) model.getTypeSpecific()).getP2Repos()));
        }
        checkIfKeyLowerCase(repoDescriptor);
        configDescriptor.addVirtualRepository(repoDescriptor);
        return configDescriptor;
    }

    private void checkIfKeyLowerCase(RepoBaseDescriptor descriptor) {
        //docker repository key must be lower case in order it will work with reverse proxy
        if (descriptor.getType().equals(RepoType.Docker)) {
            String repoKey = descriptor.getKey();
            if (!repoKey.equals(repoKey.toLowerCase())) {
                throw new InvalidNameException("Docker Repository " + repoKey + " must be in lower case");
            }
        }
    }

    public MutableCentralConfigDescriptor handleDistribution(DistributionRepositoryConfigModel model,
            MutableCentralConfigDescriptor configDescriptor) throws RepoConfigException {
        log.debug("Model resolved to distribution repo descriptor, adding.");
        log.debug(MSG_CREATING_DESCRIPTOR);
        if (!model.getTypeSpecific().getRepoType().equals(RepoType.Distribution)) {
            throw new RepoConfigException(
                    "Distribution repo's type specific configuration must be of type Distribution", SC_BAD_REQUEST);
        }
        DistributionRepoDescriptor repoDescriptor = model.toDescriptor(repoValidator, repoBuilder, configDescriptor);
        configDescriptor.addDistributionRepository(repoDescriptor);
        ReverseProxyDescriptor reverseProxyDescriptor = getReverseProxyDescriptor(repoDescriptor, model);
        configDescriptor.updateReverseProxy(reverseProxyDescriptor);
        return configDescriptor;
    }

    public MutableCentralConfigDescriptor handleReleaseBundles(ReleaseBundlesRepositoryConfigModel model,
            MutableCentralConfigDescriptor configDescriptor) throws RepoConfigException {
        log.debug("Model resolved to release bundle repo descriptor, adding.");
        log.debug(MSG_CREATING_DESCRIPTOR);
        ReleaseBundlesRepoDescriptor repoDescriptor = model.toDescriptor(repoValidator, repoBuilder, configDescriptor);
        checkIfKeyLowerCase(repoDescriptor);
        configDescriptor.addReleaseBundlesRepository(repoDescriptor);
        configDescriptor.conditionallyAddToBackups(repoDescriptor);
        ReverseProxyDescriptor reverseProxyDescriptor = getReverseProxyDescriptor(repoDescriptor, model);
        configDescriptor.updateReverseProxy(reverseProxyDescriptor);
        return configDescriptor;
    }

    List<RepoDescriptor> validateAndCreateP2ConfigRepos(MutableCentralConfigDescriptor configDescriptor,
            VirtualRepoDescriptor repoDescriptor, List<P2Repo> requestedRepos) {
        BasicStatusHolder status = new BasicStatusHolder();
        Map<String, List<String>> subCompositeUrls = Maps.newHashMap();
        //Addon verifies each url and crunches out a list of local and remote repositories that should be aggregated
        //In this virtual based on the URLs passed to it
        List<P2Repo> p2Repos = addonsManager.addonByType(P2Addon.class).verifyRemoteRepositories(configDescriptor,
                repoDescriptor, null, requestedRepos, subCompositeUrls, status);
        List<RepoDescriptor> descriptorsToAdd = Lists.newArrayList();
        for (P2Repo repo : p2Repos) {
            RepoDescriptor newDescriptor = repo.getDescriptor();
            if (!configDescriptor.isRepositoryExists(repo.getRepoKey())) {
                if (repo.getDescriptor() instanceof HttpRepoDescriptor) {
                    HttpRepoDescriptor newRemoteDescriptor = (HttpRepoDescriptor) newDescriptor;
                    //Set default proxy for automatically created new remote repos
                    newRemoteDescriptor.setProxy(configDescriptor.defaultProxyDefined());
                    //P2 remote repos list remote items by default, as we're not creating through the normal
                    //model chain we have to set it here for new repos
                    newRemoteDescriptor.setListRemoteFolderItems(
                            RepoConfigDefaultValues.DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE);
                    checkIfKeyLowerCase(newRemoteDescriptor);
                    configDescriptor.addRemoteRepository(newRemoteDescriptor);
                }
                log.info("Remote repository {} is being created based on {}'s P2 config", newDescriptor.getKey(),
                        repoDescriptor.getKey());
            }
            descriptorsToAdd.add(newDescriptor);
        }
        return descriptorsToAdd.stream().distinct().collect(Collectors.toList());
    }

    private ReverseProxyDescriptor getReverseProxyDescriptor(RepoBaseDescriptor repoDescriptor,
            RepositoryConfigModel model) {
        return model.getReverseProxyDescriptor(repoDescriptor, repoBuilder);
    }
}
