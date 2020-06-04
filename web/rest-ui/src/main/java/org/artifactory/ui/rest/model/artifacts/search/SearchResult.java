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

import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.rest.common.service.RestResponse;

import java.util.Collection;

/**
 * @author Shay Yaakov
 */
public class SearchResult <T extends BaseSearchResult> extends BaseModel {

    private Collection<T> results;
    private String searchExpression;
    private long resultsCount;
    private boolean isLimitSearchResults;

    public SearchResult(){} // for jackson

    public SearchResult(Collection<T> results, String searchExpression, long resultsCount, boolean isLimitSearchResults) {
        this.results = results;
        this.searchExpression = searchExpression;
        this.resultsCount = resultsCount;
        this.isLimitSearchResults = isLimitSearchResults;
    }

    public String getSearchExpression() {
        return searchExpression;
    }

    public Collection<T> getResults() {
        return results;
    }

    public String getMessage() {
        int maxResults = ConstantValues.searchMaxResults.getInt();
        int queryLimit = ConstantValues.searchUserQueryLimit.getInt();

        StringBuilder msg = new StringBuilder();
        //Return this only if we limit the search results and don't return the full number of results found
        if (isLimitSearchResults && resultsCount > maxResults) {
            msg.append("Showing first ").append(maxResults).append(" out of ").
                    append(resultsCount == queryLimit ? "more than " : "")
                    .append(resultsCount).append(" matches found");
        } else if (isLimitSearchResults && resultsCount == -1) {
            msg.append("Showing first ").append(maxResults).append(" found matches");
        } else {
            msg.append("Search Results - ").append(resultsCount).append(" Items");
        }
        return msg.toString();
    }

    public void addNotifications(RestResponse response) {
        if (resultsCount == 0) {
            response.warn("No artifacts found. You can broaden your search by using the * and ? wildcards");
        }
        if (isLimitSearchResults && resultsCount >= ConstantValues.searchMaxResults.getInt()) {
            response.warn("Search results are limited. Please consider refining your search.");
        }
    }
}
