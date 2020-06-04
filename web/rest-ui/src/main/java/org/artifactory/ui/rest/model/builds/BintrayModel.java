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

import org.artifactory.api.bintray.BintrayParams;
import org.artifactory.rest.common.model.BaseModel;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class BintrayModel extends BaseModel {

    private List<String> binTrayRepositories;
    private List<String> binTrayPackages;
    private List<String> binTrayVersions;
    private BintrayParams bintrayParams;


    public List<String> getBinTrayRepositories() {
        return binTrayRepositories;
    }

    public void setBinTrayRepositories(List<String> binTrayRepositories) {
        this.binTrayRepositories = binTrayRepositories;
    }

    public List<String> getBinTrayPackages() {
        return binTrayPackages;
    }

    public void setBinTrayPackages(List<String> binTrayPackages) {
        this.binTrayPackages = binTrayPackages;
    }

    public List<String> getBinTrayVersions() {
        return binTrayVersions;
    }

    public void setBinTrayVersions(List<String> binTrayVersions) {
        this.binTrayVersions = binTrayVersions;
    }

    public BintrayParams getBintrayParams() {
        return bintrayParams;
    }

    public void setBintrayParams(BintrayParams bintrayParams) {
        this.bintrayParams = bintrayParams;
    }
}
