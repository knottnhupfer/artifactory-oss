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

package org.artifactory.ui.rest.service.onboarding;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.onboarding.OnboardingRepoState;
import org.artifactory.ui.rest.model.onboarding.OnboardingReposStateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.artifactory.repo.config.RepoConfigDefaultValues.EXAMPLE_REPO_KEY;

/**
 * Return a map of repo types to one of 3 possible states - unset, already set or unavailable (in oss)
 *
 * @author nadavy
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetUnsetReposService implements RestService {

    private RepositoryService repositoryService;

    private AddonsManager addonsManager;

    @Autowired
    public GetUnsetReposService(RepositoryService repositoryService, AddonsManager addonsManager) {
        this.repositoryService = repositoryService;
        this.addonsManager = addonsManager;
    }


    private static final List<RepoType> unsupportedRepoTypes = Lists.newArrayList(
            RepoType.P2,
            RepoType.VCS,
            RepoType.Distribution,
            RepoType.ReleaseBundles
    );

    private static final List<RepoType> ossSupportedTypes = Lists.newArrayList(
            RepoType.Maven,
            RepoType.Gradle,
            RepoType.Ivy,
            RepoType.SBT,
            RepoType.Generic
    );

    private static final List<RepoType> conanCESupportedTypes = Lists.newArrayList(
            RepoType.Generic,
            RepoType.Conan
    );

    private static final List<RepoType> jcrSupportedTypes = Lists.newArrayList(
            RepoType.Generic,
            RepoType.Docker,
            RepoType.Helm
    );

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        Map<RepoType, OnboardingRepoState> repoTypesMap = getSupportedRepoTypesMap();
        filterRepoTypesMap(repoTypesMap);
        response.iModel(new OnboardingReposStateModel(repoTypesMap));
    }

    /**
     * Filter repositories already set up
     */
    private void filterRepoTypesMap(Map<RepoType, OnboardingRepoState> repoTypesMap) {
        repositoryService.getLocalRepoDescriptors().stream()
                .filter(repoDescriptor -> !repoDescriptor.getKey().equals(EXAMPLE_REPO_KEY))
                .forEach(repoDescriptor -> repoTypesMap.replace(repoDescriptor.getType(),
                        OnboardingRepoState.ALREADY_SET));
        repositoryService.getRemoteRepoDescriptors().stream()
                .filter(repoDescriptor -> !"jcenter".equals(repoDescriptor.getKey()))
                .forEach(repoDescriptor -> repoTypesMap.replace(repoDescriptor.getType(),
                        OnboardingRepoState.ALREADY_SET));
        repositoryService.getVirtualRepoDescriptors()
                .forEach(repoDescriptor -> repoTypesMap.replace(repoDescriptor.getType(),
                        OnboardingRepoState.ALREADY_SET));
        // build-info repo is not part of the on boarding wizard
        repoTypesMap.replace(RepoType.BuildInfo, OnboardingRepoState.ALREADY_SET);
        // jfrogsupport-bundle repo is not part of the on boarding wizard
        repoTypesMap.replace(RepoType.Support, OnboardingRepoState.ALREADY_SET);
    }

    /**
     * Return a map of supported repo types for onboarding setup
     */
     Map<RepoType, OnboardingRepoState> getSupportedRepoTypesMap() {
        Map<RepoType, OnboardingRepoState> repoTypesMap = Maps.newHashMap();
        boolean isOss = addonsManager instanceof OssAddonsManager;
        OnboardingRepoState defaultState = isOss ? OnboardingRepoState.UNAVAILABLE :
                OnboardingRepoState.UNSET;
        // set all repo types to the default state
        Arrays.stream(RepoType.values())
                .forEach(repoType -> repoTypesMap.put(repoType, defaultState));
        // in oss make only ossSupportedTypes available
        if (isOss) {
            if (addonsManager.getArtifactoryRunningMode().isConan()) {
                conanCESupportedTypes.forEach(repoType -> repoTypesMap.put(repoType, OnboardingRepoState.UNSET));
            } else if (addonsManager.getArtifactoryRunningMode().isJcrOrJcrAol() ) {
                jcrSupportedTypes.forEach(repoType -> repoTypesMap.put(repoType, OnboardingRepoState.UNSET));
            } else {
                ossSupportedTypes.forEach(repoType -> repoTypesMap.put(repoType, OnboardingRepoState.UNSET));
            }
        }
        // remove unsupported repo types
        unsupportedRepoTypes.forEach(repoTypesMap::remove);
        return repoTypesMap;
    }
}
