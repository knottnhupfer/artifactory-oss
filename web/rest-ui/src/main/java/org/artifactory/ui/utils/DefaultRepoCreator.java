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

package org.artifactory.ui.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.vcs.VcsGitProvider;
import org.artifactory.repo.onboarding.DefaultRepoModel;
import org.artifactory.repo.onboarding.RemoteDefaultRepoModel;
import org.artifactory.repo.onboarding.VirtualDefaultRepoModel;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.admin.configuration.repository.GeneralRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.*;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualSelectedRepository;
import org.artifactory.ui.rest.service.admin.configuration.repositories.CreateRepositoryConfigService;
import org.artifactory.ui.rest.service.onboarding.CreateDefaultReposResponseModel;
import org.artifactory.ui.rest.service.onboarding.CreateDefaultReposResponseModel.CreatedReposByType;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.artifactory.repo.RepoDetailsType.DISTRIBUTION;
import static org.artifactory.repo.RepoDetailsType.LOCAL;

/**
 * @author nadavy
 */
public class DefaultRepoCreator {

    private static final Logger log = LoggerFactory.getLogger(DefaultRepoCreator.class);

    private Map<String, DefaultRepoModel> model;

    private MutableCentralConfigDescriptor configDescriptor;

    /**
     * Initialize default repos map
     */
    public DefaultRepoCreator() {
        CentralConfigService configService = ContextHelper.get().getCentralConfig();
        configDescriptor = configService.getMutableDescriptor();
        // load json to model
        try (InputStream json = getClass().getResourceAsStream("/templates/defaultRepository.json")) {
            model = JacksonReader.streamAsValueTypeReference(json, new TypeReference<List<DefaultRepoModel>>() {})
                    .stream()
                    .collect(Collectors.toMap(DefaultRepoModel::getRepoType, defaultModel -> defaultModel));
        } catch (IOException e) {
            String message = "Can't import default repository file";
            log.error(message);
            log.debug(message, e);
        }
    }

    /**
     * Create default repositories of given repo type. returns a list of created repos
     */
    public void createDefaultRepos(CreateRepositoryConfigService createRepoService, RestResponse response,
                                   RepoType repoType, CreateDefaultReposResponseModel createDefaultReposResponseModel,
                                   boolean isEdgeLicensed) {
        List<String> excludedLocalReposToInclude = Lists.newArrayList();
        List<String> excludedRemoteReposToInclude = Lists.newArrayList();
        CreatedReposByType createdReposByType =
                createDefaultReposResponseModel.addRepoType(repoType);
        createdReposByType.setLocalRepos(
                createLocalReposConfigModel(createRepoService, response, repoType, excludedLocalReposToInclude));
        if (!isEdgeLicensed) {
            createdReposByType.setRemoteRepos(
                    createRemoteReposConfigModel(createRepoService, response, repoType, excludedRemoteReposToInclude));
        }
        createdReposByType.setVirtualRepos(
                createVirtualReposConfigModel(createRepoService, response, repoType, excludedLocalReposToInclude,
                        excludedRemoteReposToInclude));
        createDefaultReposResponseModel.setValid(configDescriptor != null && createDefaultReposResponseModel.isValid());
    }

    public MutableCentralConfigDescriptor getConfigDescriptor() {
        return configDescriptor;
    }

    /**
     * Finds and creates local repos for given repo type
     */
    private List<String> createLocalReposConfigModel(CreateRepositoryConfigService createRepoService,
            RestResponse response, RepoType repoType, List<String> excludedReposToInclude) {
        List<String> repoKeys = model.get(repoType.getType()).getLocalRepoKeys();
        List<String> localReposCreated = Lists.newArrayList();
        if (repoKeys != null) {
            MutableCentralConfigDescriptor newConfigDescriptor;
            for (String localRepoKey : repoKeys) {
                try {
                    newConfigDescriptor = createRepoService
                            .createRepo(response, createLocalRepoConfigModel(repoType, localRepoKey),
                                    configDescriptor);
                    if (newConfigDescriptor != null) {
                        localReposCreated.add(localRepoKey);
                        configDescriptor = newConfigDescriptor;
                    } else {
                        if (isSameRepoSameTypeAlreadyExists(localRepoKey, repoType)) {
                            localReposCreated.add(localRepoKey);
                            log.debug("{} already exists, but not excluding from virtuals", localRepoKey);
                        } else {
                            log.debug("Repository {} already exists", localRepoKey);
                            excludedReposToInclude.add(localRepoKey);
                        }
                    }
                } catch (RepoConfigException e) {
                    String message = "Cannot create default local repositories for " + repoType.getType() + ", cause: " +
                            e.getMessage();
                    log.error(message);
                    log.debug(message, e);
                }
            }
        }
        return localReposCreated;
    }

    /**
     * Create a local default repo of given repo type
     */
    private LocalRepositoryConfigModel createLocalRepoConfigModel(RepoType repoType, String repoKey) {
        LocalRepositoryConfigModel localRepositoryConfigModel = new LocalRepositoryConfigModel();
        GeneralRepositoryConfigModel general = new GeneralRepositoryConfigModel();
        general.setRepoKey(repoKey);
        LocalBasicRepositoryConfigModel basic = new LocalBasicRepositoryConfigModel();
        TypeSpecificConfigModel typeSpecific;
        basic.setLayout(model.get(repoType.getType()).getLayout());
        typeSpecific = getTypeSpecific(repoType);
        if (RepoType.Maven == repoType) {
            if (repoKey.contains("snapshot")) {
                ((MavenTypeSpecificConfigModel) typeSpecific).setHandleSnapshots(true);
                ((MavenTypeSpecificConfigModel) typeSpecific).setHandleReleases(false);
            } else {
                ((MavenTypeSpecificConfigModel) typeSpecific).setHandleSnapshots(false);
                ((MavenTypeSpecificConfigModel) typeSpecific).setHandleReleases(true);
            }
        }
        LocalAdvancedRepositoryConfigModel advanced = new LocalAdvancedRepositoryConfigModel();
        localRepositoryConfigModel.setTypeSpecific(typeSpecific);
        localRepositoryConfigModel.setBasic(basic);
        localRepositoryConfigModel.setAdvanced(advanced);
        localRepositoryConfigModel.setReplications(null);
        localRepositoryConfigModel.setGeneral(general);
        return localRepositoryConfigModel;
    }

    /**
     * Finds and creates remote repos for given repo type
     */
    private List<String> createRemoteReposConfigModel(CreateRepositoryConfigService createRepoService,
            RestResponse response, RepoType repoType, List<String> excludedReposToInclude) {
        List<RemoteDefaultRepoModel> repoKeys = model.get(repoType.getType()).getRemoteRepoKeys();
        List<String> remoteReposCreated = Lists.newArrayList();
        if (repoKeys != null) {
            MutableCentralConfigDescriptor newConfigDescriptor;
            for (RemoteDefaultRepoModel remoteRepo : repoKeys) {
                try {
                    newConfigDescriptor = createRepoService
                            .createRepo(response, createRemoteRepoConfigModel(repoType, remoteRepo), configDescriptor);
                    if (newConfigDescriptor != null) {
                        remoteReposCreated.add(remoteRepo.getRepoKey());
                        configDescriptor = newConfigDescriptor;
                    } else {
                        // add jcenter to the response in case already created
                        if (isJCenterAlreadyExists(remoteRepo.getRepoKey())) {
                            remoteReposCreated.add(remoteRepo.getRepoKey());
                            log.debug("{} already exists, but not excluding from virtuals", remoteRepo);
                        } else {
                            if (! isSameRepoSameTypeAlreadyExists(remoteRepo.getRepoKey(), repoType)) {
                                excludedReposToInclude.add(remoteRepo.getRepoKey());
                                log.debug("Repository {} already exists", remoteRepo);
                            }
                        }
                    }
                } catch (RepoConfigException e) {
                    String message = "Cannot create default remote repositories for " + repoType.getType() + ", cause: " +
                            e.getMessage();
                    log.error(message);
                    log.debug(message, e);
                }
            }
        }
        return remoteReposCreated;
    }

    private boolean isJCenterAlreadyExists(String remoteRepoKey) {
        return "jcenter".equals(remoteRepoKey) &&
                isSameRepoSameTypeAlreadyExists(remoteRepoKey, RepoType.Maven);
    }

    private boolean isSameRepoSameTypeAlreadyExists(String repoKey, RepoType repoType) {
        RemoteRepoDescriptor repoDescriptor = configDescriptor.getRemoteRepositoriesMap().get(repoKey);
        return configDescriptor.isRepositoryExists(repoKey) &&
                repoDescriptor != null &&
                repoDescriptor.getType().equals(repoType);
    }

    /**
     * Create a remote default repo of given repo type
     */
    private RemoteRepositoryConfigModel createRemoteRepoConfigModel(RepoType repoType, RemoteDefaultRepoModel remoteRepo) {
        DefaultRepoModel defaultRepoModel = model.get(repoType.getType());
        RemoteRepositoryConfigModel remoteRepositoryConfigModel = new RemoteRepositoryConfigModel();
        GeneralRepositoryConfigModel general = new GeneralRepositoryConfigModel();
        general.setRepoKey(remoteRepo.getRepoKey());
        RemoteBasicRepositoryConfigModel basic = new RemoteBasicRepositoryConfigModel();
        RemoteAdvancedRepositoryConfigModel advanced = new RemoteAdvancedRepositoryConfigModel();
        TypeSpecificConfigModel typeSpecific;
        switch (repoType) {
            case Gradle:
            case Ivy:
            case SBT:
                basic.setLayout("maven-2-default");
                typeSpecific = getTypeSpecific(RepoType.Maven);
                break;
            default:
                basic.setLayout(defaultRepoModel.getLayout());
                typeSpecific = getTypeSpecific(repoType);
                break;
        }
        if (Strings.isNullOrEmpty(remoteRepo.getUrl())) {
            basic.setUrl(typeSpecific.getUrl());
        } else {
           basic.setUrl(remoteRepo.getUrl());
        }
        if (isNotBlank(remoteRepo.getGitProvider()) && typeSpecific instanceof VcsTypeSpecificConfigModel) {
            ((VcsTypeSpecificConfigModel)typeSpecific).setGitProvider(VcsGitProvider.valueOf(remoteRepo.getGitProvider()));
        }
        remoteRepositoryConfigModel.setTypeSpecific(typeSpecific);
        remoteRepositoryConfigModel.setBasic(basic);
        remoteRepositoryConfigModel.setAdvanced(advanced);
        remoteRepositoryConfigModel.setReplications(null);
        remoteRepositoryConfigModel.setGeneral(general);
        return remoteRepositoryConfigModel;
    }


    /**
     * Create a virtual default repo of given repo type
     */
    private List<String> createVirtualReposConfigModel(CreateRepositoryConfigService createRepoService,
            RestResponse response, RepoType repoType, List<String> excludedLocalReposToInclude,
            List<String> excludedRemoteReposToInclude) {
        List<VirtualDefaultRepoModel> virtualRepos = model.get(repoType.getType()).getVirtualRepoKeys();
        List<String> virtualReposCreated = Lists.newArrayList();
        if (virtualRepos != null) {
            MutableCentralConfigDescriptor newConfigDescriptor;
            for (VirtualDefaultRepoModel virtualRepo : virtualRepos) {
                try {
                    newConfigDescriptor = createRepoService
                            .createRepo(response,
                                    createVirtualRepoConfigModel(repoType, virtualRepo, excludedLocalReposToInclude,
                                            excludedRemoteReposToInclude), configDescriptor);
                    if (newConfigDescriptor != null) {
                        virtualReposCreated.add(virtualRepo.getRepoKey());
                        configDescriptor = newConfigDescriptor;
                    }
                } catch (RepoConfigException e) {
                    String message = "Cannot create default virtual repositories for " + repoType.getType() + ", cause: " +
                            e.getMessage();
                    log.error(message);
                    log.debug(message, e);
                }

            }
        }
        return virtualReposCreated;
    }

    private VirtualRepositoryConfigModel createVirtualRepoConfigModel(RepoType repoType,
            VirtualDefaultRepoModel virtualRepoModel, List<String> excludedLocalReposToInclude,
            List<String> excludedRemoteReposToInclude) {
        DefaultRepoModel defaultRepoModel = model.get(repoType.getType());
        VirtualRepositoryConfigModel virtualRepositoryConfigModel = new VirtualRepositoryConfigModel();
        GeneralRepositoryConfigModel general = new GeneralRepositoryConfigModel();
        general.setRepoKey(virtualRepoModel.getRepoKey());
        VirtualBasicRepositoryConfigModel basic = new VirtualBasicRepositoryConfigModel();
        basic.setDefaultDeploymentRepo(virtualRepoModel.getDefaultDeployment());
        basic.setSelectedRepositories(
                getVirtualSelectedRepos(virtualRepoModel, excludedLocalReposToInclude, excludedRemoteReposToInclude));
        basic.setLayout(defaultRepoModel.getLayout());
        VirtualAdvancedRepositoryConfigModel advanced = new VirtualAdvancedRepositoryConfigModel();
        TypeSpecificConfigModel typeSpecific = getTypeSpecific(repoType);
        virtualRepositoryConfigModel.setTypeSpecific(typeSpecific);
        virtualRepositoryConfigModel.setBasic(basic);
        virtualRepositoryConfigModel.setAdvanced(advanced);
        virtualRepositoryConfigModel.setGeneral(general);
        return virtualRepositoryConfigModel;
    }

    /**
     * returns all given repotype local and remote default repos.
     * default virtual will contain all those repos by default
     */
    private List<VirtualSelectedRepository> getVirtualSelectedRepos(VirtualDefaultRepoModel virtualRepo,
            List<String> excludedLocalReposToInclude, List<String> excludedRemoteReposToInclude) {
        List<VirtualSelectedRepository> virtualSelectedRepositoryList = Lists.newArrayList();
        List<String> includedLocalRepos = virtualRepo.getIncludedLocalRepos();
        if (includedLocalRepos != null) {
            includedLocalRepos.removeAll(excludedLocalReposToInclude);
            for (String localRepo : includedLocalRepos) {
                VirtualSelectedRepository virtualSelectedRepository = new VirtualSelectedRepository();
                virtualSelectedRepository.setRepoName(localRepo);
                virtualSelectedRepository.setType(LOCAL.typeNameLowercase());
                virtualSelectedRepositoryList.add(virtualSelectedRepository);
            }
        }
        List<String> includedRemoteRepos = virtualRepo.getIncludedRemoteRepos();
        if (includedRemoteRepos != null) {
            includedRemoteRepos.removeAll(excludedRemoteReposToInclude);
            for (String remoteRepo : includedRemoteRepos) {
                VirtualSelectedRepository virtualSelectedRepository = new VirtualSelectedRepository();
                virtualSelectedRepository.setRepoName(remoteRepo);
                virtualSelectedRepository.setType(DISTRIBUTION.typeNameLowercase());
                virtualSelectedRepositoryList.add(virtualSelectedRepository);
            }
        }
        return virtualSelectedRepositoryList;
    }

    private TypeSpecificConfigModel getTypeSpecific(RepoType repoType) {
        switch (repoType) {
            case Maven:
                return new MavenTypeSpecificConfigModel();
            case Bower:
                return new BowerTypeSpecificConfigModel();
            case Chef:
                return new ChefTypeSpecificConfigModel();
            case CocoaPods:
                return new CocoaPodsTypeSpecificConfigModel();
            case Debian:
                return new DebTypeSpecificConfigModel();
            case Composer:
                return new ComposerTypeSpecificConfigModel();
            case Docker:
                return new DockerTypeSpecificConfigModel();
            case Gems:
                return new GemsTypeSpecificConfigModel();
            case GitLfs:
                return new GitLfsTypeSpecificConfigModel();
            case Gradle:
                return new GradleTypeSpecificConfigModel();
            case Go:
                return new GoTypeSpecificConfigModel();
            case Helm:
                return new HelmTypeSpecificConfigModel();
            case Ivy:
                return new IvyTypeSpecificConfigModel();
            case Npm:
                return new NpmTypeSpecificConfigModel();
            case NuGet:
                return new NugetTypeSpecificConfigModel();
            case Pypi:
                return new PypiTypeSpecificConfigModel();
            case Vagrant:
                return new VagrantTypeSpecificConfigModel();
            case Opkg:
                return new OpkgTypeSpecificConfigModel();
            case YUM:
                return new YumTypeSpecificConfigModel();
            case SBT:
                return new SbtTypeSpecificConfigModel();
            case Conan:
                return new ConanTypeSpecificConfigModel();
            case Puppet:
                return new PuppetTypeSpecificConfigModel();
            case CRAN:
                return new CranTypeSpecificConfigModel();
            case Conda:
                return new CondaTypeSpecificConfigModel();
            default:
                return new GenericTypeSpecificConfigModel();
        }
    }
}