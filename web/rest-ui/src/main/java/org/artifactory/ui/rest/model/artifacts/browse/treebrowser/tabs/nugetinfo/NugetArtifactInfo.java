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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.nugetinfo;

import org.artifactory.nuget.NuMetaData;
import org.artifactory.nuget.NuSpecFrameworkAssembly;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class NugetArtifactInfo extends BaseArtifactInfo {

    private NugetGeneralInfo nugetGeneralInfo;
    private NugetDescription nugetDescription;
    private List dependencies;
    private List<NuSpecFrameworkAssembly> frameworkAssemblies;
    private List references;
    public NugetArtifactInfo() {
    }

    public NugetArtifactInfo(NuMetaData nuMetaData) {
        nugetGeneralInfo = new NugetGeneralInfo(nuMetaData);
        nugetDescription = new NugetDescription(nuMetaData);
        this.dependencies = nuMetaData.getDependencies();
        this.frameworkAssemblies = nuMetaData.getFrameworkAssemblies();
        this.references = nuMetaData.getReferences();
        super.setRepoKey(null);
        super.setPath(null);
    }

    public NugetGeneralInfo getNugetGeneralInfo() {
        return nugetGeneralInfo;
    }

    public void setNugetGeneralInfo(
            NugetGeneralInfo nugetGeneralInfo) {
        this.nugetGeneralInfo = nugetGeneralInfo;
    }

    public NugetDescription getNugetDescription() {
        return nugetDescription;
    }

    public void setNugetDescription(
            NugetDescription nugetDescription) {
        this.nugetDescription = nugetDescription;
    }

    public List getDependencies() {
        return dependencies;
    }

    public void setDependencies(List dependencies) {
        this.dependencies = dependencies;
    }

    public List<NuSpecFrameworkAssembly> getFrameworkAssemblies() {
        return frameworkAssemblies;
    }

    public void setFrameworkAssemblies(List<NuSpecFrameworkAssembly> frameworkAssemblies) {
        this.frameworkAssemblies = frameworkAssemblies;
    }

    public List getReferences() {
        return references;
    }

    public void setReferences(List references) {
        this.references = references;
    }
}
