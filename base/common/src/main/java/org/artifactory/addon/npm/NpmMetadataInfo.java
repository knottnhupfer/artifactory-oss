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

package org.artifactory.addon.npm;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class NpmMetadataInfo {

    private NpmInfo npmInfo;
    private List<NpmDependency> npmDependencies;
    private List<NpmDependency> npmDevDependencies;


    public NpmMetadataInfo(NpmInfo npmInfo, List<NpmDependency> dependencies, List<NpmDependency> devDependencies) {
        this.npmInfo = npmInfo;
        this.npmDependencies = dependencies;
        this.npmDevDependencies = devDependencies;
    }

    public NpmInfo getNpmInfo() {
        return npmInfo;
    }

    public void setNpmInfo(NpmInfo npmInfo) {
        this.npmInfo = npmInfo;
    }

    public List<NpmDependency> getNpmDependencies() {
        return npmDependencies;
    }

    public List<NpmDependency> getNpmDevDependencies() {
        return npmDevDependencies;
    }

    public void setNpmDependencies(List<NpmDependency> npmDependencies) {
        this.npmDependencies = npmDependencies;
    }

    public void setNpmDevDependencies(List<NpmDependency> npmDevDependencies) {
        this.npmDevDependencies = npmDevDependencies;
    }

}
