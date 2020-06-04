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

import org.artifactory.api.license.ModuleLicenseModel;
import org.artifactory.rest.common.model.BaseModel;

import java.util.Collection;
import java.util.Set;

/**
 * @author Chen Keinan
 */
public class BuildLicenseModel extends BaseModel {

    private Collection<ModuleLicenseModel> licenses;
    private Set<ModuleLicenseModel> publishedModules;
    private Set<String> scopes;

    public BuildLicenseModel() {
    }

    public BuildLicenseModel(Collection<ModuleLicenseModel> values,
                             Set<ModuleLicenseModel> publishedModules, Set<String> scopes) {
        this.licenses = values;
        this.publishedModules = publishedModules;
        this.scopes = scopes;
    }

    public Collection<ModuleLicenseModel> getLicenses() {
        return licenses;
    }

    public void setLicenses(Collection<ModuleLicenseModel> licenses) {
        this.licenses = licenses;
    }

    public Set<ModuleLicenseModel> getPublishedModules() {
        return publishedModules;
    }

    public void setPublishedModules(Set<ModuleLicenseModel> publishedModules) {
        this.publishedModules = publishedModules;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }
}
