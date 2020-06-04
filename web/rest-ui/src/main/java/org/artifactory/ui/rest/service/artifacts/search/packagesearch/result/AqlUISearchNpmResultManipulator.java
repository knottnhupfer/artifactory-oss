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

package org.artifactory.ui.rest.service.artifacts.search.packagesearch.result;

import org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.PackageSearchCriteria;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageSearchResult;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds a scope field whenever a scoped package is being returned (name is @{scope}/{name})
 *
 * @author Dan Feldman
 */
public class AqlUISearchNpmResultManipulator implements AqlUISearchResultManipulator {
    private static final Logger log = LoggerFactory.getLogger(AqlUISearchNpmResultManipulator.class);

    @Override
    public void manipulate(PackageSearchResult result) {
        if (CollectionUtils.isNullOrEmpty(result.getExtraFieldsMap().get(PackageSearchCriteria.npmName.name()))) {
            return;
        }
        String npmName = result.getExtraFieldsMap().get(PackageSearchCriteria.npmName.name()).iterator().next();
        if (npmName.contains("@")) {
            try {
                String[] split = npmName.split("/");
                log.debug("Manipulator adding npm scope: '{}' and changing package name to '{}'", split[0], split[1]);
                result.getExtraFieldsMap().put(PackageSearchCriteria.npmScope.name(), split[0]);
            } catch (Exception e) {
                log.warn("Error parsing npm package name: '{}", npmName);
            }
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && getClass() == o.getClass();
    }
}
