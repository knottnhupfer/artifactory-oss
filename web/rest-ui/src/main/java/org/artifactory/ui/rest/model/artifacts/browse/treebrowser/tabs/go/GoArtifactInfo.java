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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.go;

import org.artifactory.addon.go.GoDependency;
import org.artifactory.addon.go.GoInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

import java.util.List;

/**
 * @author Liz Dashevski
 */
public class GoArtifactInfo extends BaseArtifactInfo {

    private GoInfo goInfo;
    private List<GoDependency> goDependencyList;

    @SuppressWarnings({"UnusedDeclaration"})
    public GoInfo getGoInfo() {
        return goInfo;
    }

    public void setGoInfo(GoInfo goInfo) {
        this.goInfo = goInfo;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public List<GoDependency> getGoDependencies() {
        return goDependencyList;
    }

    public void setGoDependencies(List<GoDependency> goDependencyList) {
        this.goDependencyList = goDependencyList;
    }
}
