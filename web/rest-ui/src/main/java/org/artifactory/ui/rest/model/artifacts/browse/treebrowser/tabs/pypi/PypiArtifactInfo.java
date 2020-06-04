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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.pypi;

import org.artifactory.addon.pypi.PypiPkgInfo;
import org.artifactory.addon.pypi.PypiPkgMetadata;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Chen Keinan
 */
public class PypiArtifactInfo extends BaseArtifactInfo {

    private PypiPkgInfo pypiPkgInfo;
    @Nullable
    public List<String> categories;
    @Nullable
    public List<String> requires;
    @Nullable
    public List<String> provides;
    @Nullable
    public List<String> obsoletes;

    PypiArtifactInfo() {
    }

    public PypiArtifactInfo(PypiPkgMetadata pypiPkgMetadata) {
        this.pypiPkgInfo = pypiPkgMetadata.getPypiPkgInfo();
        this.categories = pypiPkgMetadata.getCategories();
        this.requires = pypiPkgMetadata.getRequires();
        this.provides = pypiPkgMetadata.getProvides();
        this.obsoletes = pypiPkgMetadata.getObsoletes();
        clearRepoData();
    }

    public PypiPkgInfo getPypiPkgInfo() {
        return pypiPkgInfo;
    }

    public void setPypiPkgInfo(PypiPkgInfo pypiPkgInfo) {
        this.pypiPkgInfo = pypiPkgInfo;
    }

    @Nullable
    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(@Nullable List<String> categories) {
        this.categories = categories;
    }

    @Nullable
    public List<String> getRequires() {
        return requires;
    }

    public void setRequires(@Nullable List<String> requires) {
        this.requires = requires;
    }

    @Nullable
    public List<String> getProvides() {
        return provides;
    }

    public void setProvides(@Nullable List<String> provides) {
        this.provides = provides;
    }

    @Nullable
    public List<String> getObsoletes() {
        return obsoletes;
    }

    public void setObsoletes(@Nullable List<String> obsoletes) {
        this.obsoletes = obsoletes;
    }
}
