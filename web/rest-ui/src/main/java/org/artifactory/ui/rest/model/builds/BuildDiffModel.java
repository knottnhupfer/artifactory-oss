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

package org.artifactory.ui.rest.model.builds;

import org.artifactory.rest.common.model.BaseModel;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class BuildDiffModel extends BaseModel {

    private List<ModuleArtifactModel> artifacts;
    private List<ModuleDependencyModel> dependencies;
    private List<BuildPropsModel> props;

    public List<ModuleArtifactModel> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<ModuleArtifactModel> artifacts) {
        this.artifacts = artifacts;
    }

    public List<ModuleDependencyModel> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<ModuleDependencyModel> dependencies) {
        this.dependencies = dependencies;
    }

    public List<BuildPropsModel> getProps() {
        return props;
    }

    public void setProps(List<BuildPropsModel> props) {
        this.props = props;
    }
}
