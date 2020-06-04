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

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.addon.p2.P2Repo;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
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
import org.artifactory.ui.rest.model.admin.configuration.repository.releasebundles.ReleaseBundlesRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.*;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.*;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.xray.XrayRepoConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.reverseProxy.ReverseProxyRepoModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.RepoConfigValidator;
import org.jfrog.client.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility class for converting model to descriptor
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RepoConfigDescriptorBuilder {

    private CentralConfigService centralConfig;

    private RepoConfigValidator configValidator;

    @Autowired
    private AddonsManager addonsManager;

    public LocalRepoDescriptor buildLocalDescriptor(LocalRepositoryConfigModel model) {
        LocalRepoDescriptor descriptor = new LocalRepoDescriptor();
        populateSharedGeneralDescriptorValues(model.getGeneral(), descriptor);
        populateSharedBasicDescriptorValues(model.getBasic(), descriptor);
        buildAndSetXrayConfig(descriptor, model.getBasic());
        buildAndSetDownloadRedirectConfig(descriptor, model.getAdvanced());
        populateSharedAdvancedDescriptorValues(model.getAdvanced(), descriptor);
        populateSharedTypeSpecificDescriptorValues(model.getTypeSpecific(), descriptor);
        populateLocalTypeSpecificDescriptorValues(model.getTypeSpecific(), descriptor);
        return descriptor;
    }

    public HttpRepoDescriptor buildRemoteDescriptor(RemoteRepositoryConfigModel model) {
        HttpRepoDescriptor descriptor = new HttpRepoDescriptor();
        populateSharedGeneralDescriptorValues(model.getGeneral(), descriptor);
        populateSharedBasicDescriptorValues(model.getBasic(), descriptor);
        populateRemoteBasicDescriptorValues(model.getBasic(), descriptor);
        buildAndSetXrayConfig(descriptor, model.getBasic());
        buildAndSetDownloadRedirectConfig(descriptor, model.getAdvanced());
        populateSharedAdvancedDescriptorValues(model.getAdvanced(), descriptor);
        populateRemoteAdvancedDescriptorValues(model.getAdvanced(), descriptor);
        populateSharedTypeSpecificDescriptorValues(model.getTypeSpecific(), descriptor);
        populateRemoteTypeSpecificDescriptorValues(model.getTypeSpecific(), descriptor);
        return descriptor;
    }

    public VirtualRepoDescriptor buildVirtualDescriptor(VirtualRepositoryConfigModel model, MutableCentralConfigDescriptor configDescriptor) {
        VirtualRepoDescriptor descriptor = new VirtualRepoDescriptor();
        populateSharedGeneralDescriptorValues(model.getGeneral(), descriptor);
        populateSharedBasicDescriptorValues(model.getBasic(), descriptor);
        // if not validate resolve virtual values from config descriptor (instead of config service)
        populateVirtualBasicDescriptorValues(model.getBasic(), descriptor, configDescriptor);
        populateVirtualAdvancedDescriptorValues(model.getAdvanced(), descriptor);
        populateVirtualTypeSpecific(model.getTypeSpecific(), descriptor);
        return descriptor;
    }

    public DistributionRepoDescriptor buildDistributionDescriptor(DistributionRepositoryConfigModel model) {
        DistributionRepoDescriptor descriptor = new DistributionRepoDescriptor();
        populateSharedGeneralDescriptorValues(model.getGeneral(), descriptor);
        populateSharedBasicDescriptorValues(model.getBasic(), descriptor);
        populateDistributionBasicDescriptorValues(model.getBasic(), descriptor);
        populateSharedAdvancedDescriptorValues(model.getAdvanced(), descriptor);
        populateDistributionAdvancedDescriptorValues(model.getAdvanced(), descriptor);
        descriptor.setType(RepoType.Distribution);
        descriptor.setBintrayApplication(centralConfig.getDescriptor()
                .getBintrayApplication(model.getTypeSpecific().getBintrayAppConfig()));
        return descriptor;
    }

    public ReleaseBundlesRepoDescriptor buildReleaseBundlesDescriptor(ReleaseBundlesRepositoryConfigModel model) {
        ReleaseBundlesRepoDescriptor descriptor = new ReleaseBundlesRepoDescriptor();
        populateSharedGeneralDescriptorValues(model.getGeneral(), descriptor);
        populateSharedBasicDescriptorValues(model.getBasic(), descriptor);
        buildAndSetXrayConfig(descriptor, model.getBasic());
        populateSharedAdvancedDescriptorValues(model.getAdvanced(), descriptor);
        populateSharedTypeSpecificDescriptorValues(model.getTypeSpecific(), descriptor);
        populateLocalTypeSpecificDescriptorValues(model.getTypeSpecific(), descriptor);
        descriptor.setType(RepoType.ReleaseBundles);
        return descriptor;
    }

    private void populateDistributionBasicDescriptorValues(DistributionBasicRepositoryConfigModel basic,
            DistributionRepoDescriptor descriptor) {
        descriptor.setProductName(basic.getProductName());
        descriptor.setDefaultNewRepoPrivate(basic.getDefaultNewRepoPrivate());
        descriptor.setDefaultNewRepoPremium(basic.getDefaultNewRepoPremium());
        descriptor.setDefaultLicenses(basic.getDefaultLicenses());
        descriptor.setDefaultVcsUrl(basic.getDefaultVcsUrl());
    }

    private void populateDistributionAdvancedDescriptorValues(DistributionAdvancedRepositoryConfigModel advanced,
            DistributionRepoDescriptor descriptor) {
        descriptor.setRules(advanced.getDistributionRules());
        if (StringUtils.isNotBlank(advanced.getProxy())) {
            descriptor.setProxy(centralConfig.getDescriptor().getProxy(advanced.getProxy()));
        }
        descriptor.setWhiteListedProperties(advanced.getWhiteListedProperties());
        descriptor.setGpgSign(advanced.isGpgSign());
        descriptor.setGpgPassPhrase(advanced.getGpgPassPhrase());
    }

    private void populateSharedGeneralDescriptorValues(GeneralRepositoryConfigModel model,
            RepoBaseDescriptor descriptor) {
        descriptor.setKey(model.getRepoKey());
    }

    /**
     * Populates basic descriptor values that are shared between local and remote repos
     */
    private void populateSharedBasicDescriptorValues(BasicRepositoryConfigModel model, RepoBaseDescriptor descriptor) {
        descriptor.setDescription(model.getPublicDescription());
        descriptor.setNotes(model.getInternalDescription());
        descriptor.setIncludesPattern(model.getIncludesPattern());
        descriptor.setExcludesPattern(model.getExcludesPattern());
        if (StringUtils.isNotBlank(model.getLayout())) { //Don't enforce on virtual
            descriptor.setRepoLayout(centralConfig.getDescriptor().getRepoLayout(model.getLayout()));
        }
    }

    /**
     * Populates remote basic descriptor values
     */
    private void populateRemoteBasicDescriptorValues(RemoteBasicRepositoryConfigModel model,
            HttpRepoDescriptor descriptor) {
        descriptor.setUrl(model.getUrl());
        if (model.getRemoteLayoutMapping() != null) {
            descriptor.setRemoteRepoLayout(centralConfig.getDescriptor().getRepoLayout(model.getRemoteLayoutMapping()));
        }
        descriptor.setOffline(model.isOffline());
        descriptor.setContentSynchronisation(model.getContentSynchronisation());
    }

    private void populateVirtualBasicDescriptorValues(VirtualBasicRepositoryConfigModel model,
            VirtualRepoDescriptor descriptor, MutableCentralConfigDescriptor configDescriptor) {
        descriptor.setRepositories(model.getSelectedRepositories().stream()
                .map(repo -> configValidator.mapRepoKeyToDescriptor(repo, configDescriptor))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        if (StringUtils.isNotBlank(model.getDefaultDeploymentRepo())) {
            descriptor.setDefaultDeploymentRepo(configDescriptor.getLocalRepositoriesMap().get(model.getDefaultDeploymentRepo()));
        }
    }

    private void populateVirtualAdvancedDescriptorValues(VirtualAdvancedRepositoryConfigModel model,
            VirtualRepoDescriptor descriptor) {
        descriptor.setArtifactoryRequestsCanRetrieveRemoteArtifacts(model.getRetrieveRemoteArtifacts());
        buildReverseProxyDescriptor(model, descriptor);
    }

    /**
     * Populates advanced descriptor values that are shared between local and remote repos
     */
    private void populateSharedAdvancedDescriptorValues(AdvancedRepositoryConfigModel model,
            RealRepoDescriptor descriptor) {
        descriptor.setBlackedOut(model.isBlackedOut());
        descriptor.setArchiveBrowsingEnabled(model.getAllowContentBrowsing());
        List<PropertySetNameModel> propertySets = model.getPropertySets();
        if (propertySets != null) {
            descriptor.setPropertySets(model.getPropertySets().stream()
                    .map(propSet -> getPropSetByName(propSet.getName()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
        buildReverseProxyDescriptor(model, descriptor);
    }

    public ReverseProxyDescriptor buildReverseProxyDescriptor(AdvancedRepositoryConfigModel model,
            RepoBaseDescriptor descriptor){
        ReverseProxyRepoModel reverseProxy = model.getReverseProxy();
        MutableCentralConfigDescriptor mutableDescriptor = ContextHelper.get().getCentralConfig().getMutableDescriptor();
        ReverseProxyDescriptor reverseProxyDescriptor = mutableDescriptor.getCurrentReverseProxy();
        if (reverseProxy != null && reverseProxyDescriptor != null) {
            if (reverseProxy.getServerPort() == null) {
                reverseProxyDescriptor.deleteReverseProxyConfig(descriptor.getKey());
            } else {
                ReverseProxyRepoConfig reverseProxyRepoConfig = new ReverseProxyRepoConfig();
                reverseProxyRepoConfig.setServerName(reverseProxy.getServerName());
                reverseProxyRepoConfig.setPort(reverseProxy.getServerPort());
                reverseProxyRepoConfig.setRepoRef(descriptor);
                reverseProxyDescriptor.addReverseProxyRepoConfig(reverseProxyRepoConfig);
            }
        }
        return reverseProxyDescriptor;
    }

    /**
     * Populates remote advanced descriptor values
     */
    private void populateRemoteAdvancedDescriptorValues(RemoteAdvancedRepositoryConfigModel model,
            HttpRepoDescriptor descriptor) {
        //network
        RemoteNetworkRepositoryConfigModel network = model.getNetwork();
        if (network != null) {
            if(StringUtils.isNotBlank(network.getProxy())) {
                descriptor.setProxy(centralConfig.getDescriptor().getProxy(network.getProxy()));
            }
            descriptor.setLocalAddress(network.getLocalAddress());
            descriptor.setUsername(network.getUsername());
            descriptor.setPassword(CryptoHelper.encryptIfNeeded(ArtifactoryHome.get(), network.getPassword()));
            descriptor.setSocketTimeoutMillis(network.getSocketTimeout());
            descriptor.setAllowAnyHostAuth(network.getLenientHostAuth());
            descriptor.setEnableCookieManagement(network.getCookieManagement());
            descriptor.setClientTlsCertificate(network.getSelectedInstalledCertificate());
        }
        //cache
        RemoteCacheRepositoryConfigModel cache = model.getCache();
        if (cache != null) {
            descriptor.setUnusedArtifactsCleanupPeriodHours(cache.getKeepUnusedArtifactsHours());
            descriptor.setRetrievalCachePeriodSecs(cache.getRetrievalCachePeriodSecs());
            descriptor.setAssumedOfflinePeriodSecs(cache.getAssumedOfflineLimitSecs());
            descriptor.setMissedRetrievalCachePeriodSecs(cache.getMissedRetrievalCachePeriodSecs());
        }
        //other
        descriptor.setQueryParams(model.getQueryParams());
        descriptor.setPropagateQueryParams(model.isPropagateQueryParams());
        descriptor.setHardFail(model.getHardFail());
        descriptor.setStoreArtifactsLocally(model.isStoreArtifactsLocally());
        descriptor.setSynchronizeProperties(model.getSynchronizeArtifactProperties());
        descriptor.setShareConfiguration(model.isShareConfiguration());
        descriptor.setBlockMismatchingMimeTypes(model.isBlockMismatchingMimeTypes());
        descriptor.setMismatchingMimeTypesOverrideList(
                PathUtils.collectionToDelimitedString(model.getMismatchingMimeTypesOverrideList()));
        descriptor.setBypassHeadRequests(model.isBypassHeadRequests());
    }

    /**
     * Populates type specific values shared by local and remote repos
     */
    private void populateSharedTypeSpecificDescriptorValues(TypeSpecificConfigModel type,
            RealRepoDescriptor descriptor) {
        descriptor.setType(type.getRepoType());
        switch (type.getRepoType()) {
            case Maven:
            case Gradle:
            case Ivy:
            case SBT:
                MavenTypeSpecificConfigModel maven = (MavenTypeSpecificConfigModel) type;
                descriptor.setMaxUniqueSnapshots(maven.getMaxUniqueSnapshots());
                descriptor.setHandleReleases(maven.getHandleReleases());
                descriptor.setHandleSnapshots(maven.getHandleSnapshots());
                descriptor.setSuppressPomConsistencyChecks(maven.getSuppressPomConsistencyChecks());
                break;
            case NuGet:
                descriptor.setForceNugetAuthentication(((NugetTypeSpecificConfigModel) type).isForceNugetAuthentication());
                break;
        }
    }

    /**
     * Populates type specific values for local repos
     */
    private void populateLocalTypeSpecificDescriptorValues(TypeSpecificConfigModel type,
            LocalRepoDescriptor descriptor) {
        switch (type.getRepoType()) {
            case Maven:
            case Gradle:
            case Ivy:
            case SBT:
                MavenTypeSpecificConfigModel maven = (MavenTypeSpecificConfigModel) type;
                descriptor.setSnapshotVersionBehavior(maven.getSnapshotVersionBehavior());
                descriptor.setChecksumPolicyType(maven.getLocalChecksumPolicy());
                break;
            case YUM:
                YumTypeSpecificConfigModel yum = (YumTypeSpecificConfigModel) type;
                descriptor.setYumGroupFileNames(yum.getGroupFileNames());
                descriptor.setYumRootDepth(yum.getMetadataFolderDepth());
                descriptor.setCalculateYumMetadata(yum.isAutoCalculateYumMetadata());
                descriptor.setEnableFileListsIndexing(yum.isEnableFileListsIndexing());
                break;
            case Docker:
                DockerTypeSpecificConfigModel docker = (DockerTypeSpecificConfigModel) type;
                descriptor.setDockerApiVersion(docker.getDockerApiVersion().toString());
                descriptor.setMaxUniqueTags(docker.getMaxUniqueTags());
                descriptor.setBlockPushingSchema1(
                        DockerApiVersion.V2.equals(docker.getDockerApiVersion()) && docker.getBlockPushingSchema1());
                break;
            case Debian:
                DebTypeSpecificConfigModel deb = (DebTypeSpecificConfigModel) type;
                descriptor.setDebianTrivialLayout(deb.getTrivialLayout());
                descriptor.setOptionalIndexCompressionFormats(deb.getOptionalIndexCompressionFormats());
                break;
            case NuGet:
                NugetTypeSpecificConfigModel nuget = (NugetTypeSpecificConfigModel) type;
                descriptor.setMaxUniqueSnapshots(nuget.getMaxUniqueSnapshots());
                break;
        }
    }

    /**
     * Populates type specific values for remote repos
     */
    private void populateRemoteTypeSpecificDescriptorValues(TypeSpecificConfigModel type,
            HttpRepoDescriptor descriptor) {
        descriptor.setType(type.getRepoType());
        switch (type.getRepoType()) {
            case P2:
            case Maven:
            case Gradle:
            case Ivy:
            case SBT:
                MavenTypeSpecificConfigModel maven = (MavenTypeSpecificConfigModel) type;
                descriptor.setFetchJarsEagerly(maven.getEagerlyFetchJars());
                descriptor.setFetchSourcesEagerly(maven.getEagerlyFetchSources());
                descriptor.setRejectInvalidJars(maven.getRejectInvalidJars());
                descriptor.setListRemoteFolderItems(maven.isListRemoteFolderItems());
                descriptor.setChecksumPolicyType(maven.getRemoteChecksumPolicy());

                //Set p2 url for supporting repos
                if (descriptor.getUrl() != null) { //Should always be true, but this is to avoid accidental nulls
                    descriptor.setP2OriginalUrl(descriptor.getUrl());
                }
                break;
            case Bower:
                BowerTypeSpecificConfigModel bower = (BowerTypeSpecificConfigModel) type;
                buildAndSetBowerConfig(descriptor, bower);
                descriptor.setListRemoteFolderItems(bower.isListRemoteFolderItems());
                break;
            case CocoaPods:
                CocoaPodsTypeSpecificConfigModel pods = (CocoaPodsTypeSpecificConfigModel) type;
                buildAndSetPodsConfig(descriptor, pods);
                descriptor.setListRemoteFolderItems(pods.isListRemoteFolderItems());
                break;
            case VCS:
                VcsTypeSpecificConfigModel vcs = (VcsTypeSpecificConfigModel) type;
                buildAndSetVcsConfig(descriptor, vcs);
                descriptor.setListRemoteFolderItems(vcs.isListRemoteFolderItems());
                break;
            case Docker:
                DockerTypeSpecificConfigModel docker = (DockerTypeSpecificConfigModel) type;
                descriptor.setEnableTokenAuthentication(docker.isEnableTokenAuthentication());
                descriptor.setListRemoteFolderItems(docker.isListRemoteFolderItems());
                if (descriptor.getExternalDependencies() == null) {
                    descriptor.setExternalDependencies(new ExternalDependenciesConfig());
                }
                descriptor.getExternalDependencies().setEnabled(docker.getEnableForeignLayersCaching());
                descriptor.getExternalDependencies().getPatterns().clear();
                descriptor.getExternalDependencies().getPatterns().addAll(docker.getExternalPatterns());
                descriptor.getExternalDependencies().setRemoteRepo(null); //not used
                descriptor.setBlockPushingSchema1(
                        DockerApiVersion.V2.equals(docker.getDockerApiVersion()) && docker.getBlockPushingSchema1());
                break;
            case Debian:
                DebTypeSpecificConfigModel deb = (DebTypeSpecificConfigModel) type;
                descriptor.setListRemoteFolderItems(deb.isListRemoteFolderItems());
                break;
            case NuGet:
                NugetTypeSpecificConfigModel nuGet = (NugetTypeSpecificConfigModel) type;
                descriptor.setListRemoteFolderItems(nuGet.isListRemoteFolderItems());
                buildAndSetNugetConfig(descriptor, nuGet);
                break;
            case Generic:
                GenericTypeSpecificConfigModel generic = (GenericTypeSpecificConfigModel) type;
                descriptor.setListRemoteFolderItems(generic.isListRemoteFolderItems());
                break;
            case Go:
                GoTypeSpecificConfigModel go = (GoTypeSpecificConfigModel) type;
                buildAndSetGoConfig(descriptor, go);
                break;
            case YUM:
                YumTypeSpecificConfigModel yum = (YumTypeSpecificConfigModel) type;
                descriptor.setListRemoteFolderItems(yum.isListRemoteFolderItems());
                break;
            case Npm:
                NpmTypeSpecificConfigModel npm = (NpmTypeSpecificConfigModel) type;
                descriptor.setListRemoteFolderItems(npm.isListRemoteFolderItems());
                break;
            case Gems:
                GemsTypeSpecificConfigModel gems = (GemsTypeSpecificConfigModel) type;
                descriptor.setListRemoteFolderItems(gems.isListRemoteFolderItems());
                break;
            case Pypi:
                PypiTypeSpecificConfigModel pypi = (PypiTypeSpecificConfigModel) type;
                buildAndSetPyPIConfig(descriptor, pypi);
                descriptor.setListRemoteFolderItems(pypi.isListRemoteFolderItems());
                break;
            case GitLfs:
                GitLfsTypeSpecificConfigModel gitlfs = (GitLfsTypeSpecificConfigModel) type;
                descriptor.setListRemoteFolderItems(gitlfs.isListRemoteFolderItems());
                break;
            case Composer:
                ComposerTypeSpecificConfigModel composer = (ComposerTypeSpecificConfigModel) type;
                buildAndSetComposerConfig(descriptor, composer);
                descriptor.setListRemoteFolderItems(composer.isListRemoteFolderItems());
                break;
            case Chef:
                ChefTypeSpecificConfigModel chef = (ChefTypeSpecificConfigModel) type;
                descriptor.setListRemoteFolderItems(chef.isListRemoteFolderItems());
                break;
        }
    }

    private void populateVirtualTypeSpecific(TypeSpecificConfigModel type, VirtualRepoDescriptor descriptor) {
        descriptor.setType(type.getRepoType());
        switch (type.getRepoType()) {
            case P2:
                buildAndSetVirtualP2Config(descriptor, (P2TypeSpecificConfigModel) type);
                break;
            case NuGet:
                NugetTypeSpecificConfigModel nuget = (NugetTypeSpecificConfigModel) type;
                descriptor.setForceNugetAuthentication(nuget.isForceNugetAuthentication());
                break;
            case Bower:
                BowerTypeSpecificConfigModel bower = (BowerTypeSpecificConfigModel) type;
                if (descriptor.getExternalDependencies() == null) {
                    descriptor.setExternalDependencies(new ExternalDependenciesConfig());
                }
                descriptor.getExternalDependencies().setEnabled(bower.getEnableExternalDependencies());
                descriptor.getExternalDependencies().getPatterns().clear();
                descriptor.getExternalDependencies().getPatterns().addAll(bower.getExternalPatterns());
                descriptor.getExternalDependencies().setRemoteRepo(
                        centralConfig.getDescriptor().getRemoteRepositoriesMap().get(bower.getExternalRemoteRepo()));
                break;
            case Npm:
                NpmTypeSpecificConfigModel npm = (NpmTypeSpecificConfigModel) type;
                if (descriptor.getExternalDependencies() == null) {
                    descriptor.setExternalDependencies(new ExternalDependenciesConfig());
                }
                descriptor.getExternalDependencies().setEnabled(npm.getEnableExternalDependencies());
                descriptor.getExternalDependencies().getPatterns().clear();
                descriptor.getExternalDependencies().getPatterns().addAll(npm.getExternalPatterns());
                descriptor.getExternalDependencies().setRemoteRepo(
                        centralConfig.getDescriptor().getRemoteRepositoriesMap().get(npm.getExternalRemoteRepo()));
                VirtualCacheConfig virtualCacheConfig = descriptor.getVirtualCacheConfig();
                virtualCacheConfig = virtualCacheConfig != null ?  virtualCacheConfig : new VirtualCacheConfig();
                virtualCacheConfig.setVirtualRetrievalCachePeriodSecs(npm.getVirtualRetrievalCachePeriodSecs());
                descriptor.setVirtualCacheConfig(virtualCacheConfig);
                break;
            case Go:
                GoTypeSpecificConfigModel go = (GoTypeSpecificConfigModel) type;
                if (descriptor.getExternalDependencies() == null) {
                    descriptor.setExternalDependencies(new ExternalDependenciesConfig());
                }
                descriptor.getExternalDependencies().setEnabled(go.getEnableExternalDependencies());
                descriptor.getExternalDependencies().getPatterns().clear();
                descriptor.getExternalDependencies().getPatterns().addAll(go.getExternalPatterns());
                descriptor.getExternalDependencies().setRemoteRepo(null); //not used by go
                break;
            case YUM:
                YumTypeSpecificConfigModel yum = (YumTypeSpecificConfigModel) type;
                virtualCacheConfig = descriptor.getVirtualCacheConfig();
                virtualCacheConfig = virtualCacheConfig != null ?  virtualCacheConfig : new VirtualCacheConfig();
                virtualCacheConfig.setVirtualRetrievalCachePeriodSecs(yum.getVirtualRetrievalCachePeriodSecs());
                descriptor.setVirtualCacheConfig(virtualCacheConfig);
                break;
            case Helm:
                HelmTypeSpecificConfigModel helm = (HelmTypeSpecificConfigModel) type;
                virtualCacheConfig = descriptor.getVirtualCacheConfig();
                virtualCacheConfig = virtualCacheConfig != null ?  virtualCacheConfig : new VirtualCacheConfig();
                virtualCacheConfig.setVirtualRetrievalCachePeriodSecs(helm.getVirtualRetrievalCachePeriodSecs());
                descriptor.setVirtualCacheConfig(virtualCacheConfig);
                break;
            case CRAN:
                CranTypeSpecificConfigModel cran = (CranTypeSpecificConfigModel) type;
                virtualCacheConfig = descriptor.getVirtualCacheConfig();
                virtualCacheConfig = virtualCacheConfig != null ?  virtualCacheConfig : new VirtualCacheConfig();
                virtualCacheConfig.setVirtualRetrievalCachePeriodSecs(cran.getVirtualRetrievalCachePeriodSecs());
                descriptor.setVirtualCacheConfig(virtualCacheConfig);
                break;
            case Conan:
                ConanTypeSpecificConfigModel conan = (ConanTypeSpecificConfigModel) type;
                virtualCacheConfig = descriptor.getVirtualCacheConfig();
                virtualCacheConfig = virtualCacheConfig != null ?  virtualCacheConfig : new VirtualCacheConfig();
                virtualCacheConfig.setVirtualRetrievalCachePeriodSecs(conan.getVirtualRetrievalCachePeriodSecs());
                descriptor.setVirtualCacheConfig(virtualCacheConfig);
                break;
            case Conda:
                CondaTypeSpecificConfigModel conda = (CondaTypeSpecificConfigModel) type;
                virtualCacheConfig = descriptor.getVirtualCacheConfig();
                virtualCacheConfig = virtualCacheConfig != null ?  virtualCacheConfig : new VirtualCacheConfig();
                virtualCacheConfig.setVirtualRetrievalCachePeriodSecs(conda.getVirtualRetrievalCachePeriodSecs());
                descriptor.setVirtualCacheConfig(virtualCacheConfig);
                break;
            case Chef:
                ChefTypeSpecificConfigModel chef = (ChefTypeSpecificConfigModel) type;
                virtualCacheConfig = descriptor.getVirtualCacheConfig();
                virtualCacheConfig = virtualCacheConfig != null ?  virtualCacheConfig : new VirtualCacheConfig();
                virtualCacheConfig.setVirtualRetrievalCachePeriodSecs(chef.getVirtualRetrievalCachePeriodSecs());
                descriptor.setVirtualCacheConfig(virtualCacheConfig);
                break;
            case Debian:
                DebTypeSpecificConfigModel debian = (DebTypeSpecificConfigModel) type;
                virtualCacheConfig = descriptor.getVirtualCacheConfig();
                virtualCacheConfig = virtualCacheConfig != null ?  virtualCacheConfig : new VirtualCacheConfig();
                virtualCacheConfig.setVirtualRetrievalCachePeriodSecs(debian.getVirtualRetrievalCachePeriodSecs());
                descriptor.setVirtualCacheConfig(virtualCacheConfig);
                descriptor.setDebianDefaultArchitecturesList(debian.getVirtualArchitectures());
                descriptor.setDebianOptionalIndexCompressionFormats(debian.getOptionalIndexCompressionFormats());
                break;
            case Maven: //P2 reaches maven also
                MavenTypeSpecificConfigModel maven = (MavenTypeSpecificConfigModel) type;
                descriptor.setForceMavenAuthentication(maven.isForceMavenAuthentication());
            case Gradle:
            case Ivy:
            case SBT:
                MavenTypeSpecificConfigModel mavenTypes = (MavenTypeSpecificConfigModel) type;
                descriptor.setKeyPair(mavenTypes.getKeyPair());
                descriptor.setPomRepositoryReferencesCleanupPolicy(mavenTypes.getPomCleanupPolicy());
                break;
            case Docker:
                DockerTypeSpecificConfigModel docker = (DockerTypeSpecificConfigModel) type;
                descriptor.setResolveDockerTagsByTimestamp(docker.getResolveDockerTagsByTimestamp());
                break;
        }
    }

    private void buildAndSetNugetConfig(HttpRepoDescriptor descriptor, NugetTypeSpecificConfigModel nuGet) {
        NuGetConfiguration nugetConfig = new NuGetConfiguration();
        nugetConfig.setDownloadContextPath(nuGet.getDownloadContextPath());
        nugetConfig.setFeedContextPath(nuGet.getFeedContextPath());
        nugetConfig.setV3FeedUrl(nuGet.getV3FeedUrl());
        descriptor.setNuget(nugetConfig);
    }

    private void buildAndSetVcsConfig(HttpRepoDescriptor descriptor, VcsTypeSpecificConfigModel vcs) {
        VcsConfiguration vcsConfig = new VcsConfiguration();
        VcsGitConfiguration vcsGitConfig = new VcsGitConfiguration();
        vcsGitConfig.setProvider(vcs.getGitProvider());
        vcsGitConfig.setDownloadUrl(vcs.getDownloadUrl());
        vcsConfig.setType(vcs.getVcsType());
        vcsConfig.setGit(vcsGitConfig);
        descriptor.setVcs(vcsConfig);
        descriptor.setMaxUniqueSnapshots(vcs.getMaxUniqueSnapshots());
        descriptor.setListRemoteFolderItems(vcs.isListRemoteFolderItems());
    }

    private void buildAndSetBowerConfig(HttpRepoDescriptor descriptor, BowerTypeSpecificConfigModel bower) {
        buildAndSetVcsConfig(descriptor, bower);
        BowerConfiguration bowerConfig = new BowerConfiguration();
        bowerConfig.setBowerRegistryUrl(bower.getRegistryUrl());
        descriptor.setBower(bowerConfig);
    }

    private void buildAndSetGoConfig(HttpRepoDescriptor descriptor, GoTypeSpecificConfigModel go) {
        buildAndSetVcsConfig(descriptor, go);
    }

    private void buildAndSetPyPIConfig(HttpRepoDescriptor descriptor, PypiTypeSpecificConfigModel pyPI) {
        PypiConfiguration pyPIConfig = new PypiConfiguration();
        pyPIConfig.setPyPIRegistryUrl(pyPI.getRegistryUrl());
        pyPIConfig.setRepositorySuffix(pyPI.getRepositorySuffix());
        descriptor.setPypi(pyPIConfig);
    }

    private void buildAndSetComposerConfig(HttpRepoDescriptor descriptor, ComposerTypeSpecificConfigModel composer) {
        buildAndSetVcsConfig(descriptor, composer);
        ComposerConfiguration composerConfig = new ComposerConfiguration();
        composerConfig.setComposerRegistryUrl(composer.getRegistryUrl());
        descriptor.setComposer(composerConfig);
    }

    private void buildAndSetPodsConfig(HttpRepoDescriptor descriptor, CocoaPodsTypeSpecificConfigModel pods) {
        buildAndSetVcsConfig(descriptor, pods);
        CocoaPodsConfiguration podsConfig = new CocoaPodsConfiguration();
        podsConfig.setCocoaPodsSpecsRepoUrl(pods.getSpecsRepoUrl());
        //Specs repo provider must be Artifactory or the same as the remote's Git provider - from UI it should always
        // match in any case, users can change it from the descriptor manually.
        VcsGitConfiguration podsGitProvider = new VcsGitConfiguration();
        podsGitProvider.setProvider(pods.getGitProvider());
        podsConfig.setSpecRepoProvider(podsGitProvider);
        descriptor.setCocoaPods(podsConfig);
    }

    private void buildAndSetVirtualP2Config(VirtualRepoDescriptor descriptor, P2TypeSpecificConfigModel p2) {
        P2Configuration p2Config = new P2Configuration();
        //This just adds the selected urls - creating repos etc is done at the service
        p2Config.setUrls(p2.getP2Repos().stream()
                .map(P2Repo::getRepoUrl)
                .distinct()
                .collect(Collectors.toList()));
        descriptor.setP2(p2Config);
    }

    private void buildAndSetXrayConfig(RealRepoDescriptor descriptor, LocalBasicRepositoryConfigModel basic) {
        XrayRepoConfig xrayConfig = null;
        XrayRepoConfigModel xrayConfigModel = basic.getXrayConfig();
        if (xrayConfigModel != null) {
            xrayConfig = new XrayRepoConfig();
            xrayConfig.setEnabled(xrayConfigModel.isEnabled());
        }
        descriptor.setXrayConfig(xrayConfig);
    }

    /**
     * Reason for entire model: currently only one field - 'enabled', but will expand in the near future
     */
    private void buildAndSetDownloadRedirectConfig(RealRepoDescriptor descriptor,
            LocalAdvancedRepositoryConfigModel advanced) {
        DownloadRedirectRepoConfig downloadRedirectConfig = null;
        DownloadRedirectRepoConfigModel downloadRedirectConfigModel = advanced.getDownloadRedirectConfig();
        if (downloadRedirectConfigModel != null && !addonsManager.addonByType(CoreAddons.class).isAol()) {
            downloadRedirectConfig = new DownloadRedirectRepoConfig();
            downloadRedirectConfig.setEnabled(downloadRedirectConfigModel.isEnabled());
        }
        descriptor.setDownloadRedirectConfig(downloadRedirectConfig);
    }

    private PropertySet getPropSetByName(String name) {
        return centralConfig.getDescriptor().getPropertySets().stream()
                .filter(propertySet -> propertySet.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    static String getUrlWithoutSubpath(String url) {
        int slashslash = url.indexOf("//") + 2;
        int nextSlash = url.indexOf('/', slashslash);
        return nextSlash < 0 ? url : PathUtils.trimSlashes(url.substring(0, nextSlash));
    }

    @Autowired
    public void setCentralConfig(CentralConfigService centralConfig) {
        this.centralConfig = centralConfig;
    }

    @Autowired
    public void setConfigValidator(
            RepoConfigValidator configValidator) {
        this.configValidator = configValidator;
    }
}
