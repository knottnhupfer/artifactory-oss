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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfrog.bintray.client.api.BintrayCallException;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.p2.P2Repo;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.api.bintray.distribution.BintrayOAuthAppConfigurator;
import org.artifactory.api.bintray.distribution.model.DistributionRepoCreationDetails;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.PropertySetNameModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.AdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.BasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.DownloadRedirectRepoConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.GeneralRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.distribution.DistributionAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.distribution.DistributionBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.distribution.DistributionRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.releasebundles.ReleaseBundlesAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.releasebundles.ReleaseBundlesBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.releasebundles.ReleaseBundlesRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.*;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.remote.RemoteReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.*;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualSelectedRepository;
import org.artifactory.ui.rest.model.admin.configuration.repository.xray.XrayRepoConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.reverseProxy.ReverseProxyRepoModel;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Utility class for converting descriptor to model
 *
 * @author Aviad Shikloshi
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RepoConfigModelBuilder {
    private static final Logger log = LoggerFactory.getLogger(RepoConfigModelBuilder.class);

    private CentralConfigService centralConfig;

    private AddonsManager addonsManager;

    /**
     * Populate model configuration from local repository descriptor
     */
    public void populateLocalDescriptorValuesToModel(LocalRepoDescriptor descriptor, LocalRepositoryConfigModel model) {
        GeneralRepositoryConfigModel general = createGeneralConfig(descriptor);
        LocalBasicRepositoryConfigModel basic = createLocalBasicConfig(descriptor);
        LocalAdvancedRepositoryConfigModel advanced = createLocalAdvancedConfig(descriptor);
        TypeSpecificConfigModel typeSpecific = createLocalTypeSpecific(descriptor.getType(), descriptor);

        List<LocalReplicationDescriptor> replicationDescriptors = centralConfig.getDescriptor()
                .getMultiLocalReplications(descriptor.getKey());
        List<LocalReplicationConfigModel> replications = replicationDescriptors.stream()
                .map(this::createLocalReplicationConfig)
                .collect(Collectors.toList());

        model.setGeneral(general);
        model.setBasic(basic);
        model.setAdvanced(advanced);
        model.setTypeSpecific(typeSpecific);
        model.setReplications(replications);
    }

    /**
     * Populate model configuration from remote repository descriptor
     */
    public void populateRemoteRepositoryConfigValuesToModel(HttpRepoDescriptor descriptor,
            RemoteRepositoryConfigModel model) {
        GeneralRepositoryConfigModel general = createGeneralConfig(descriptor);
        model.setGeneral(general);

        RemoteBasicRepositoryConfigModel basic = createRemoteBasicConfig(descriptor);
        model.setBasic(basic);

        RemoteReplicationDescriptor replicationDescriptor = centralConfig.getDescriptor().getRemoteReplication(
                descriptor.getKey());
        if (replicationDescriptor != null) {
            RemoteReplicationConfigModel replication = createRemoteReplicationConfigModel(replicationDescriptor);
            model.setReplications(Lists.newArrayList(replication));
        }
        RemoteAdvancedRepositoryConfigModel advanced = createRemoteAdvancedConfig(descriptor);
        model.setAdvanced(advanced);
        TypeSpecificConfigModel typeSpecific = createRemoteTypeSpecific(descriptor.getType(), descriptor);
        model.setTypeSpecific(typeSpecific);
    }

    /**
     * Populate model configuration from virtual repository descriptor
     */
    public void populateVirtualRepositoryConfigValuesToModel(VirtualRepoDescriptor descriptor,
            VirtualRepositoryConfigModel model) {
        // General
        GeneralRepositoryConfigModel general = new GeneralRepositoryConfigModel();
        general.setRepoKey(descriptor.getKey());
        model.setGeneral(general);

        // Basic
        VirtualBasicRepositoryConfigModel basic = new VirtualBasicRepositoryConfigModel();
        Optional.ofNullable(descriptor.getRepoLayout()).ifPresent(layout -> basic.setLayout(layout.getName()));
        basic.setPublicDescription(descriptor.getDescription());
        basic.setInternalDescription(descriptor.getNotes());
        basic.setExcludesPattern(descriptor.getExcludesPattern());
        basic.setIncludesPattern(descriptor.getIncludesPattern());
        List<RepoDescriptor> repositories = descriptor.getRepositories();
        VirtualRepoResolver resolver = new VirtualRepoResolver(descriptor);
        basic.setResolvedRepositories(resolver.getOrderedRepos().stream().map(VirtualSelectedRepository::new).collect(Collectors.toList()));
        basic.setSelectedRepositories(repositories.stream().map(VirtualSelectedRepository::new).collect(Collectors.toList()));
        Optional.ofNullable(descriptor.getDefaultDeploymentRepo())
                .ifPresent(localRepoDescriptor -> basic.setDefaultDeploymentRepo(localRepoDescriptor.getKey()));
        model.setBasic(basic);

        // Advanced
        VirtualAdvancedRepositoryConfigModel advanced = new VirtualAdvancedRepositoryConfigModel();
        advanced.setRetrieveRemoteArtifacts(descriptor.isArtifactoryRequestsCanRetrieveRemoteArtifacts());
        addReverseProxyConfig(advanced, descriptor.getKey());
        model.setAdvanced(advanced);

        // Type specific
        TypeSpecificConfigModel typeSpecific = createVirtualTypeSpecific(descriptor.getType(), descriptor);
        model.setTypeSpecific(typeSpecific);
    }

    /**
     * Populate model configuration from local repository descriptor
     */
    public void populateDistributionDescriptorValuesToModel(DistributionRepoDescriptor descriptor,
            DistributionRepositoryConfigModel model) {
        GeneralRepositoryConfigModel general = createGeneralConfig(descriptor);
        DistributionBasicRepositoryConfigModel basic = createDistBasicConfig(descriptor);
        DistributionAdvancedRepositoryConfigModel advanced = createDistAdvancedConfig(descriptor);
        DistRepoTypeSpecificConfigModel typeSpecific = new DistRepoTypeSpecificConfigModel();
        populateDistributionValues(typeSpecific, descriptor);
        model.setGeneral(general);
        model.setBasic(basic);
        model.setAdvanced(advanced);
        model.setTypeSpecific(typeSpecific);
    }

    /**
     * Populate model configuration from local repository descriptor
     */
    public void populateReleaseBundlesDescriptorValuesToModel(ReleaseBundlesRepoDescriptor descriptor, ReleaseBundlesRepositoryConfigModel model) {
        GeneralRepositoryConfigModel general = createGeneralConfig(descriptor);
        ReleaseBundlesBasicRepositoryConfigModel basic = createReleaseBundlesBasicConfig(descriptor);
        ReleaseBundlesAdvancedRepositoryConfigModel advanced = createReleaseBundlesAdvancedConfig(descriptor);
        ReleaseBundlesRepoTypeSpecificConfigModel typeSpecific = new ReleaseBundlesRepoTypeSpecificConfigModel();
        model.setGeneral(general);
        model.setBasic(basic);
        model.setAdvanced(advanced);
        model.setTypeSpecific(typeSpecific);
    }

    private LocalReplicationConfigModel createLocalReplicationConfig(LocalReplicationDescriptor replicationDescriptor) {
        LocalReplicationConfigModel replication = new LocalReplicationConfigModel();
        replication.setUrl(replicationDescriptor.getUrl());
        replication.setCronExp(replicationDescriptor.getCronExp());
        replication.setEnableEventReplication(replicationDescriptor.isEnableEventReplication());
        replication.setEnabled(replicationDescriptor.isEnabled());

        ProxyDescriptor proxyDescriptor = replicationDescriptor.getProxy();
        if (proxyDescriptor != null) {
            replication.setProxy(proxyDescriptor.getKey());
        }
        replication.setSocketTimeout(replicationDescriptor.getSocketTimeoutMillis());
        replication.setSyncDeletes(replicationDescriptor.isSyncDeletes());
        replication.setSyncProperties(replicationDescriptor.isSyncProperties());
        replication.setSyncStatistics(replicationDescriptor.isSyncStatistics());
        replication.setCheckBinaryExistenceInFilestore(replicationDescriptor.isCheckBinaryExistenceInFilestore());
        replication.setUsername(replicationDescriptor.getUsername());
        replication.setPassword(replicationDescriptor.getPassword());
        replication.setPathPrefix(replicationDescriptor.getPathPrefix());
        return replication;
    }

    private RemoteReplicationConfigModel createRemoteReplicationConfigModel(
            RemoteReplicationDescriptor replicationDescriptor) {
        RemoteReplicationConfigModel replication = new RemoteReplicationConfigModel();
        replication.setEnabled(replicationDescriptor.isEnabled());
        replication.setCronExp(replicationDescriptor.getCronExp());
        replication.setSyncDeletes(replicationDescriptor.isSyncDeletes());
        replication.setSyncProperties(replicationDescriptor.isSyncProperties());
        replication.setEnableEventReplication(replicationDescriptor.isEnableEventReplication());
        replication.setPathPrefix(replicationDescriptor.getPathPrefix());
        return replication;
    }

    private GeneralRepositoryConfigModel createGeneralConfig(RepoBaseDescriptor descriptor) {
        GeneralRepositoryConfigModel general = new GeneralRepositoryConfigModel();
        general.setRepoKey(descriptor.getKey());
        return general;
    }

    private LocalBasicRepositoryConfigModel createLocalBasicConfig(LocalRepoDescriptor descriptor) {
        LocalBasicRepositoryConfigModel basic = new LocalBasicRepositoryConfigModel();
        addSharedBasicConfigModel(basic, descriptor);
        basic.setXrayConfig(getXrayConfig(descriptor.getXrayConfig()));
        return basic;
    }

    private RemoteBasicRepositoryConfigModel createRemoteBasicConfig(HttpRepoDescriptor descriptor) {
        RemoteBasicRepositoryConfigModel basic = new RemoteBasicRepositoryConfigModel();
        addSharedBasicConfigModel(basic, descriptor);
        basic.setUrl(descriptor.getUrl());
        basic.setXrayConfig(getXrayConfig(descriptor.getXrayConfig()));
        basic.setContentSynchronisation(descriptor.getContentSynchronisation());
        RepoLayout remoteRepoLayout = descriptor.getRemoteRepoLayout();
        if (remoteRepoLayout != null) {
            basic.setRemoteLayoutMapping(remoteRepoLayout.getName());
        }
        basic.setOffline(descriptor.isOffline());
        return basic;
    }

    private DistributionBasicRepositoryConfigModel createDistBasicConfig(DistributionRepoDescriptor descriptor) {
        DistributionBasicRepositoryConfigModel basic = new DistributionBasicRepositoryConfigModel();
        addSharedBasicConfigModel(basic, descriptor);
        basic.setProductName(descriptor.getProductName());
        basic.setDefaultNewRepoPrivate(descriptor.isDefaultNewRepoPrivate());
        basic.setDefaultNewRepoPremium(descriptor.isDefaultNewRepoPremium());
        basic.setDefaultLicenses(descriptor.getDefaultLicenses());
        basic.setDefaultVcsUrl(descriptor.getDefaultVcsUrl());
        return basic;
    }

    private ReleaseBundlesBasicRepositoryConfigModel createReleaseBundlesBasicConfig(ReleaseBundlesRepoDescriptor descriptor) {
        ReleaseBundlesBasicRepositoryConfigModel basic = new ReleaseBundlesBasicRepositoryConfigModel();
        addSharedBasicConfigModel(basic, descriptor);
        return basic;
    }

    private void addSharedBasicConfigModel(BasicRepositoryConfigModel basic, RepoDescriptor descriptor) {
        basic.setPublicDescription(descriptor.getDescription());
        basic.setInternalDescription(descriptor.getNotes());
        basic.setIncludesPattern(descriptor.getIncludesPattern());
        basic.setExcludesPattern(descriptor.getExcludesPattern());
        Optional.ofNullable(descriptor.getRepoLayout()).ifPresent(repoLayout -> basic.setLayout(repoLayout.getName()));
    }

    private LocalAdvancedRepositoryConfigModel createLocalAdvancedConfig(LocalRepoDescriptor descriptor) {
        LocalAdvancedRepositoryConfigModel advanced = new LocalAdvancedRepositoryConfigModel();
        addSharedAdvancedConfigModel(advanced, descriptor);
        advanced.setDownloadRedirectConfig(getDownloadRedirectConfig(descriptor.getDownloadRedirectConfig()));
        return advanced;
    }


    private RemoteAdvancedRepositoryConfigModel createRemoteAdvancedConfig(HttpRepoDescriptor descriptor) {
        RemoteAdvancedRepositoryConfigModel advanced = new RemoteAdvancedRepositoryConfigModel();
        addSharedAdvancedConfigModel(advanced, descriptor);
        advanced.setDownloadRedirectConfig(getDownloadRedirectConfig(descriptor.getDownloadRedirectConfig()));
        RemoteNetworkRepositoryConfigModel networkModel = createNetworkConfig(descriptor);
        advanced.setNetwork(networkModel);
        RemoteCacheRepositoryConfigModel cacheConfig = createCacheConfig(descriptor);
        advanced.setCache(cacheConfig);
        advanced.setStoreArtifactsLocally(descriptor.isStoreArtifactsLocally());
        advanced.setSynchronizeArtifactProperties(descriptor.isSynchronizeProperties());
        advanced.setHardFail(descriptor.isHardFail());
        advanced.setQueryParams(descriptor.getQueryParams());
        advanced.setShareConfiguration(descriptor.isShareConfiguration());
        advanced.setBlockMismatchingMimeTypes(descriptor.isBlockMismatchingMimeTypes());
        advanced.setBypassHeadRequests(descriptor.isBypassHeadRequests());
        if (StringUtils.isNotBlank(descriptor.getMismatchingMimeTypesOverrideList())) {
            advanced.setMismatchingMimeTypesOverrideList(
                    Lists.newArrayList(descriptor.getMismatchingMimeTypesOverrideList().split(",")));
        }
        return advanced;
    }

    private DistributionAdvancedRepositoryConfigModel createDistAdvancedConfig(DistributionRepoDescriptor descriptor) {
        DistributionAdvancedRepositoryConfigModel advanced = new DistributionAdvancedRepositoryConfigModel();
        addSharedAdvancedConfigModel(advanced, descriptor);
        advanced.setDistributionRules(descriptor.getRules());
        if (descriptor.getProxy() != null) {
            advanced.setProxy(descriptor.getProxy().getKey());
        }
        advanced.setWhiteListedProperties(descriptor.getWhiteListedProperties());
        advanced.setGpgSign(descriptor.isGpgSign());
        advanced.setGpgPassPhrase(descriptor.getGpgPassPhrase());
        return advanced;
    }

    private ReleaseBundlesAdvancedRepositoryConfigModel createReleaseBundlesAdvancedConfig(LocalRepoDescriptor descriptor) {
        ReleaseBundlesAdvancedRepositoryConfigModel advanced = new ReleaseBundlesAdvancedRepositoryConfigModel();
        addSharedAdvancedConfigModel(advanced, descriptor);
        return advanced;
    }

    private void addSharedAdvancedConfigModel(AdvancedRepositoryConfigModel advanced, RealRepoDescriptor descriptor) {
        advanced.setAllowContentBrowsing(descriptor.isArchiveBrowsingEnabled());
        advanced.setBlackedOut(descriptor.isBlackedOut());
        List<PropertySet> propertySetsList = descriptor.getPropertySets();
        List<PropertySetNameModel> propertySetNameModelList = collectPropertySets(propertySetsList);
        advanced.setPropertySets(propertySetNameModelList);
        addReverseProxyConfig(advanced, descriptor.getKey());
    }

    private RemoteCacheRepositoryConfigModel createCacheConfig(HttpRepoDescriptor descriptor) {
        RemoteCacheRepositoryConfigModel cacheConfig = new RemoteCacheRepositoryConfigModel();
        cacheConfig.setKeepUnusedArtifactsHours(descriptor.getUnusedArtifactsCleanupPeriodHours());
        cacheConfig.setRetrievalCachePeriodSecs(descriptor.getRetrievalCachePeriodSecs());
        cacheConfig.setAssumedOfflineLimitSecs(descriptor.getAssumedOfflinePeriodSecs());
        cacheConfig.setMissedRetrievalCachePeriodSecs(descriptor.getMissedRetrievalCachePeriodSecs());
        return cacheConfig;
    }

    private RemoteNetworkRepositoryConfigModel createNetworkConfig(HttpRepoDescriptor descriptor) {
        RemoteNetworkRepositoryConfigModel networkModel = new RemoteNetworkRepositoryConfigModel();
        if (descriptor.getProxy() != null) {
            networkModel.setProxy(descriptor.getProxy().getKey());
        }
        networkModel.setLocalAddress(descriptor.getLocalAddress());
        networkModel.setUsername(descriptor.getUsername());
        networkModel.setPassword(descriptor.getPassword());
        networkModel.setSocketTimeout(descriptor.getSocketTimeoutMillis());
        networkModel.setLenientHostAuth(descriptor.isAllowAnyHostAuth());
        networkModel.setCookieManagement(descriptor.isEnableCookieManagement());
        networkModel.setSyncProperties(descriptor.isSynchronizeProperties());
        String clientTleCertificate = descriptor.getClientTlsCertificate();
        if (StringUtils.isNotBlank(clientTleCertificate) && clientTleCertificate.contains(ArtifactWebstartAddon.SSL_CERT_ALIAS_PREFIX)) {
            clientTleCertificate = clientTleCertificate.substring(ArtifactWebstartAddon.SSL_CERT_ALIAS_PREFIX.length());
        }
        networkModel.setSelectedInstalledCertificate(clientTleCertificate);
        ArtifactWebstartAddon webstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
        List<String> certAliases = webstartAddon.getSslCertNames();
        if (CollectionUtils.notNullOrEmpty(certAliases)) {
            networkModel.setInstalledCertificatesList(certAliases);
        }
        return networkModel;
    }

    private TypeSpecificConfigModel createLocalTypeSpecific(RepoType type, LocalRepoDescriptor descriptor) {
        MavenTypeSpecificConfigModel mavenModel = null;
        TypeSpecificConfigModel model = null;
        switch (type) {
            case Bower:
                model = new BowerTypeSpecificConfigModel();
                break;
            case Chef:
                model = new ChefTypeSpecificConfigModel();
                break;
            case CocoaPods:
                model = new CocoaPodsTypeSpecificConfigModel();
                break;
            case Composer:
                model = new ComposerTypeSpecificConfigModel();
                break;
            case Conan:
                model = new ConanTypeSpecificConfigModel();
                break;
            case Docker:
                DockerTypeSpecificConfigModel dockerType = new DockerTypeSpecificConfigModel();
                DockerApiVersion dockerApiVersion = descriptor.getDockerApiVersion();
                if (dockerApiVersion != null) {
                    dockerType.setDockerApiVersion(dockerApiVersion);
                }
                dockerType.setMaxUniqueTags(descriptor.getMaxUniqueTags());
                dockerType.setBlockPushingSchema1(descriptor.isBlockPushingSchema1());
                model = dockerType;
                break;
            case Helm:
                model = new HelmTypeSpecificConfigModel();
                break;
            case Go:
                model = new GoTypeSpecificConfigModel();
                break;
            case CRAN:
                model = new CranTypeSpecificConfigModel();
                break;
            case Conda:
                model = new CondaTypeSpecificConfigModel();
                break;
            case NuGet:
                NugetTypeSpecificConfigModel nugetType = new NugetTypeSpecificConfigModel();
                populateSharedNuGetValues(nugetType, descriptor);
                nugetType.setMaxUniqueSnapshots(descriptor.getMaxUniqueSnapshots());
                model = nugetType;
                break;
            case Npm:
                model = new NpmTypeSpecificConfigModel();
                break;
            case Pypi:
                model = new PypiTypeSpecificConfigModel();
                break;
            case Puppet:
                model = new PuppetTypeSpecificConfigModel();
                break;
            case Vagrant:
                model = new VagrantTypeSpecificConfigModel();
                break;
            case GitLfs:
                model = new GitLfsTypeSpecificConfigModel();
                break;
            case Debian:
                DebTypeSpecificConfigModel debType = new DebTypeSpecificConfigModel();
                debType.setTrivialLayout(descriptor.isDebianTrivialLayout());
                debType.setOptionalIndexCompressionFormats(descriptor.getOptionalIndexCompressionFormats());
                model = debType;
                break;
            case Opkg:
                model = new OpkgTypeSpecificConfigModel();
                break;
            case YUM:
                YumTypeSpecificConfigModel yumType = new YumTypeSpecificConfigModel();
                yumType.setMetadataFolderDepth(descriptor.getYumRootDepth());
                yumType.setGroupFileNames(descriptor.getYumGroupFileNames());
                yumType.setAutoCalculateYumMetadata(descriptor.isCalculateYumMetadata());
                yumType.setEnableFileListsIndexing(descriptor.isEnableFileListsIndexing());
                model = yumType;
                break;
            case Gems:
                model = new GemsTypeSpecificConfigModel();
                break;
            case Generic:
                model = new GenericTypeSpecificConfigModel();
                break;
            case Maven:
                mavenModel = new MavenTypeSpecificConfigModel();
                break;
            case Gradle:
                mavenModel = new GradleTypeSpecificConfigModel();
                break;
            case Ivy:
                mavenModel = new IvyTypeSpecificConfigModel();
                break;
            case SBT:
                mavenModel = new SbtTypeSpecificConfigModel();
                break;
        }
        if (model != null) {
            return model;
        }
        // We will get here only if our model is maven / gradle / ivy / sbt and we populate the values
        populateMavenLocalValues(mavenModel, descriptor);
        return mavenModel;
    }

    private TypeSpecificConfigModel createRemoteTypeSpecific(RepoType type, HttpRepoDescriptor descriptor) {
        MavenTypeSpecificConfigModel mavenModel = null;
        TypeSpecificConfigModel model = null;
        switch (type) {
            case Maven:
                mavenModel = new MavenTypeSpecificConfigModel();
                break;
            case Gradle:
                mavenModel = new GradleTypeSpecificConfigModel();
                break;
            case Ivy:
                mavenModel = new IvyTypeSpecificConfigModel();
                break;
            case SBT:
                mavenModel = new SbtTypeSpecificConfigModel();
                break;
            case P2:
                mavenModel = new P2TypeSpecificConfigModel();
                break;
            case Debian:
                DebTypeSpecificConfigModel debType = new DebTypeSpecificConfigModel();
                debType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = debType;
                break;
            case Opkg:
                OpkgTypeSpecificConfigModel opkgType = new OpkgTypeSpecificConfigModel();
                opkgType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = opkgType;
                break;
            case Docker:
                DockerTypeSpecificConfigModel dockerType = new DockerTypeSpecificConfigModel();
                dockerType.setEnableTokenAuthentication(descriptor.isEnableTokenAuthentication());
                dockerType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                ExternalDependenciesConfig dockerExternalDependencies = descriptor.getExternalDependencies();
                if (dockerExternalDependencies != null) {
                    dockerType.setEnableForeignLayersCaching(dockerExternalDependencies.isEnabled());
                    dockerType.setExternalPatterns(dockerExternalDependencies.getPatterns());
                }
                dockerType.setBlockPushingSchema1(descriptor.isBlockPushingSchema1());
                model = dockerType;
                break;
            case NuGet:
                NugetTypeSpecificConfigModel nugetType = new NugetTypeSpecificConfigModel();
                populateSharedNuGetValues(nugetType, descriptor);
                NuGetConfiguration nuget = descriptor.getNuget();
                if (nuget != null) {
                    nugetType.setDownloadContextPath(nuget.getDownloadContextPath());
                    nugetType.setFeedContextPath(nuget.getFeedContextPath());
                    nugetType.setV3FeedUrl(nuget.getV3FeedUrl());
                }
                nugetType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = nugetType;
                break;
            case Npm:
                NpmTypeSpecificConfigModel npmType = new NpmTypeSpecificConfigModel();
                npmType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = npmType;
                break;
            case Pypi:
                PypiTypeSpecificConfigModel pypiType = new PypiTypeSpecificConfigModel();
                pypiType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                PypiConfiguration pyPIConfiguration = descriptor.getPypi();
                if (pyPIConfiguration != null) {
                    pypiType.setRegistryUrl(pyPIConfiguration.getPyPIRegistryUrl());
                    pypiType.setRepositorySuffix(pyPIConfiguration.getRepositorySuffix());
                }
                model = pypiType;
                break;
            case Puppet:
                PuppetTypeSpecificConfigModel puppetType = new PuppetTypeSpecificConfigModel();
                puppetType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = puppetType;
                break;
            case Chef:
                ChefTypeSpecificConfigModel chefType = new ChefTypeSpecificConfigModel();
                chefType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = chefType;
                break;
            case VCS:
                VcsTypeSpecificConfigModel vcsType = new VcsTypeSpecificConfigModel();
                populateVcsValues(vcsType, descriptor);
                model = vcsType;
                break;
            case Bower:
                BowerTypeSpecificConfigModel bowerType = new BowerTypeSpecificConfigModel();
                populateVcsValues(bowerType, descriptor);
                BowerConfiguration bowerConfiguration = descriptor.getBower();
                if (bowerConfiguration != null) {
                    bowerType.setRegistryUrl(bowerConfiguration.getBowerRegistryUrl());
                }
                model = bowerType;
                break;
            case Go:
                GoTypeSpecificConfigModel goType = new GoTypeSpecificConfigModel();
                populateVcsValues(goType, descriptor);
                model = goType;
                break;
            case CRAN:
                model = new CranTypeSpecificConfigModel();
                break;
            case Conan:
                model = new ConanTypeSpecificConfigModel();
                break;
            case Conda:
                model = new CondaTypeSpecificConfigModel();
                break;
            case CocoaPods:
                CocoaPodsTypeSpecificConfigModel podsType = new CocoaPodsTypeSpecificConfigModel();
                populateVcsValues(podsType, descriptor);
                CocoaPodsConfiguration podsConfiguration = descriptor.getCocoaPods();
                if (podsConfiguration != null) {
                    podsType.setSpecsRepoUrl(podsConfiguration.getCocoaPodsSpecsRepoUrl());
                    podsType.setSpecsRepoProvider(podsConfiguration.getSpecRepoProvider());
                }
                model = podsType;
                break;
            case Gems:
                GemsTypeSpecificConfigModel gemsType = new GemsTypeSpecificConfigModel();
                gemsType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = gemsType;
                break;
            case Generic:
                GenericTypeSpecificConfigModel genericType = new GenericTypeSpecificConfigModel();
                genericType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = genericType;
                break;
            case YUM:
                YumTypeSpecificConfigModel yumType = new YumTypeSpecificConfigModel();
                yumType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                yumType.setAutoCalculateYumMetadata(null);
                yumType.setMetadataFolderDepth(null);
                yumType.setGroupFileNames(null);
                model = yumType;
                break;
            case GitLfs:
                GitLfsTypeSpecificConfigModel gitLfsType = new GitLfsTypeSpecificConfigModel();
                gitLfsType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = gitLfsType;
                break;
            case Helm:
                model = new HelmTypeSpecificConfigModel();
                break;
            case Composer:
                ComposerTypeSpecificConfigModel composerType = new ComposerTypeSpecificConfigModel();
                populateVcsValues(composerType, descriptor);
                ComposerConfiguration composerConfiguration = descriptor.getComposer();
                if (composerConfiguration != null) {
                    composerType.setRegistryUrl(composerConfiguration.getComposerRegistryUrl());
                }
                model = composerType;
                break;
        }
        if (model != null) {
            return model;
        }
        populateMavenRemoteValues(mavenModel, descriptor);
        return mavenModel;
    }

    private TypeSpecificConfigModel createVirtualTypeSpecific(RepoType type, VirtualRepoDescriptor descriptor) {
        TypeSpecificConfigModel typeSpecific = null;
        MavenTypeSpecificConfigModel mavenModel = null;
        switch (type) {
            case Maven:
                mavenModel = new MavenTypeSpecificConfigModel();
                mavenModel.setForceMavenAuthentication(descriptor.isForceMavenAuthentication());
                break;
            case Chef:
                ChefTypeSpecificConfigModel chef = new ChefTypeSpecificConfigModel();
                if (descriptor.getVirtualCacheConfig() != null) {
                    chef.setVirtualRetrievalCachePeriodSecs(descriptor.getVirtualCacheConfig()
                            .getVirtualRetrievalCachePeriodSecs());
                }
                typeSpecific = chef;
                break;
            case Gradle:
                mavenModel = new GradleTypeSpecificConfigModel();
                break;
            case Go:
                GoTypeSpecificConfigModel goModel = new GoTypeSpecificConfigModel();
                ExternalDependenciesConfig goExternalDependencies = descriptor.getExternalDependencies();
                if (goExternalDependencies != null) {
                    goModel.setEnableExternalDependencies(goExternalDependencies.isEnabled());
                    goModel.setExternalPatterns(goExternalDependencies.getPatterns());
                }
                typeSpecific = goModel;
                break;
            case CRAN:
                CranTypeSpecificConfigModel cranTypeSpecificConfigModel = new CranTypeSpecificConfigModel();
                if (descriptor.getVirtualCacheConfig() != null) {
                    cranTypeSpecificConfigModel.setVirtualRetrievalCachePeriodSecs(descriptor.getVirtualCacheConfig()
                            .getVirtualRetrievalCachePeriodSecs());
                }
                typeSpecific = cranTypeSpecificConfigModel;
                break;
            case Conan:
                ConanTypeSpecificConfigModel conanTypeSpecificConfigModel = new ConanTypeSpecificConfigModel();
                if (descriptor.getVirtualCacheConfig() != null) {
                    conanTypeSpecificConfigModel.setVirtualRetrievalCachePeriodSecs(descriptor.getVirtualCacheConfig()
                            .getVirtualRetrievalCachePeriodSecs());
                }
                typeSpecific = conanTypeSpecificConfigModel;
                break;
            case Conda:
                CondaTypeSpecificConfigModel condaTypeSpecificConfigModel = new CondaTypeSpecificConfigModel();
                if (descriptor.getVirtualCacheConfig() != null) {
                    condaTypeSpecificConfigModel.setVirtualRetrievalCachePeriodSecs(descriptor.getVirtualCacheConfig()
                            .getVirtualRetrievalCachePeriodSecs());
                }
                typeSpecific = condaTypeSpecificConfigModel;
                break;
            case Debian:
                DebTypeSpecificConfigModel debianTypeSpecificConfigModel = new DebTypeSpecificConfigModel();
                if (descriptor.getVirtualCacheConfig() != null) {
                    debianTypeSpecificConfigModel.setVirtualRetrievalCachePeriodSecs(descriptor.getVirtualCacheConfig()
                            .getVirtualRetrievalCachePeriodSecs());
                    debianTypeSpecificConfigModel.setVirtualArchitectures(Arrays.asList(descriptor.getDebianDefaultArchitectures().split(",")));
                    debianTypeSpecificConfigModel.setOptionalIndexCompressionFormats(descriptor.getDebianOptionalIndexCompressionFormats());
                }
                typeSpecific = debianTypeSpecificConfigModel;
                break;
            case Helm:
                HelmTypeSpecificConfigModel helm = new HelmTypeSpecificConfigModel();
                if (descriptor.getVirtualCacheConfig() != null) {
                    helm.setVirtualRetrievalCachePeriodSecs(descriptor.getVirtualCacheConfig()
                            .getVirtualRetrievalCachePeriodSecs());
                }
                typeSpecific = helm;
                break;
            case Ivy:
                mavenModel = new IvyTypeSpecificConfigModel();
                break;
            case SBT:
                mavenModel = new SbtTypeSpecificConfigModel();
                break;
            case P2:
                P2TypeSpecificConfigModel p2 = new P2TypeSpecificConfigModel();
                populateVirtualP2Values(p2, descriptor);
                mavenModel = p2;
                break;
            case Gems:
                typeSpecific = new GemsTypeSpecificConfigModel();
                break;
            case GitLfs:
                typeSpecific = new GitLfsTypeSpecificConfigModel();
                break;
            case Npm:
                NpmTypeSpecificConfigModel npmType = new NpmTypeSpecificConfigModel();
                ExternalDependenciesConfig npmExternalDependencies = descriptor.getExternalDependencies();
                if (npmExternalDependencies != null) {
                    npmType.setEnableExternalDependencies(npmExternalDependencies.isEnabled());
                    if (npmExternalDependencies.getRemoteRepo() != null) {
                        npmType.setExternalRemoteRepo(npmExternalDependencies.getRemoteRepo().getKey());
                    }
                    npmType.setExternalPatterns(npmExternalDependencies.getPatterns());
                }
                if (descriptor.getVirtualCacheConfig() != null) {
                    npmType.setVirtualRetrievalCachePeriodSecs(descriptor.getVirtualCacheConfig()
                            .getVirtualRetrievalCachePeriodSecs());
                }
                typeSpecific = npmType;
                break;
            case Bower:
                BowerTypeSpecificConfigModel bowerType = new BowerTypeSpecificConfigModel();
                ExternalDependenciesConfig bowerExternalDependencies = descriptor.getExternalDependencies();
                if (bowerExternalDependencies != null) {
                    bowerType.setEnableExternalDependencies(bowerExternalDependencies.isEnabled());
                    if (bowerExternalDependencies.getRemoteRepo() != null) {
                        bowerType.setExternalRemoteRepo(bowerExternalDependencies.getRemoteRepo().getKey());
                    }
                    bowerType.setExternalPatterns(bowerExternalDependencies.getPatterns());
                }
                typeSpecific = bowerType;
                break;
            case NuGet:
                typeSpecific = new NugetTypeSpecificConfigModel();
                populateSharedNuGetValues((NugetTypeSpecificConfigModel) typeSpecific, descriptor);
                break;
            case Pypi:
                typeSpecific = new PypiTypeSpecificConfigModel();
                break;
            case Puppet:
                typeSpecific = new PuppetTypeSpecificConfigModel();
                break;
            case Docker:
                DockerTypeSpecificConfigModel dockerType = new DockerTypeSpecificConfigModel();
                dockerType.setResolveDockerTagsByTimestamp(descriptor.isResolveDockerTagsByTimestamp());
                typeSpecific = dockerType;
                break;
            case YUM:
                YumTypeSpecificConfigModel yum = new YumTypeSpecificConfigModel();
                if (descriptor.getVirtualCacheConfig() != null) {
                    yum.setVirtualRetrievalCachePeriodSecs(descriptor.getVirtualCacheConfig()
                            .getVirtualRetrievalCachePeriodSecs());
                }
                typeSpecific = yum;
                break;
            case Generic:
                typeSpecific = new GenericTypeSpecificConfigModel();
                break;
        }
        if (typeSpecific != null) {
            return typeSpecific;
        }
        populateMavenVirtualValues(mavenModel, descriptor);
        return mavenModel;
    }

    private void populateVcsValues(VcsTypeSpecificConfigModel vcsType, HttpRepoDescriptor descriptor) {
        vcsType.setMaxUniqueSnapshots(descriptor.getMaxUniqueSnapshots());
        VcsConfiguration vcsConf = descriptor.getVcs();
        vcsType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
        if (vcsConf != null) {
            vcsType.setVcsType(vcsConf.getType());
            VcsGitConfiguration git = vcsConf.getGit();
            if (git != null) {
                vcsType.setGitProvider(git.getProvider());
                vcsType.setDownloadUrl(git.getDownloadUrl());
            }
        }
    }

    private void populateSharedMavenValues(MavenTypeSpecificConfigModel model, RealRepoDescriptor descriptor) {
        model.setMaxUniqueSnapshots(descriptor.getMaxUniqueSnapshots());
        model.setHandleReleases(descriptor.isHandleReleases());
        model.setHandleSnapshots(descriptor.isHandleSnapshots());
        model.setSuppressPomConsistencyChecks(descriptor.isSuppressPomConsistencyChecks());
    }

    private void populateMavenLocalValues(MavenTypeSpecificConfigModel model, LocalRepoDescriptor descriptor) {
        populateSharedMavenValues(model, descriptor);
        model.setSnapshotVersionBehavior(descriptor.getSnapshotVersionBehavior());
        model.setLocalChecksumPolicy(descriptor.getChecksumPolicyType());
    }

    private void populateMavenRemoteValues(MavenTypeSpecificConfigModel model, HttpRepoDescriptor descriptor) {
        populateSharedMavenValues(model, descriptor);
        model.setEagerlyFetchJars(descriptor.isFetchJarsEagerly());
        model.setEagerlyFetchSources(descriptor.isFetchSourcesEagerly());
        model.setRemoteChecksumPolicy(descriptor.getChecksumPolicyType());
        model.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
        model.setRejectInvalidJars(descriptor.isRejectInvalidJars());
    }

    private void populateMavenVirtualValues(MavenTypeSpecificConfigModel model, VirtualRepoDescriptor descriptor) {
        model.setPomCleanupPolicy(descriptor.getPomRepositoryReferencesCleanupPolicy());
        model.setKeyPair(descriptor.getKeyPair());
    }

    private void populateVirtualP2Values(P2TypeSpecificConfigModel model, VirtualRepoDescriptor descriptor) {
        if (descriptor.getP2() == null || descriptor.getP2().getUrls() == null) {
            return;
        }
        Map<String, String> urlToRepoKeyMap = getUrlToRepoKeyMapping(descriptor.getRepositories());
        List<P2Repo> p2Repos = Lists.newArrayList();
        descriptor.getP2().getUrls().forEach(url -> {
            if (StringUtils.startsWith(url, "local://")) {
                Optional.ofNullable(resolveLocalP2RepoFromUrl(url)).ifPresent(p2Repos::add);
            } else {
                urlToRepoKeyMap.keySet().stream()
                        .map(RepoConfigDescriptorBuilder::getUrlWithoutSubpath)
                        .filter(p2Url -> RepoConfigDescriptorBuilder.getUrlWithoutSubpath(url).equals(p2Url))
                        .findAny()
                        .ifPresent(containingUrl ->
                                p2Repos.add(new P2Repo(null, urlToRepoKeyMap.get(containingUrl), url)));
            }
        });
        model.setP2Repos(p2Repos);
    }

    private P2Repo resolveLocalP2RepoFromUrl(String url) {
        String rpp = StringUtils.removeStart(url, "local://");
        //rpp = rpp.substring(0, rpp.indexOf('/'));
        RepoPath repoPath = RepoPathFactory.create(rpp);
        LocalRepoDescriptor localRepoDescriptor = centralConfig.getMutableDescriptor().getLocalRepositoriesMap()
                .get(repoPath.getRepoKey());
        if (localRepoDescriptor != null) {
            return new P2Repo(null, repoPath.getRepoKey(), url);
        }
        return null;
    }

    private List<PropertySetNameModel> collectPropertySets(List<PropertySet> propertySetsList) {
        return propertySetsList.stream()
                .map(PropertySetNameModel::new)
                .collect(Collectors.toList());
    }

    /**
     * Creates a mapping of url -> remote repo key to help build the P2 model (maps 'maven group' repos only)
     */
    private Map<String, String> getUrlToRepoKeyMapping(List<RepoDescriptor> descriptors) {
        ConcurrentMap<String, String> mapping = Maps.newConcurrentMap();
        descriptors.stream()
                .filter(repoDescriptor -> repoDescriptor instanceof HttpRepoDescriptor)
                .filter(repoDescriptor -> repoDescriptor.getType().isMavenGroup())
                .forEach(remoteDescriptor ->
                        mapping.put(((HttpRepoDescriptor) remoteDescriptor).getUrl(), remoteDescriptor.getKey()));
        return mapping;
    }

    private void populateSharedNuGetValues(NugetTypeSpecificConfigModel nuget, RepoBaseDescriptor descriptor) {
        nuget.setForceNugetAuthentication(descriptor.isForceNugetAuthentication());
    }

    private void populateDistributionValues(DistRepoTypeSpecificConfigModel dist,
            DistributionRepoDescriptor descriptor) {
        if (descriptor.getBintrayApplication() == null) {
            log.error("Repository {} is missing it's Bintray OAuth config.", descriptor.getKey());
            return;
        }
        BintrayApplicationConfig appConfig = descriptor.getBintrayApplication();
        dist.setBintrayAppConfig(appConfig.getKey());
        dist.setClientId(CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), appConfig.getClientId()));
        dist.setOrg(appConfig.getOrg());
        try {
            BintrayOAuthAppConfigurator appConfigurator = ContextHelper.get().beanForType(BintrayOAuthAppConfigurator.class);
            DistributionRepoCreationDetails orgDetails = appConfigurator.getRepoCreationDetails(descriptor.getKey());
            if (orgDetails != null) {
                dist.setPremium(orgDetails.isOrgPremium);
                dist.setAvailableLicenses(orgDetails.orgLicenses);
            }
        } catch (Exception e) {
            String err = "Error retrieving Bintray org-specific data for repo wizard: ";
            log.error(err + ExceptionUtils.getRootCause(e).getMessage(), e);
            Throwable bintrayException = ExceptionUtils.getCauseOfType(e, BintrayCallException.class);
            if (bintrayException != null) {
                int statusCode = ((BintrayCallException) bintrayException).getStatusCode();
                if (statusCode == 401 || statusCode == 403) {
                    dist.setAuthenticated(false);
                }
            }
        }
    }

    private XrayRepoConfigModel getXrayConfig(XrayRepoConfig xrayRepoConfig) {
        XrayRepoConfigModel model = null;
        if (xrayRepoConfig != null) {
            model = new XrayRepoConfigModel();
            model.setEnabled(xrayRepoConfig.isEnabled());
        }
        return model;
    }

    private DownloadRedirectRepoConfigModel getDownloadRedirectConfig(DownloadRedirectRepoConfig downloadRedirectRepoConfig) {
        DownloadRedirectRepoConfigModel model = null;
        if (downloadRedirectRepoConfig != null) {
            model = new DownloadRedirectRepoConfigModel();
            model.setEnabled(downloadRedirectRepoConfig.isEnabled());
        }
        return model;
    }


    private void addReverseProxyConfig(AdvancedRepositoryConfigModel advanced, String repoKey) {
        CentralConfigDescriptor configDescriptor = centralConfig.getDescriptor();
        ReverseProxyDescriptor reverseProxyDescriptor = configDescriptor.getCurrentReverseProxy();
        if (reverseProxyDescriptor != null) {
            ReverseProxyRepoConfig reverseProxyRepoConfig = reverseProxyDescriptor.getReverseProxyRepoConfig(repoKey);
            advanced.setReverseProxy(reverseProxyToModel(reverseProxyRepoConfig, reverseProxyDescriptor));
        }
    }

    private ReverseProxyRepoModel reverseProxyToModel(ReverseProxyRepoConfig reverseProxy,
            ReverseProxyDescriptor reverseProxyDescriptor) {
        if (reverseProxy != null) {
            ReverseProxyRepoModel model = new ReverseProxyRepoModel();
            model.setConfigurationKey(reverseProxyDescriptor.getKey());
            model.setServerName(reverseProxy.getServerName());
            model.setServerPort(reverseProxy.getPort());
            return model;
        }
        return null;
    }

    @Autowired
    public void setCentralConfig(CentralConfigService centralConfig) {
        this.centralConfig = centralConfig;
    }

    @Autowired
    public void setAddonsManager(AddonsManager addonsManager) {
        this.addonsManager = addonsManager;
    }
}
