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

package org.artifactory.ui.rest.service.utils.repositories;

import com.google.common.collect.Lists;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.PackageSearchCriteria;
import org.artifactory.ui.rest.model.utils.repositories.RepoKeyType;
import org.artifactory.ui.rest.model.utils.repositories.RepositoriesData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.artifactory.descriptor.repo.RepoType.BuildInfo;
import static org.artifactory.descriptor.repo.RepoType.ReleaseBundles;
import static org.artifactory.descriptor.repo.SupportBundleRepoDescriptor.SUPPORT_BUNDLE_REPO_NAME;
import static org.artifactory.repo.RepoDetailsType.*;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAllRepositoriesService implements RestService {

    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private RepositoryService repoService;
    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        Boolean userOnly = Boolean.valueOf(request.getQueryParamByKey("user"));
        Boolean deploy = Boolean.valueOf(request.getQueryParamByKey("deploy"));
        Boolean localOnly = Boolean.valueOf(request.getQueryParamByKey("local"));
        Boolean searchOnly = Boolean.valueOf(request.getQueryParamByKey("search"));
        Boolean backupOnly = Boolean.valueOf(request.getQueryParamByKey("backup"));
        Boolean packageSearchOnly = Boolean.valueOf(request.getQueryParamByKey("packageSearch"));
        Boolean localRemoteOnly = Boolean.valueOf(request.getQueryParamByKey("all"));
        Boolean permission = Boolean.valueOf(request.getQueryParamByKey("permission"));

        RepositoriesData repositoriesData = new RepositoriesData();
        if (searchOnly) { //Called from the search screen
            searchRepoData(response, repositoriesData);
        } else if (packageSearchOnly) {
            packageSearchRepoData(response, repositoriesData);
        } else if (backupOnly) { //Called from the Backups and Repositories Import/Export screens
            repositoriesData.setRepoList(getRepoData(localOnly, true, false));
            response.iModel(repositoriesData);
        } else if (localRemoteOnly) {
            List<RepoKeyType> allRepositoriesData = getAllRepositoriesData();
            repositoriesData.setRepoTypesList(allRepositoriesData);
            response.iModelList(allRepositoriesData);
        } else if (permission) { //Called from the Permissions screen
            List<RepoKeyType> allRepositoriesData = getPermissionRepoData();
            repositoriesData.setRepoTypesList(allRepositoriesData);
            response.iModel(repositoriesData);
        } else if (deploy) {
            getRepositoriesDataForDeploy(repositoriesData);
            response.iModel(repositoriesData);
        } else {
            /// get repo data for local or user
            getRepositoriesData(userOnly, localOnly, repositoriesData);
            response.iModel(repositoriesData);
        }
    }

    /**
     * get repository list for search screen
     *
     * @param artifactoryResponse - encapsulated data related to response
     * @param repositoriesData    - repository data
     */
    private void searchRepoData(RestResponse artifactoryResponse, RepositoriesData repositoriesData) {
        List<RepoKeyType> orderdRepoKeys = getOrderedRepoKeys();
        repositoriesData.setRepoTypesList(orderdRepoKeys);
        artifactoryResponse.iModel(repositoriesData);
    }

    /**
     * get repository list for package search screen
     *
     * @param artifactoryResponse - encapsulated data related to response
     * @param repositoriesData    - repository data
     */
    private void packageSearchRepoData(RestResponse artifactoryResponse, RepositoriesData repositoriesData) {
        List<RepoKeyType> orderedRepoKeys = getPackageSearchRepos();
        repositoriesData.setRepoTypesList(orderedRepoKeys);
        artifactoryResponse.iModel(repositoriesData);
    }

    /**
     * return repositories data for user / local / all
     *
     * @param userOnly  - if true return only user repositories
     * @param localOnly - if true return only local repositories
     */
    private void getRepositoriesData(Boolean userOnly, Boolean localOnly, RepositoriesData repositoriesData) {
        List<String> repoData;
        if (userOnly) {
            repoData = getRepoData(true, false, true);
            List<RepoKeyType> deployReposes = new ArrayList<>();
            Map<String, LocalRepoDescriptor> localRepositoriesMap = centralConfigService.getDescriptor().getLocalRepositoriesMap();
            repoData.forEach(repoKey -> {
                if (repoKey.equals(SupportBundleRepoDescriptor.SUPPORT_BUNDLE_REPO_NAME)) {
                    deployReposes.add(new RepoKeyType(RepoType.Support, repoKey));
                } else {
                    LocalRepoDescriptor localRepoDescriptor = localRepositoriesMap.get(repoKey);
                    deployReposes.add(new RepoKeyType(localRepoDescriptor.getType(), repoKey));
                }
            });
            if (authorizationService.isAdmin()) {
                repositoriesData.setRepoTypesList(deployReposes);
            } else {
                repositoriesData.setRepoTypesList(getUserRepoForDeploy(deployReposes));
            }
        } else {
            repoData = getRepoData(localOnly, false, false);
            repositoriesData.setRepoList(repoData);
        }
    }

    /**
     * return repositories data for deploy (like for user, but include virtual repos with configured default deployment local repo)
     */
    private void getRepositoriesDataForDeploy(RepositoriesData repositoriesData) {
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        repositoriesData.setFileUploadMaxSizeMb(mutableDescriptor.getFileUploadMaxSizeMb());

        List<String> repoData;
        repoData = getRepoData(true, false, false);
        List<String> virtualRepoData;
        virtualRepoData = getVirtualRepoData();

        List<RepoKeyType> deployReposes = new ArrayList<>();
        repoData.forEach(repoKey -> {
            Map<String, LocalRepoDescriptor> localRepositoriesMap = centralConfigService.getDescriptor().getLocalRepositoriesMap();
            LocalRepoDescriptor localRepoDescriptor = localRepositoriesMap.get(repoKey);
            RepoKeyType repoKeyType = new RepoKeyType(localRepoDescriptor.getType(), repoKey);
            RepoLayout layout = localRepoDescriptor.getRepoLayout();
            if (layout != null) {
                repoKeyType.setLayoutPattern(layout.getArtifactPathPattern());
                repoKeyType.setLayoutFolderItegRevRegex(layout.getFolderIntegrationRevisionRegExp());
                repoKeyType.setLayoutFileItegRevRegex(layout.getFileIntegrationRevisionRegExp());
            }
            deployReposes.add(repoKeyType);
        });
        virtualRepoData.forEach(repoKey -> {
            Map<String, VirtualRepoDescriptor> virtualRepositoriesMap = centralConfigService.getDescriptor().getVirtualRepositoriesMap();
            VirtualRepoDescriptor virtualRepoDescriptor = virtualRepositoriesMap.get(repoKey);
            if (virtualRepoDescriptor.getDefaultDeploymentRepo() != null)  {
                RepoKeyType repoKeyType = new RepoKeyType(virtualRepoDescriptor.getType(), repoKey);
                RepoLayout layout = virtualRepoDescriptor.getRepoLayout();
                if (layout != null) {
                    repoKeyType.setLayoutPattern(layout.getArtifactPathPattern());
                    repoKeyType.setLayoutFolderItegRevRegex(layout.getFolderIntegrationRevisionRegExp());
                    repoKeyType.setLayoutFileItegRevRegex(layout.getFileIntegrationRevisionRegExp());
                }
                deployReposes.add(repoKeyType);
            }
        });
        if (authorizationService.isAdmin()) {
            repositoriesData.setRepoTypesList(deployReposes);
        } else {
            repositoriesData.setRepoTypesList(getUserRepoForDeploy(deployReposes));
        }
    }

    /**
     * return repo data list from config descriptor
     *
     * @return repo data list
     */
    private List<String> getRepoData(boolean localOnly, boolean includeDistRepos, boolean includeSupportBundleRepo) {
        List<String> repos = new ArrayList<>();
        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
        Map<String, LocalRepoDescriptor> localRepoDescriptorMap = descriptor.getLocalRepositoriesMap();
        // add remote repositories
        if (!localOnly) {
            Map<String, RemoteRepoDescriptor> remoteRepoDescriptorMap = descriptor.getRemoteRepositoriesMap();
            repos.addAll(remoteRepoDescriptorMap.keySet());
        }
        if (includeDistRepos) {
            Map<String, DistributionRepoDescriptor> distRepos = descriptor.getDistributionRepositoriesMap();
            repos.addAll(distRepos.keySet());
            Map<String, ReleaseBundlesRepoDescriptor> releaseRepos = descriptor.getReleaseBundlesRepositoriesMap();
            repos.addAll(releaseRepos.keySet());
        }
        if (includeSupportBundleRepo) {
            repos.add(SUPPORT_BUNDLE_REPO_NAME);
        }
        repos.addAll(localRepoDescriptorMap.keySet());
        return repos;
    }

    /**
     * return virtual repo data list from config descriptor
     *
     * @return repo data list
     */
    private List<String> getVirtualRepoData() {
        List<String> repos = new ArrayList<>();
        Map<String, VirtualRepoDescriptor> virtualRepoDescriptorMap = centralConfigService.getDescriptor().getVirtualRepositoriesMap();
        repos.addAll(virtualRepoDescriptorMap.keySet());
        return repos;
    }

    /**
     * get list of local repository allowed for this user to deploy
     *
     * @return - list of repositories
     */
    private List<RepoKeyType> getUserRepoForDeploy(List<RepoKeyType> localRepos) {
        List<RepoKeyType> userAuthorizedRepos = Lists.newArrayList();
        localRepos.forEach(repoType -> {
            RepoPath repoPath = InternalRepoPathFactory.create(repoType.getRepoKey(), "");
            if (RepoType.BuildInfo.equals(repoType.getRepoType())) {
                //Special case for build repo since it moves files to the correct path automatically its ok to show
                //deployment anywhere
              if (authorizationService.hasBuildPermission(ArtifactoryPermission.DEPLOY)) {
                  userAuthorizedRepos.add(repoType);
              }
            } else if (!repoType.getRepoKey().endsWith("-cached") && authorizationService.canDeploy(repoPath)) {
                userAuthorizedRepos.add(repoType);
            } else {
                getVirtualDeployRepoIfAvailable(userAuthorizedRepos, repoType);
            }
        });
        return userAuthorizedRepos;
    }

    private void getVirtualDeployRepoIfAvailable(List<RepoKeyType> userAuthorizedRepos, RepoKeyType repoType) {
        VirtualRepoDescriptor virtualRepoDescriptor = repoService.virtualRepoDescriptorByKey(repoType.getRepoKey());
        if (virtualRepoDescriptor != null) {
            LocalRepoDescriptor defaultDeploymentRepo = virtualRepoDescriptor.getDefaultDeploymentRepo();
            if (defaultDeploymentRepo != null) {
                RepoPath repoPath = InternalRepoPathFactory.create(defaultDeploymentRepo.getKey(), "");
                if (authorizationService.canDeploy(repoPath)) {
                    userAuthorizedRepos.add(repoType);
                }
            }
        }
    }


    private List<RepoKeyType> getOrderedRepoKeys() {
        List<LocalRepoDescriptor> repoSet = Lists.newArrayList();
        List<LocalRepoDescriptor> localAndCachedRepoDescriptors = repoService.getLocalAndCachedRepoDescriptors();
        localAndCachedRepoDescriptors.addAll(repoService.getDistributionRepoDescriptors());
        Collections.sort(localAndCachedRepoDescriptors, new LocalAndCachedDescriptorsComparator());
        localAndCachedRepoDescriptors.stream()
                .filter(descriptor -> authorizationService.canRead(InternalRepoPathFactory.repoRootPath(descriptor.getKey())))
                .forEach(repoSet::add);
        List<RepoKeyType> repoKeys = Lists.newArrayList();
        for (LocalRepoDescriptor descriptor : repoSet) {
            String type;
            if (descriptor instanceof LocalCacheRepoDescriptor) {
                type = REMOTE.typeNameLowercase();
            } else if (descriptor instanceof DistributionRepoDescriptor) {
                type = DISTRIBUTION.typeNameLowercase();
            } else {
                type = LOCAL.typeNameLowercase();
            }
            repoKeys.add(new RepoKeyType(type, descriptor.getKey()));
        }
        return repoKeys;
    }

    private List<RepoKeyType> getPackageSearchRepos() {
        List<RepoKeyType> repoKeys = Lists.newArrayList();
        List<LocalRepoDescriptor> localAndCache = repoService.getLocalAndCachedRepoDescriptors();
        List<LocalRepoDescriptor> localOnly = repoService.getLocalRepoDescriptors();
        Collections.sort(localAndCache, new LocalAndCachedDescriptorsComparator());
        for (PackageSearchCriteria.PackageSearchType type : PackageSearchCriteria.PackageSearchType.values()) {
            //Get Docker repos only once
            if (type.equals(PackageSearchCriteria.PackageSearchType.dockerV1)) {
                continue;
            }
            List<LocalRepoDescriptor> repoList = type.isRemoteCachesProps() ? localAndCache : localOnly;
            repoList.stream()
                    .filter(descriptor -> descriptor.getType().equals(type.getRepoType()) ||
                                    (descriptor.getType().isMavenGroup() && type.getRepoType().isMavenGroup())
                    )
                    .filter(descriptor ->
                            authorizationService.canRead(InternalRepoPathFactory.repoRootPath(descriptor.getKey())))
                    .forEach(repoDescriptor -> {
                        String localOrRemote = repoDescriptor.isCache() ? REMOTE.typeNameLowercase() : LOCAL.typeNameLowercase();
                        if (repoDescriptor.getType().equals(RepoType.Docker)) {
                            repoKeys.add(new RepoKeyType(localOrRemote, repoDescriptor.getType(),
                                    repoDescriptor.getKey(), repoDescriptor.getDockerApiVersion()));
                        } else {
                            repoKeys.add(new RepoKeyType(localOrRemote, repoDescriptor.getType(),
                                    repoDescriptor.getKey()));
                        }
                    });
        }
        return repoKeys;
    }

    /**
     * Comparator that compares local and cached repositories according to their type (local or cached local) and then
     * internally sorting them by their key.
     */
    private static class LocalAndCachedDescriptorsComparator implements Comparator<RepoDescriptor> {
        @Override
        public int compare(RepoDescriptor o1, RepoDescriptor o2) {
            if (o1 instanceof LocalCacheRepoDescriptor && !(o2 instanceof LocalCacheRepoDescriptor)) {
                return 1;
            } else if (!(o1 instanceof LocalCacheRepoDescriptor) && (o2 instanceof LocalCacheRepoDescriptor)) {
                return -1;
            } else {
                return o1.getKey().toLowerCase().compareTo(o2.getKey().toLowerCase());
            }
        }
    }

    /**
     * return remote and local repository data
     *
     * @return list of repositories repo keys
     */
    private List<RepoKeyType> getAllRepositoriesData() {
        List<RepoKeyType> repos = getLocalRepoKeyTypes();
        Map<String, RemoteRepoDescriptor> remoteRepoDescriptorMap = centralConfigService.getDescriptor().getRemoteRepositoriesMap();
        remoteRepoDescriptorMap.keySet().forEach(key -> repos.add(new RepoKeyType(REMOTE.typeNameLowercase(), key)));
        repos.addAll(centralConfigService.getDescriptor().getDistributionRepositoriesMap().keySet().stream()
                .map(repoKey -> new RepoKeyType(DISTRIBUTION.typeNameLowercase(), repoKey))
                .collect(Collectors.toList()));
        Map<String, ReleaseBundlesRepoDescriptor> releaseBundlesReposMap = centralConfigService.getDescriptor()
                .getReleaseBundlesRepositoriesMap();
        releaseBundlesReposMap.keySet().forEach(key -> repos.add(new RepoKeyType("releaseBundles", key)));
        return repos;
    }

    private List<RepoKeyType> getPermissionRepoData() {
        List<RepoKeyType> repos = new ArrayList<>();
        List<RealRepoDescriptor> localAndRemoteRepoDescriptors = repoService.getLocalAndRemoteRepoDescriptors();
        localAndRemoteRepoDescriptors.stream()
                .filter(repo -> !BuildInfo.equals(repo.getType()) && !ReleaseBundles.equals(repo.getType()))
                .forEach(repoDesc -> {
            if (repoDesc instanceof RemoteRepoDescriptor) {
                repos.add(new RepoKeyType(REMOTE.typeNameLowercase(), repoDesc.getKey()));
            } else {
                repos.add(new RepoKeyType(LOCAL.typeNameLowercase(), repoDesc.getKey()));
            }
        });
        repoService.getDistributionRepoDescriptors()
                .forEach(descriptor -> repos.add(new RepoKeyType(DISTRIBUTION.typeNameLowercase(), descriptor.getKey())));
        return repos;
    }

    private List<RepoKeyType> getLocalRepoKeyTypes() {
        List<RepoKeyType> repos = new ArrayList<>();
        Map<String, LocalRepoDescriptor> localRepoDescriptorMap = centralConfigService.getDescriptor().getLocalRepositoriesMap();
        localRepoDescriptorMap.values().forEach(desc -> repos.add(new RepoKeyType(desc.getType(), desc.getKey())));
        return repos;
    }
}
