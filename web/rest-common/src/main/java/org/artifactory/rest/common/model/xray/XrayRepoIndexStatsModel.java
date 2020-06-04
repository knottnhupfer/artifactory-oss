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

package org.artifactory.rest.common.model.xray;

import org.artifactory.rest.common.model.BaseModel;

/**
 * Model that represent a repository index state.
 * Potential is all the artifacts that can be indexed
 * (@see XrayHandler#extensionsForIndex)
 *
 * @author Shay Bagants
 */
public class XrayRepoIndexStatsModel extends BaseModel {
    private long potential = 0;

    public XrayRepoIndexStatsModel() {
    }

    public XrayRepoIndexStatsModel(long potential) {
        this.potential = potential;
    }

    public long getPotential() {
        return potential;
    }

    public void setPotential(long potential) {
        this.potential = potential;
    }
}
