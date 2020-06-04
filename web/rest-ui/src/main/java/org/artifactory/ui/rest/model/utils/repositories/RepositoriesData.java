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

package org.artifactory.ui.rest.model.utils.repositories;

import org.artifactory.rest.common.model.BaseModel;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class RepositoriesData extends BaseModel {

    private List<String> repoList;
    private List<RepoKeyType> repoTypesList;
    private Integer fileUploadMaxSizeMb;

    public RepositoriesData() {
    }

    public RepositoriesData(List<String> repoData) {
        this.repoList = repoData;
    }

    public List<String> getRepoList() {
        return repoList;
    }

    public void setRepoList(List<String> repoList) {
        this.repoList = repoList;
    }

    public List<RepoKeyType> getRepoTypesList() {
        return repoTypesList;
    }

    public void setRepoTypesList(List<RepoKeyType> repoTypesList) {
        this.repoTypesList = repoTypesList;
    }

    public Integer getFileUploadMaxSizeMb() {
        return fileUploadMaxSizeMb;
    }

    public void setFileUploadMaxSizeMb(Integer fileUploadMaxSizeMb) {
        this.fileUploadMaxSizeMb = fileUploadMaxSizeMb;
    }
}
