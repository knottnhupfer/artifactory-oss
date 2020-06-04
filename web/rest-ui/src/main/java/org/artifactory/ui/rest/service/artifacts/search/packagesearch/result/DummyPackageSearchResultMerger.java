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
 * A dummy package search result merger that does nothing - merge by repo path (which is expected to be unique).
 * @author Yinon Avraham
 */
public class DummyPackageSearchResultMerger implements PackageSearchResultMerger {

    public static final PackageSearchResultMerger DUMMY_MERGER = new DummyPackageSearchResultMerger();

    private DummyPackageSearchResultMerger() {}

    @Override
    @Nonnull
    public String getMergeKey(PackageSearchResult result) {
        return result.getRepoPath().toPath();
    }

    @Override
    @Nonnull
    public PackageSearchResult merge(Set<PackageSearchResult> packageSearchResults) {
        assert packageSearchResults.size() == 1;
        return packageSearchResults.iterator().next();
    }

    @Override
    public boolean isOperateOnSingleEntry() {
        return false;
    }
}
