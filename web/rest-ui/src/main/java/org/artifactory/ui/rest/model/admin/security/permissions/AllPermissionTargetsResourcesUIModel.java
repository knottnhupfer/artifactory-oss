/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2016 JFrog Ltd.
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

package org.artifactory.ui.rest.model.admin.security.permissions;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.security.permissions.PermissionTargetModel;
import org.artifactory.security.permissions.RepoPermissionTargetModel;
import org.artifactory.ui.rest.model.utils.repositories.RepoKeyType;
import org.artifactory.util.CollectionUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.artifactory.security.PermissionTarget.*;
import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_NULL;

/**
 * @author Dan Feldman
 */
@Getter
@Setter
@NoArgsConstructor
@JsonSerialize(include = NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllPermissionTargetsResourcesUIModel extends BaseModel {

    private String name;
    //Total repos in this permission target, shown when user press (see all) button
    private Set<RepoKeyType> repos;
    //Total builds in this permission target, shown when user press (see all) button
    private List<String> buildIncludePatterns;
    private List<String> buildExcludePatterns;

    @JsonIgnore //Here as a convenience instead of passing it down all the method chain the constructor calls
    private List<RepoKeyType> allRealRepos;

    public AllPermissionTargetsResourcesUIModel(PermissionTargetModel backendModel, List<RepoKeyType> allRealRepos) {
        this.name = backendModel.getName();
        this.allRealRepos = allRealRepos;
        populateBuildModel(backendModel);
        populateRepoModel(backendModel);
    }

    private void populateBuildModel(PermissionTargetModel backendModel) {
        RepoPermissionTargetModel buildModel = backendModel.getBuild();
        if (buildModel != null) {
            this.buildIncludePatterns = Optional.ofNullable(buildModel.getIncludePatterns()).orElse(Lists.newArrayList());
            this.buildExcludePatterns = Optional.ofNullable(buildModel.getExcludePatterns()).orElse(Lists.newArrayList());
        }
    }

    private void populateRepoModel(PermissionTargetModel backendModel) {
        RepoPermissionTargetModel repoModel = backendModel.getRepo();
        if (repoModel != null) {
            repos = repoModel.getRepositories()
                    .stream()
                    .map(this::repoKeyTypeByRepoKey)
                    .collect(Collectors.toSet());
            inflateAnyVariantsToRepoKeys();
        }
    }

    /**
     * Replaces the 'ANY' holder variants {@link org.artifactory.security.PermissionTarget} with actual repo keys
     */
    private void inflateAnyVariantsToRepoKeys() {
        if (CollectionUtils.isNullOrEmpty(repos)) {
            return;
        }
        if (repos.stream().anyMatch(repoKeyType -> ANY_REPO.equals(repoKeyType.getRepoKey()))) {
            List<String> allRepoKeys = allRealRepos
                    .stream()
                    .map(RepoKeyType::getRepoKey)
                    .collect(Collectors.toList());
            repos.clear();
            repos.addAll(allRepoKeys.stream()
                    .map(this::repoKeyTypeByRepoKey)
                    .collect(Collectors.toSet()));
        }
        inflateAnyVariantToRepoKeys(ANY_LOCAL_REPO, RepoKeyType::getIsLocal);
        inflateAnyVariantToRepoKeys(ANY_REMOTE_REPO, RepoKeyType::getIsRemote);
        inflateAnyVariantToRepoKeys(ANY_DISTRIBUTION_REPO, RepoKeyType::isDistribution);
    }

    /**
     * Filters repos from the 'all' list based on an 'ANY_' variant and populates the outgoing repo list accordingly
     */
    private void inflateAnyVariantToRepoKeys(String anyVariant, Predicate<RepoKeyType> filterReposBy) {
        if (repos.stream().anyMatch(repo -> repo.getRepoKey().equals(anyVariant))) {
            List<RepoKeyType> allReposByAnyVariant = allRealRepos
                    .stream()
                    .filter(filterReposBy)
                    .collect(Collectors.toList());
            repos = repos.stream()
                    .filter(repoKeyType -> !anyVariant.equals(repoKeyType.getRepoKey()))
                    .collect(Collectors.toSet());
            repos.addAll(allReposByAnyVariant);
        }
    }

    private RepoKeyType repoKeyTypeByRepoKey(String repoKey) {
        RepoKeyType repoKeyType = allRealRepos.stream()
                .filter(repo -> repo.getRepoKey().equals(repoKey))
                .findFirst()
                .orElse(null);
        if (repoKeyType == null) {
            repoKeyType = new RepoKeyType();
            repoKeyType.setRepoKey(repoKey);
        }
        return repoKeyType;
    }
}
