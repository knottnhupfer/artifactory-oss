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

package org.artifactory.ui.rest.model.artifacts.search.quicksearch;

import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearch;


/**
 * @author Chen Keinan
 */
public class QuickSearch extends BaseSearch {
    private String query;
    private String relativePath;
    private ItemSearchResults<ArtifactSearchResult> searchResultItemSearchResults;

    public QuickSearch() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public ItemSearchResults<ArtifactSearchResult> getSearchResultItemSearchResults() {
        return searchResultItemSearchResults;
    }

    public void setSearchResultItemSearchResults(
            ItemSearchResults<ArtifactSearchResult> searchResultItemSearchResults) {
        this.searchResultItemSearchResults = searchResultItemSearchResults;
    }
}
