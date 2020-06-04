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

package org.artifactory.api.rest.distribution.bundle.models;

import com.google.common.collect.Sets;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;

/**
 * @author Tomer Mayost
 */
@Data
@NoArgsConstructor
public class BundlesResponse {
    LinkedHashMap<String, Set<BundleVersion>> bundles = new LinkedHashMap<>();

    public void add(String bundleName, BundleVersion versions) {
        Set<BundleVersion> bundleVersions = Optional.ofNullable(bundles.get(bundleName)).orElseGet(Sets::newHashSet);
        bundleVersions.add(versions);
        bundles.put(bundleName, bundleVersions);
    }
}
