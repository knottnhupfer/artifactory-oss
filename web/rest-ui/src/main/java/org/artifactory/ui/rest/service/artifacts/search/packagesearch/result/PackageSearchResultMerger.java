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

import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageSearchResult;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * A package search result merger.
 * <p>
 * A merger is called after all results collected and enables to merge a collection of results into a single result.
 * </p>
 * @author Yinon Avraham
 * @see DummyPackageSearchResultMerger
 */
public interface PackageSearchResultMerger {

    /**
     * Get a key by which the given result should be merged
     * @param result the result for which to get the merge key
     * @return the merge key
     */
    @Nonnull
    String getMergeKey(PackageSearchResult result);

    /**
     * Merge the given results into a single result
     * @param packageSearchResults the set of results to be merged
     * @return the merged result
     */
    @Nonnull
    PackageSearchResult merge(Set<PackageSearchResult> packageSearchResults);

    /**
     * Indicator for whether the merger should be used always, even if there is a single entry.
     */
    boolean isOperateOnSingleEntry();

}
