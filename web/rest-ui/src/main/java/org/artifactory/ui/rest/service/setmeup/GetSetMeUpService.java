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

package org.artifactory.ui.rest.service.setmeup;

import com.google.common.collect.Sets;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.*;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.setmeup.SetMeUpModel;
import org.artifactory.ui.rest.model.utils.repositories.RepoKeyType;
import org.artifactory.util.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @author chen Keinan
 * @author Lior Hasson
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetSetMeUpService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        /// get list of repositories descriptor by user permissions
        List<RepoKeyType> repoKeyTypes = getRepoKeyTypes();
        // update set me up model
        SetMeUpModel setMeUpModel = getSetMeUpModel(request, repoKeyTypes);
        response.iModel(setMeUpModel);
    }

    private SetMeUpModel getSetMeUpModel(ArtifactoryRestRequest artifactoryRequest, List<RepoKeyType> repoKeyTypes) {
        SetMeUpModel setMeUpModel = new SetMeUpModel();
        setMeUpModel.setRepoKeyTypes(repoKeyTypes);
        String servletContextUrl = HttpUtils.getServletContextUrl(artifactoryRequest.getServletRequest());
        setMeUpModel.setBaseUrl(servletContextUrl);
        setMeUpModel.setServerId(centralConfigService.getServerName());
        setMeUpModel.setHostname(ContextHelper.get().beanForType(AddonsManager.class).addonByType(CoreAddons.class).getArtifactoryServerName());
        return setMeUpModel;
    }

    /**
     * get user keys type list
     * @return - list of user keys types
     */
    private List<RepoKeyType> getRepoKeyTypes() {
        Set<RepoBaseDescriptor> userRepos = getUserRepos();
        List<RepoKeyType> repoKeyTypes = new ArrayList<>();
        userRepos.forEach(userRepo -> {
            if (!userRepo.getKey().endsWith("-cache")) {
                RepoKeyType repoKeyType = new RepoKeyType(userRepo.getType(), userRepo.getKey());
                // update can read or deploy
                updateCanReadOrDeploy(userRepo, repoKeyType);

                if(userRepo instanceof LocalRepoDescriptor){
                    repoKeyType.setIsLocal(true);
                }
                if(userRepo instanceof RemoteRepoDescriptor){
                    repoKeyType.setIsRemote(true);
                }
                if(userRepo instanceof VirtualRepoDescriptor){
                    repoKeyType.setIsVirtual(true);
                    if (((VirtualRepoDescriptor)userRepo).getDefaultDeploymentRepo() != null) {
                        repoKeyType.setIsDefaultDeploymentConfigured(true);
                    }
                }
                repoKeyTypes.add(repoKeyType);
            }
        });

        return repoKeyTypes;
    }

    private void updateCanReadOrDeploy(RepoBaseDescriptor userRepo, RepoKeyType repoKeyType) {
        RepoPath repoPath;
        repoKeyType.setCanRead(false);
        repoKeyType.setCanDeploy(false);

        if(userRepo instanceof HttpRepoDescriptor){
            repoPath = InternalRepoPathFactory.repoRootPath(userRepo.getKey() + "-cache");
        }
        else {
            repoPath = InternalRepoPathFactory.repoRootPath(userRepo.getKey());
        }

        if (authorizationService.canRead(repoPath) ||
                authorizationService.userHasPermissionsOnRepositoryRoot(repoPath.getRepoKey())) {
            repoKeyType.setCanRead(true);
        }

        if (userRepo instanceof VirtualRepoDescriptor) {
            LocalRepoDescriptor defaultDeploymentRepo = ((VirtualRepoDescriptor) userRepo).getDefaultDeploymentRepo();
            if (defaultDeploymentRepo != null) {
                repoPath = InternalRepoPathFactory.repoRootPath(defaultDeploymentRepo.getKey());
            }
        }

        if(authorizationService.canDeploy(repoPath)) {
            repoKeyType.setCanDeploy(true);
        }
    }

    /**
     * get list of repositories allowed for this user to deploy
     *
     * @return - list of repositories
     */
    private Set<RepoBaseDescriptor> getUserRepos() {
        Set<RepoBaseDescriptor> baseDescriptors = Sets.newTreeSet(new RepoComparator());
        List<LocalRepoDescriptor> localDescriptors = repositoryService.getLocalAndCachedRepoDescriptors();
        removeNonPermissionRepositories(localDescriptors);
        baseDescriptors.addAll(localDescriptors);
        // add remote repo
        List<RemoteRepoDescriptor> remoteDescriptors = repositoryService.getRemoteRepoDescriptors();
        removeNonPermissionRepositories(remoteDescriptors);
        baseDescriptors.addAll(remoteDescriptors);
        // add virtual repo
        List<VirtualRepoDescriptor> virtualDescriptors = repositoryService.getVirtualRepoDescriptors();
        removeNonPermissionRepositories(virtualDescriptors);
        baseDescriptors.addAll(virtualDescriptors);

        return baseDescriptors;
    }

    /**
     *
     * filter non permitted repositories
     */
    private void removeNonPermissionRepositories(List<? extends RepoDescriptor> repositories) {
        AuthorizationService authorizationService = ContextHelper.get().getAuthorizationService();
        repositories.removeIf(
                repoDescriptor -> !authorizationService.userHasPermissionsOnRepositoryRoot(repoDescriptor.getKey()));
    }


    private static class RepoComparator implements Comparator<RepoBaseDescriptor> {
        @Override
        public int compare(RepoBaseDescriptor descriptor1, RepoBaseDescriptor descriptor2) {

            //Local repositories can be either ordinary or caches
            if (descriptor1 instanceof LocalRepoDescriptor) {
                boolean repo1IsCache = ((LocalRepoDescriptor) descriptor1).isCache();
                boolean repo2IsCache = ((LocalRepoDescriptor) descriptor2).isCache();

                //Cache repositories should appear in a higher priority
                if (repo1IsCache && !repo2IsCache) {
                    return 1;
                } else if (!repo1IsCache && repo2IsCache) {
                    return -1;
                }
            }
            return descriptor1.getKey().compareTo(descriptor2.getKey());
        }
    }
}
