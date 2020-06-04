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

package org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.rest.common.validator.RepositoryNameValidator;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.PropertySetNameModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.AdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.BasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.DownloadRedirectRepoConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.distribution.DistributionAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.distribution.DistributionBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.distribution.DistributionRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.releasebundles.ReleaseBundlesRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.*;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.DistRepoTypeSpecificConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.TypeSpecificConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualSelectedRepository;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.http.HttpStatus.*;
import static org.artifactory.repo.RepoValidationConstants.DOWNLOAD_REDIRECT_AND_STORE_LOCALLY_ERROR;
import static org.artifactory.repo.RepoValidationConstants.DOWNLOAD_REDIRECT_NO_ENTERPRISE_PLUS_EDGE_ERROR;
import static org.artifactory.repo.config.RepoConfigDefaultValues.*;

/**
 * Service validates values in the model and sets default values as needed.
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RepoConfigValidator {
    private static final Logger log = LoggerFactory.getLogger(RepoConfigValidator.class);

    private final UrlValidator urlValidator = new UrlValidator("http", "https");

    private CentralConfigService centralConfig;
    private AddonsManager addonsManager;

    @Autowired
    public RepoConfigValidator(CentralConfigService centralConfig, AddonsManager addonsManager) {
        this.centralConfig = centralConfig;
        this.addonsManager = addonsManager;
    }

    public void validateLocal(LocalRepositoryConfigModel model) throws RepoConfigException {
        verifyAllSectionsExist(model);

        validateSharedBasic(model.getBasic());

        LocalAdvancedRepositoryConfigModel advanced = model.getAdvanced();
        validateSharedAdvanced(advanced);
        validateDownloadRedirectLicenseType(advanced, model.getGeneral().getRepoKey());

        TypeSpecificConfigModel typeSpecific = model.getTypeSpecific();
        typeSpecific.validateSharedTypeSpecific();
        typeSpecific.validateLocalTypeSpecific();
    }

    /**
     * Currently, there is no a single unified place for descriptor validation and each module (UI, REST, xml)
     * has it's own validation.
     * Here we are printing a warning in case the repo is configured for download redirect, though not failing it.
     *
     * When a single unified validation will be made, it should go into it.
     */
    private void validateDownloadRedirectLicenseType(LocalAdvancedRepositoryConfigModel advanced, String repoKey) {
        if (addonsManager.isEnterprisePlusInstalled() || addonsManager.isEdgeLicensed()) {
            return;
        }
        // License is not Enterprise+, check if one of the repos is configured for it
        DownloadRedirectRepoConfigModel downloadRedirectConfig = advanced.getDownloadRedirectConfig();
        if (downloadRedirectConfig != null && downloadRedirectConfig.isEnabled() && log.isWarnEnabled()) {
            log.warn("{}", String.format(DOWNLOAD_REDIRECT_NO_ENTERPRISE_PLUS_EDGE_ERROR, repoKey));
        }
    }

    public void validateRemote(RemoteRepositoryConfigModel model) throws RepoConfigException {
        verifyAllSectionsExist(model);

        //basic
        RemoteBasicRepositoryConfigModel basic = model.getBasic();
        validateSharedBasic(basic);
        if (isBlank(basic.getUrl())) {
            throw new RepoConfigException("URL cannot be empty", SC_BAD_REQUEST);
        }
        try {
            urlValidator.validate(basic.getUrl());
        } catch (UrlValidator.UrlValidationException e) {
            throw new RepoConfigException("Invalid URL: " + e.getMessage(), SC_BAD_REQUEST, e);
        }
        if (basic.getRemoteLayoutMapping() != null
                && centralConfig.getDescriptor().getRepoLayout(basic.getLayout()) == null) {
            throw new RepoConfigException("Invalid remote repository layout", SC_BAD_REQUEST);
        }
        basic.setOffline(ofNullable(basic.isOffline()).orElse(DEFAULT_OFFLINE));

        //advanced
        RemoteAdvancedRepositoryConfigModel advanced = model.getAdvanced();
        validateSharedAdvanced(advanced);
        advanced.setHardFail(ofNullable(advanced.getHardFail()).orElse(DEFAULT_HARD_FAIL));
        advanced.setStoreArtifactsLocally(
                ofNullable(advanced.isStoreArtifactsLocally()).orElse(DEFAULT_STORE_ARTIFACTS_LOCALLY));
        validateDownloadRedirectLicenseType(advanced, model.getGeneral().getRepoKey());
        DownloadRedirectRepoConfigModel downloadRedirectConfig = advanced.getDownloadRedirectConfig();
        if (downloadRedirectConfig != null && downloadRedirectConfig.isEnabled() && !advanced.isStoreArtifactsLocally()) {
            throw new RepoConfigException(DOWNLOAD_REDIRECT_AND_STORE_LOCALLY_ERROR, SC_BAD_REQUEST);
        }
        advanced.setSynchronizeArtifactProperties(
                ofNullable(advanced.getSynchronizeArtifactProperties()).orElse(DEFAULT_SYNC_PROPERTIES));
        advanced.setShareConfiguration(
                ofNullable(advanced.isShareConfiguration()).orElse(DEFAULT_SHARE_CONFIG));

        //network
        RemoteNetworkRepositoryConfigModel network = advanced.getNetwork();
        if (network != null) {
            if (StringUtils.isNotBlank(network.getProxy()) &&
                    centralConfig.getDescriptor().getProxy(network.getProxy()) == null) {
                throw new RepoConfigException("Invalid proxy configuration", SC_BAD_REQUEST);
            }
            network.setSocketTimeout(ofNullable(network.getSocketTimeout()).orElse(DEFAULT_SOCKET_TIMEOUT));
            network.setSyncProperties(ofNullable(network.isSyncProperties()).orElse(DEFAULT_SYNC_PROPERTIES));
            network.setCookieManagement(
                    ofNullable(network.getCookieManagement()).orElse(DEFAULT_COOKIE_MANAGEMENT));
            network.setLenientHostAuth(
                    ofNullable(network.getLenientHostAuth()).orElse(DEFAULT_LENIENENT_HOST_AUTH));
        }

        //cache
        RemoteCacheRepositoryConfigModel cache = advanced.getCache();
        if (cache != null) {

            cache.setKeepUnusedArtifactsHours(
                    ofNullable(cache.getKeepUnusedArtifactsHours()).orElse(DEFAULT_KEEP_UNUSED_ARTIFACTS));
            cache.setRetrievalCachePeriodSecs(
                    ofNullable(cache.getRetrievalCachePeriodSecs()).orElse(DEFAULT_RETRIEVAL_CACHE_PERIOD));
            cache.setAssumedOfflineLimitSecs(
                    ofNullable(cache.getAssumedOfflineLimitSecs()).orElse(DEFAULT_ASSUMED_OFFLINE));
            cache.setMissedRetrievalCachePeriodSecs(
                    ofNullable(cache.getMissedRetrievalCachePeriodSecs()).orElse(
                            DEFAULT_MISSED_RETRIEVAL_PERIOD));
        }

        TypeSpecificConfigModel typeSpecific = model.getTypeSpecific();
        typeSpecific.validateSharedTypeSpecific();
        typeSpecific.validateRemoteTypeSpecific();
    }

    public void validateVirtual(VirtualRepositoryConfigModel model, MutableCentralConfigDescriptor configDescriptor)
            throws RepoConfigException {
        //Sections and aggregated repos validation
        verifyAllSectionsExist(model);
        validateAggregatedReposExistAndTypesMatch(model, configDescriptor);

        //basic
        model.getBasic().setIncludesPattern(ofNullable(model.getBasic().getIncludesPattern())
                .orElse(DEFAULT_INCLUDES_PATTERN));

        //advanced
        model.getAdvanced().setRetrieveRemoteArtifacts(ofNullable(model.getAdvanced()
                .getRetrieveRemoteArtifacts()).orElse(DEFAULT_VIRTUAL_CAN_RETRIEVE_FROM_REMOTE));

        //type specific
        model.getTypeSpecific().validateVirtualTypeSpecific(addonsManager);
    }

    public void validateDistribution(DistributionRepositoryConfigModel model) throws RepoConfigException {
        verifyAllSectionsExist(model);
        DistributionBasicRepositoryConfigModel basic = model.getBasic();
        DistributionAdvancedRepositoryConfigModel advanced = model.getAdvanced();
        DistRepoTypeSpecificConfigModel typeSpecific = model.getTypeSpecific();

        //basic
        validateSharedBasic(basic);

        //advanced
        validateSharedAdvanced(advanced);
        advanced.setDistributionRules(
                ofNullable(advanced.getDistributionRules()).orElse(Lists.newArrayList()));
        //type specific
        validateDistConfig(typeSpecific);
    }

    public void validateReleaseBundle(ReleaseBundlesRepositoryConfigModel model) throws RepoConfigException {
        verifyAllSectionsExist(model);
    }

    /**
     * Validates all given repo keys exist - throws an error for the first not found one.
     *
     * @param repoKeys - Keys to check if existing
     */
    public void validateSelectedReposInVirtualExist(List<VirtualSelectedRepository> repoKeys,
            MutableCentralConfigDescriptor configDescriptor)
            throws RepoConfigException {
        String nonExistentKey = repoKeys.stream()
                .map(VirtualSelectedRepository::getRepoName)
                .filter(repoKey -> !configDescriptor.getLocalRepositoriesMap().containsKey(repoKey) &&
                        !configDescriptor.getRemoteRepositoriesMap().containsKey(repoKey) &&
                        !configDescriptor.getVirtualRepositoriesMap().containsKey(repoKey))
                .findAny()
                .orElse(null);
        if (StringUtils.isNotBlank(nonExistentKey)) {
            throw new RepoConfigException("Repository '" + nonExistentKey + "' does not exist", SC_NOT_FOUND);
        }
    }

    private void validateAggregatedReposExistAndTypesMatch(VirtualRepositoryConfigModel model,
            MutableCentralConfigDescriptor configDescriptor)
            throws RepoConfigException {
        List<VirtualSelectedRepository> repoKeys = ofNullable(model.getBasic().getSelectedRepositories())
                .orElse(Lists.newArrayList());
        if (CollectionUtils.isNullOrEmpty(repoKeys)) {
            model.getBasic().setSelectedRepositories(repoKeys);
        } else {

            validateSelectedReposInVirtualExist(repoKeys, configDescriptor);
            RepoDescriptor invalidTypeDescriptor = repoKeys.stream()
                    .map(repoKey -> mapRepoKeyToDescriptor(repoKey, configDescriptor))
                    .filter(repoDescriptor -> !filterByType(model.getTypeSpecific().getRepoType(), repoDescriptor))
                    .findAny().orElse(null);
            if (invalidTypeDescriptor != null) {
                throw new RepoConfigException("Repository '" + model.getGeneral().getRepoKey()
                        + "' aggregates another repository '" + invalidTypeDescriptor.getKey() + "' that has a "
                        + "mismatching package type " + invalidTypeDescriptor.getType().name(), SC_FORBIDDEN);
            }

        }
    }

    private boolean filterByType(RepoType type, RepoDescriptor repo) {
        return repo != null && (type.equals(RepoType.Generic) || type.equals(RepoType.P2) ||
                (type.isMavenGroup() ? repo.getType().isMavenGroup() : repo.getType().equals(type)));
    }

    public RepoDescriptor mapRepoKeyToDescriptor(VirtualSelectedRepository repository,
            MutableCentralConfigDescriptor configDescriptor) {
        String repoKey = repository.getRepoName();
        RepoDescriptor descriptor = configDescriptor.getLocalRepositoriesMap().get(repoKey);
        if (descriptor == null) {
            descriptor = configDescriptor.getRemoteRepositoriesMap().get(repoKey);
        }
        if (descriptor == null) {
            descriptor = configDescriptor.getVirtualRepositoriesMap().get(repoKey);
        }
        return descriptor;
    }

    private void validateSharedBasic(BasicRepositoryConfigModel basic) throws RepoConfigException {
        basic.setIncludesPattern(ofNullable(basic.getIncludesPattern()).orElse(DEFAULT_INCLUDES_PATTERN));
        if (basic.getLayout() == null || centralConfig.getDescriptor().getRepoLayout(basic.getLayout()) == null) {
            throw new RepoConfigException("Invalid repository layout", SC_BAD_REQUEST);
        }
    }

    private void validateSharedAdvanced(AdvancedRepositoryConfigModel model) throws RepoConfigException {
        if (model.getPropertySets() == null) {
            return;
        }
        String invalidPropSet = model.getPropertySets().stream()
                .map(PropertySetNameModel::getName)
                .filter(propSetName -> !centralConfig.getMutableDescriptor().isPropertySetExists(propSetName))
                .findAny().orElse(null);
        if (StringUtils.isNotBlank(invalidPropSet)) {
            throw new RepoConfigException("Property set " + invalidPropSet + " doesn't exist", SC_NOT_FOUND);
        }
        model.setAllowContentBrowsing(
                ofNullable(model.getAllowContentBrowsing()).orElse(DEFAULT_ALLOW_CONTENT_BROWSING));
        model.setBlackedOut(ofNullable(model.isBlackedOut()).orElse(DEFAULT_BLACKED_OUT));
    }

    private void verifyAllSectionsExist(RepositoryConfigModel model) throws RepoConfigException {
        if (model.getGeneral() == null) {
            throw new RepoConfigException("Repository Key cannot be empty", SC_BAD_REQUEST);
        } else if (model.getBasic() == null) {
            throw new RepoConfigException("Basic configuration cannot be empty", SC_BAD_REQUEST);
        }
        if (model.getAdvanced() == null) {
            throw new RepoConfigException("Advanced configuration cannot be empty", SC_BAD_REQUEST);
        }
        if (model.getTypeSpecific() == null) {
            throw new RepoConfigException("Package type configuration cannot be empty", SC_BAD_REQUEST);
        }
    }

    public void validateRepoName(String repoKey, String repoType) throws RepoConfigException {
        RepositoryNameValidator.validateRepoName(repoKey, repoType, centralConfig);
    }

    private void validateDistConfig(DistRepoTypeSpecificConfigModel model) throws RepoConfigException {
        //New repo without authentication string
        if (isBlank(model.getBintrayAppConfig()) && isBlank(model.getBintrayAuthString())) {
            throw new RepoConfigException("Bintray OAuth authentication string cannot be empty",
                    HttpStatus.SC_BAD_REQUEST);
        }
    }
}
