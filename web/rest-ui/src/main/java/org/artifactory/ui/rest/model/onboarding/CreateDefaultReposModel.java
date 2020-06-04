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

package org.artifactory.ui.rest.model.onboarding;

import com.google.common.collect.Lists;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.model.BaseModel;

import java.util.List;

/**
 * List of requested repo types to create default repos for
 * url /api/onboarding/createDefaultRepos
 *
 * @author nadavy
 */
public class CreateDefaultReposModel extends BaseModel {
    private List<RepoType> repoTypeList = Lists.newArrayList();
    private boolean fromOnboarding;

    public CreateDefaultReposModel(List<RepoType> repoTypeList, boolean fromOnboarding) {
        this.repoTypeList = repoTypeList;
        this.fromOnboarding = fromOnboarding;
    }

    public CreateDefaultReposModel() {

    }

    public boolean isFromOnboarding() {
        return fromOnboarding;
    }

    public List<RepoType> getRepoTypeList() {
        return repoTypeList;
    }
}
