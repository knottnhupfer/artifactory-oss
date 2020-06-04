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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.cran;

import org.artifactory.addon.cran.CranDependencyMetadataInfo;
import org.artifactory.addon.cran.CranInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

import java.util.List;

/**
 * @author Inbar Tal
 */
public class CranArtifactInfo extends BaseArtifactInfo {

    private CranInfo cranInfo;
    private List<CranDependencyMetadataInfo> cranDependencies;
    private List<CranDependencyMetadataInfo> cranImports;
    private List<CranDependencyMetadataInfo> cranSuggests;

    public CranInfo getCranInfo() {
        return cranInfo;
    }

    public void setCranInfo(CranInfo cranInfo) {
        this.cranInfo = cranInfo;
    }

    public List<CranDependencyMetadataInfo> getCranDependencies() {
        return cranDependencies;
    }

    public void setCranDependencies(List<CranDependencyMetadataInfo> cranDependencies) {
        this.cranDependencies = cranDependencies;
    }

    public List<CranDependencyMetadataInfo> getCranImports() {
        return cranImports;
    }

    public void setCranImports(List<CranDependencyMetadataInfo> cranImports) {
        this.cranImports = cranImports;
    }

    public List<CranDependencyMetadataInfo> getCranSuggests() {
        return cranSuggests;
    }

    public void setCranSuggests(List<CranDependencyMetadataInfo> cranSuggests) {
        this.cranSuggests = cranSuggests;
    }
}


