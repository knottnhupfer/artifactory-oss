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

package org.artifactory.ui.rest.model.artifacts.search;

import lombok.Data;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageNativeModel;

import java.util.Collection;


/**
 * @author ortalh
 */
@Data
public class PackagesNativeModel implements RestModel {
    private Collection<PackageNativeModel> results;
    private long resultsCount;

    //Don't remove this, the test needs it
    public PackagesNativeModel() {

    }

    public PackagesNativeModel(Collection<PackageNativeModel> results, long resultsCount) {
        this.results = results;
        this.resultsCount = resultsCount;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
