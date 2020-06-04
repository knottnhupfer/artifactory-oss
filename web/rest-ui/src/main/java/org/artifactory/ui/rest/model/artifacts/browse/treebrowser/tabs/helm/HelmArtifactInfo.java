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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.helm;

import org.artifactory.addon.helm.HelmDependencyMetadataInfo;
import org.artifactory.addon.helm.HelmInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

import java.util.List;

/**
 * @author nadavy
 */
public class HelmArtifactInfo extends BaseArtifactInfo {

    private HelmInfo helmInfo;
    private List<HelmDependencyMetadataInfo> helmDependencies;

    @SuppressWarnings({"UnusedDeclaration"})
    public HelmInfo getHelmInfo() {
        return helmInfo;
    }

    public void setHelmInfo(HelmInfo helmInfo) {
        this.helmInfo = helmInfo;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public List<HelmDependencyMetadataInfo> getHelmDependencies() {
        return helmDependencies;
    }

    public void setHelmDependencies(List<HelmDependencyMetadataInfo> helmDependencies) {
        this.helmDependencies = helmDependencies;
    }
}
