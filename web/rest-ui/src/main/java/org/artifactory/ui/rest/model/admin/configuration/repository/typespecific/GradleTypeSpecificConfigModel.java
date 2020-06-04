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

package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.util.JsonUtil;

import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_SUPPRESS_POM_CHECKS;

/**
 * @author Dan Feldman
 */
public class GradleTypeSpecificConfigModel extends MavenTypeSpecificConfigModel {

    //local
    private Boolean suppressPomConsistencyChecks = DEFAULT_SUPPRESS_POM_CHECKS;

    @Override
    public Boolean getSuppressPomConsistencyChecks() {
        return this.suppressPomConsistencyChecks;
    }

    @Override
    public void setSuppressPomConsistencyChecks(Boolean suppressPomConsistencyChecks) {
        this.suppressPomConsistencyChecks = suppressPomConsistencyChecks;
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.Gradle;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
