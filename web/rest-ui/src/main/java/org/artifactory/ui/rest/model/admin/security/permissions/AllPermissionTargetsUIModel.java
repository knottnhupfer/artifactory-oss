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

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.security.PrincipalConfiguration;
import org.artifactory.security.permissions.PermissionTargetModel;
import org.artifactory.security.permissions.RepoPermissionTargetModel;
import org.artifactory.util.CollectionUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jfrog.common.StreamSupportUtils;

import java.util.Set;
import java.util.stream.Collectors;

import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_NULL;

/**
 * @author Dan Feldman
 * @author Omri Ziv
 */
@Getter
@Setter
@NoArgsConstructor
@JsonSerialize(include = NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllPermissionTargetsUIModel extends BaseModel {

    //Max no. of elements to show in a column in the Permission Management screen.
    private static final int MAX_COLUMN_ELEMENTS = 6;

    private String name;
    private boolean hasRepos;
    private boolean hasBuilds;
    private Set<String> groups = Sets.newHashSet();
    private Set<String> users = Sets.newHashSet();
    private int totalUsers;
    private int totalGroups;

    public AllPermissionTargetsUIModel(PermissionTargetModel backendModel) {
        this.name = backendModel.getName();
        populateBuildModel(backendModel);
        populateRepoModel(backendModel);
        totalUsers = users.size();
        users = StreamSupportUtils.stream(users)
                .limit(MAX_COLUMN_ELEMENTS)
                .collect(Collectors.toSet());
        totalGroups = groups.size();
        groups = StreamSupportUtils.stream(groups)
                .limit(MAX_COLUMN_ELEMENTS)
                .collect(Collectors.toSet());
    }

    private void populateBuildModel(PermissionTargetModel backendModel) {
        RepoPermissionTargetModel buildModel = backendModel.getBuild();
        if (buildModel != null) {
            hasBuilds = CollectionUtils.notNullOrEmpty(buildModel.getIncludePatterns())
                    || CollectionUtils.notNullOrEmpty(buildModel.getExcludePatterns());
            populateByRepoModel(buildModel);
        }
    }

    private void populateRepoModel(PermissionTargetModel backendModel) {
        RepoPermissionTargetModel repoModel = backendModel.getRepo();
        if (repoModel != null) {
            hasRepos = CollectionUtils.notNullOrEmpty(repoModel.getRepositories());
            populateByRepoModel(repoModel);
        }
    }

    private void populateByRepoModel(RepoPermissionTargetModel repoModel) {
        PrincipalConfiguration repoActions = repoModel.getActions();
        if (repoActions.getUsers() != null) {
            users.addAll(repoActions.getUsers().keySet());
        }
        if (repoActions.getGroups() != null) {
            groups.addAll(repoActions.getGroups().keySet());
        }
    }

}
